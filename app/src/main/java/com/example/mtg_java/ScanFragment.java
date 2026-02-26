package com.example.mtg_java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mtg_java.scanner.DebugSaver;
import com.example.mtg_java.scanner.ImageProxyUtils;
import com.example.mtg_java.scanner.LiveDebugOverlay;
import com.example.mtg_java.scanner.RectangleDetector;
import com.example.mtg_java.scanner.TFLiteImageClassifier;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;

    private ExecutorService cameraExecutor;
    private TFLiteImageClassifier classifier;
    private PreviewView previewView;
    private LiveDebugOverlay liveOverlay;

    private boolean navigated = false;

    private static final long TICK_MS = 50L;
    private static final long WARMUP_MS = 150L;

    private static final int GATE_TOP = 135;
    private static final int GATE_BOTTOM = 1124;

    private static final int CROP_L = 20;
    private static final int CROP_T = 220;
    private static final int CROP_R = 700;
    private static final int CROP_B = 720;

    private static final int MODEL_W = 564;
    private static final int MODEL_H = 411;

    // Keep as a secondary safety check (less strict than detector stage)
    private static final float CARD_ASPECT = 5f / 7f;
    private static final float ASPECT_WIGGLE = 0.30f; // a bit looser than before

    private long lastDebugSaveMs = 0L;
    private static final long DEBUG_SAVE_MIN_INTERVAL_MS = 50L;

    // Fast stability (EMA)
    private Rect stableRect = null;
    private long stableRectTs = 0L;
    private static final long STABLE_TTL_MS = 300L;
    private static final float STABLE_ALPHA = 0.35f;
    private static final float STABLE_IOU_MIN = 0.30f;

    // Simple model cooldown so we don't spam inference when we already have a good rect
    private long lastInferenceMs = 0L;
    private static final long INFERENCE_MIN_INTERVAL_MS = 80L;

    public ScanFragment() {
        super(R.layout.fragment_scan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);
        liveOverlay = view.findViewById(R.id.liveOverlay);

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new BrowseFragment())
                    .addToBackStack(null)
                    .commit();
        });

        if (!OpenCVLoader.initLocal()) {
            Toast.makeText(requireContext(), "OpenCV init failed", Toast.LENGTH_LONG).show();
            return;
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        MyApp app = (MyApp) requireActivity().getApplication();
        classifier = app.getClassifierBlocking();
        if (classifier == null) {
            Toast.makeText(requireContext(), "Model not available", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            previewView.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE
            );
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                int rotation = getPreviewRotationSafe();

                Preview preview = new Preview.Builder()
                        .setTargetRotation(rotation)
                        .build();

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetRotation(rotation)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(720, 1280))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                final long[] startTime = {0L};
                final long[] lastTick = {0L};

                analysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    try {
                        long now = System.currentTimeMillis();
                        if (startTime[0] == 0L) startTime[0] = now;

                        if (now - lastTick[0] < TICK_MS) return;
                        lastTick[0] = now;

                        if (navigated) return;

                        Bitmap frame = ImageProxyUtils.toRgbaBitmapUpright(imageProxy);
                        int frameW = frame.getWidth();
                        int frameH = frame.getHeight();

                        int gateTop = Math.max(0, GATE_TOP);
                        int gateBottom = Math.min(frameH, GATE_BOTTOM);
                        Rect cropRect = new Rect(CROP_L, CROP_T, CROP_R, CROP_B);

                        // Detect inside ROI band (fast)
                        RectangleDetector.Quad quad = RectangleDetector.findBestQuad(frame, gateTop, gateBottom);

                        Rect rawRect = (quad != null) ? quad.boundingRect : null;
                        Rect drawRect = rawRect;

                        // If we have a rect, stabilize it (also improves overlay)
                        if (rawRect != null) {
                            drawRect = stabilizeRect(rawRect, now);
                        }

                        // ALWAYS draw every tick (even if not accepted)
                        Rect finalDrawRect = drawRect;
                        requireActivity().runOnUiThread(() -> {
                            if (liveOverlay != null) {
                                liveOverlay.update(finalDrawRect, frameW, frameH, gateTop, gateBottom, cropRect);
                            }
                        });

                        if (quad == null || drawRect == null) return;

                        // Warmup gate (avoid immediate trigger while camera settles)
                        if (now - startTime[0] < WARMUP_MS) return;

                        // --- Acceptance thresholds (Step 3) ---
                        // Use areaNorm + aspectScore; score is helpful but device-dependent.
                        // These defaults are fast but not reckless.
                        if (quad.areaNorm < 0.18) return;      // too small in ROI band
                        if (quad.aspectScore < 0.55f) return;  // plausible card-ish aspect
                        if (quad.score < 2.8) return;          // combined score floor

                        Rect rect = drawRect;

                        // Extra guards (cheap)
                        if (!isCardAspect(rect)) return;
                        if (!isBigEnough(rect, frameW, frameH)) return;
                        if (rect.top < gateTop || rect.bottom > gateBottom) return;

                        // Avoid spamming inference
                        if (now - lastInferenceMs < INFERENCE_MIN_INTERVAL_MS) return;
                        lastInferenceMs = now;

                        // Crop fixed window (your chosen model crop)
                        Bitmap rawCrop = cropClamped(frame, CROP_L, CROP_T, CROP_R, CROP_B);
                        Bitmap resized = Bitmap.createScaledBitmap(rawCrop, MODEL_W, MODEL_H, true);

                        TFLiteImageClassifier.Result r = classifier.classifyTop1(resized);

                        // Save debug artifacts
                        if (now - lastDebugSaveMs >= DEBUG_SAVE_MIN_INTERVAL_MS) {
                            lastDebugSaveMs = now;

                            Bitmap debugBmp = com.example.mtg_java.scanner.DebugDraw.drawDebug(
                                    frame, rect, gateTop, gateBottom, cropRect
                            );
                            DebugSaver.saveToFolder(requireContext(), debugBmp, "debug", "debug");
                            DebugSaver.saveToFolder(requireContext(), resized, "crops_debug", "model_input");
                        }

                        // Model confidence threshold (0.85 is very strict; start here)
                        if (r.prob < 0.75f) return;

                        requireActivity().runOnUiThread(() -> navigateToResult(r.label));

                    } finally {
                        imageProxy.close();
                    }
                });

                int vw = previewView.getWidth();
                int vh = previewView.getHeight();
                if (vw <= 0 || vh <= 0) {
                    vw = 1;
                    vh = 1;
                }

                ViewPort viewPort = new ViewPort.Builder(new Rational(vw, vh), rotation).build();

                UseCaseGroup group = new UseCaseGroup.Builder()
                        .setViewPort(viewPort)
                        .addUseCase(preview)
                        .addUseCase(analysis)
                        .build();

                provider.unbindAll();
                provider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        group
                );

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private int getPreviewRotationSafe() {
        if (previewView != null && previewView.getDisplay() != null) {
            return previewView.getDisplay().getRotation();
        }
        return Surface.ROTATION_0;
    }

    private void navigateToResult(String id) {
        if (navigated) return;
        navigated = true;

        Intent i = new Intent(requireContext(), CardDetailActivity.class);
        i.putExtra("CARD_ID", id);
        startActivity(i);
    }

    private static boolean isCardAspect(Rect r) {
        float w = r.width();
        float h = r.height();
        if (w <= 0 || h <= 0) return false;

        float aspect = w / h; // width/height in upright bitmap coords
        float min = CARD_ASPECT * (1f - ASPECT_WIGGLE);
        float max = CARD_ASPECT * (1f + ASPECT_WIGGLE);
        return aspect >= min && aspect <= max;
    }

    private static boolean isBigEnough(Rect r, int frameW, int frameH) {
        float area = (float) r.width() * r.height();
        float frameArea = (float) frameW * frameH;
        return area >= 0.15f * frameArea;
    }

    private static Bitmap cropClamped(Bitmap src, int left, int top, int right, int bottom) {
        int l = Math.max(0, Math.min(left, src.getWidth() - 2));
        int t = Math.max(0, Math.min(top, src.getHeight() - 2));
        int r = Math.max(l + 1, Math.min(right, src.getWidth()));
        int b = Math.max(t + 1, Math.min(bottom, src.getHeight()));
        return Bitmap.createBitmap(src, l, t, r - l, b - t);
    }

    // -------- Stability helpers --------
    private Rect stabilizeRect(Rect r, long now) {
        if (stableRect == null || (now - stableRectTs) > STABLE_TTL_MS) {
            stableRect = new Rect(r);
            stableRectTs = now;
            return stableRect;
        }

        float iou = iou(stableRect, r);
        if (iou < STABLE_IOU_MIN) {
            stableRect = new Rect(r);
            stableRectTs = now;
            return stableRect;
        }

        stableRect = lerpRect(stableRect, r, STABLE_ALPHA);
        stableRectTs = now;
        return stableRect;
    }

    private static Rect lerpRect(Rect a, Rect b, float alpha) {
        int l = (int) (a.left + alpha * (b.left - a.left));
        int t = (int) (a.top + alpha * (b.top - a.top));
        int r = (int) (a.right + alpha * (b.right - a.right));
        int bb = (int) (a.bottom + alpha * (b.bottom - a.bottom));
        return new Rect(l, t, r, bb);
    }

    private static float iou(Rect a, Rect b) {
        int x1 = Math.max(a.left, b.left);
        int y1 = Math.max(a.top, b.top);
        int x2 = Math.min(a.right, b.right);
        int y2 = Math.min(a.bottom, b.bottom);

        int iw = Math.max(0, x2 - x1);
        int ih = Math.max(0, y2 - y1);
        int inter = iw * ih;
        if (inter <= 0) return 0f;

        int areaA = Math.max(1, a.width() * a.height());
        int areaB = Math.max(1, b.width() * b.height());
        int union = areaA + areaB - inter;

        return inter / (float) union;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        navigated = false;
        stableRect = null;
        stableRectTs = 0L;
    }

    @Override
    public void onResume() {
        super.onResume();
        navigated = false;
        stableRect = null;
        stableRectTs = 0L;
    }
}