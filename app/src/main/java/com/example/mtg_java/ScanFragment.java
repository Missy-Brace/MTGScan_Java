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
import com.example.mtg_java.scanner.RectangleDetector;
import com.example.mtg_java.scanner.TFLiteImageClassifier;
import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.OpenCVLoader;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;

    private ExecutorService cameraExecutor;
    private TFLiteImageClassifier classifier;

    private PreviewView previewView;

    private boolean navigated = false;

    private static final long TICK_MS = 10L;
    private static final long WARMUP_MS = 30L;

    // shifted up already
    private static final int GATE_TOP = 135;
    private static final int GATE_BOTTOM = 1124;

    private static final int CROP_L = 20;
    private static final int CROP_T = 220;
    private static final int CROP_R = 700;
    private static final int CROP_B = 720;

    private static final int MODEL_W = 564;
    private static final int MODEL_H = 411;

    private long lastDebugSaveMs = 0L;
    private static final long DEBUG_SAVE_MIN_INTERVAL_MS = 50L;

    // Ratio filter
    private static final float CARD_ASPECT = 5f / 7f;   // ~0.714
    private static final float ASPECT_WIGGLE = 0.25f;   // 25%

    public ScanFragment() {
        super(R.layout.fragment_scan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new BrowseFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Init OpenCV first
        boolean ok = OpenCVLoader.initLocal();
        if (!ok) {
            Toast.makeText(requireContext(), "OpenCV init failed", Toast.LENGTH_LONG).show();
            return;
        }

        // Init executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Get app-scoped classifier (do NOT close in Fragment)
        MyApp app = (MyApp) requireActivity().getApplication();
        classifier = app.getClassifierBlocking();
        if (classifier == null) {
            Toast.makeText(requireContext(), "Model not available", Toast.LENGTH_LONG).show();
            return;
        }

        // Permission then camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // IMPORTANT: wait for PreviewView to be laid out (width/height must be > 0 for ViewPort)
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
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                int rotation = getPreviewRotationSafe();

                Preview preview = new Preview.Builder()
                        .setTargetRotation(rotation)
                        .build();

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetRotation(rotation)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        // Keep your preferred analysis size; viewport will ensure same crop as preview
                        .setTargetResolution(new Size(720, 1280))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                final long[] startTime = {0L};
                final long[] lastTick = {0L};
                final Deque<String> last3 = new ArrayDeque<>(3);

                analysis.setAnalyzer(cameraExecutor, (ImageProxy imageProxy) -> {
                    try {
                        long now = System.currentTimeMillis();
                        if (startTime[0] == 0L) startTime[0] = now;

                        // 1) Throttle analysis frequency
                        if (now - lastTick[0] < TICK_MS) return;
                        lastTick[0] = now;

                        // 2) Get initial image, no alterations
                        Bitmap frame = ImageProxyUtils.toRgbaBitmapUpright(imageProxy);
                        int frameW = frame.getWidth();
                        int frameH = frame.getHeight();

                        // 3) Check if it contains a rectangle (with all the logic in RectangleDetector)
                        // If you kept the old API:
                        Rect rect = RectangleDetector.findBestRectangle(frame);
                        if (rect == null) return;

                        // If you prefer quad API:
                        // RectangleDetector.Quad quad = RectangleDetector.findBestQuadStable(frame);
                        // if (quad == null) return;
                        // Rect rect = quad.boundingRect;

                        // Apply the same extra filters you do in ScanFragment (ratio/area/gate)
                        if (!isCardAspect(rect)) return;
                        if (!isBigEnough(rect, frameW, frameH)) return;

                        int gateTop = Math.max(0, GATE_TOP);
                        int gateBottom = Math.min(frameH, GATE_BOTTOM);
                        if (rect.top < gateTop || rect.bottom > gateBottom) return;

                        // 4) Crop the image (YOU requested: crop using fixed CROP_* constants)
                        // NOTE: this is independent of rect crop, but still only triggers if rect exists & passes filters.
                        Bitmap rawCrop = cropClamped(frame, CROP_L, CROP_T, CROP_R, CROP_B);

                        // 5) Resize the crop to be the same as model's input
                        Bitmap resizedForModel = Bitmap.createScaledBitmap(rawCrop, MODEL_W, MODEL_H, true);

                        // Warmup gate (avoid immediate trigger while camera settles)
                        if (now - startTime[0] < WARMUP_MS) return;

                        // 6) Feed it to model (IMPORTANT: use resizedForModel — same image you save!)
                        TFLiteImageClassifier.Result r = classifier.classifyTop1(resizedForModel);
                        String label = r.label; // expected to be card_id UUID

                        // 7) Save debug artifacts (full frame + exact model input crop)
                        if (now - lastDebugSaveMs >= DEBUG_SAVE_MIN_INTERVAL_MS) {
                            lastDebugSaveMs = now;

                            // full frame with overlay lines
                            Rect cropRect = new Rect(CROP_L, CROP_T, CROP_R, CROP_B);
                            Bitmap debugBmp = com.example.mtg_java.scanner.DebugDraw.drawDebug(
                                    frame, rect, gateTop, gateBottom, cropRect
                            );
                            DebugSaver.saveToFolder(requireContext(), debugBmp, "debug", "debug");

                            // exact model input (what the model actually saw)
                            DebugSaver.saveToFolder(requireContext(), resizedForModel, "crops_debug", "model_input");
                        }

                        // 8) Track 3 successful hits with same top prediction
                        if (last3.size() == 3) last3.removeFirst();
                        last3.addLast(label);

                        if (last3.size() == 3 && allSame(last3)) {
                            requireActivity().runOnUiThread(() -> navigateToResult(label)); // opens CardDetailActivity by CARD_ID
                        }

                    } finally {
                        imageProxy.close();
                    }
                });

                // ✅ FORCE same crop/FOV between Preview and ImageAnalysis
                // (This fixes "debug camera is smaller than UI camera")
                int vw = previewView.getWidth();
                int vh = previewView.getHeight();
                if (vw <= 0 || vh <= 0) {
                    // Should not happen due to previewView.post, but guard anyway
                    vw = 1;
                    vh = 1;
                }

                ViewPort viewPort = new ViewPort.Builder(new Rational(vw, vh), rotation).build();

                UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
                        .setViewPort(viewPort)
                        .addUseCase(preview)
                        .addUseCase(analysis)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        useCaseGroup
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

    private void navigateToResult(String cardId) {
        if (navigated) return;
        navigated = true;

        Intent i = new Intent(requireContext(), CardDetailActivity.class);
        i.putExtra("CARD_ID", cardId);
        startActivity(i);
    }

    private static boolean allSame(Deque<String> last3) {
        if (last3.size() < 3) return false;
        String first = last3.peekFirst();
        for (String s : last3) {
            if (!first.equals(s)) return false;
        }
        return true;
    }

    private static boolean isCardAspect(Rect r) {
        if (r == null) return false;

        float w = r.width();
        float h = r.height();
        if (w <= 0 || h <= 0) return false;

        float aspect = w / h;
        float min = CARD_ASPECT * (1f - ASPECT_WIGGLE);
        float max = CARD_ASPECT * (1f + ASPECT_WIGGLE);
        return (aspect >= min && aspect <= max);
    }

    private static boolean isBigEnough(Rect r, int frameW, int frameH) {
        if (r == null) return false;
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

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // IMPORTANT: wait for layout before startCamera
            if (previewView != null) previewView.post(this::startCamera);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Do NOT close classifier here if it's owned by MyApp and shared across screens
        // if (classifier != null) classifier.close();

        if (cameraExecutor != null) cameraExecutor.shutdown();
        navigated = false;
    }
}