package com.example.mtg_java.scanner;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class RectangleDetector {
    private RectangleDetector() {}

    // Card aspect ~ 5:7
    private static final float TARGET_ASPECT = 5f / 7f; // ~0.714
    private static final float ASPECT_WIGGLE = 0.30f;   // looser than your filter; tighten later

    // Detection thresholds
    private static final double MIN_AREA_RATIO = 0.10;  // card should occupy >= 10% of analysis frame
    private static final double MIN_CONTOUR_AREA = 5_000; // hard floor; depends on resolution
    private static final double APPROX_EPS_RATIO = 0.02;  // polygon approximation aggressiveness

    // Temporal stability
    private static final float IOU_ACCEPT = 0.55f;       // require overlap w/ previous to smooth
    private static final float CORNER_SMOOTH_ALPHA = 0.35f; // EMA: higher = snappier, lower = smoother

    // Keep last stable quad between calls
    private static final Object LOCK = new Object();
    private static Quad lastQuad = null;

    /** Backwards-compatible API: returns best bounding Rect (axis-aligned) */
    public static Rect findBestRectangle(Bitmap bitmap) {
        Quad q = findBestQuadStable(bitmap);
        return q != null ? q.boundingRect : null;
    }

    /** New: returns a stable quad (4 corners) + bounding rect */
    public static Quad findBestQuadStable(Bitmap bitmap) {
        Quad fresh = findBestQuad(bitmap);
        if (fresh == null) return null;

        synchronized (LOCK) {
            if (lastQuad == null) {
                lastQuad = fresh;
                return lastQuad;
            }

            float iou = iou(lastQuad.boundingRect, fresh.boundingRect);

            if (iou >= IOU_ACCEPT) {
                // Smooth corners
                lastQuad = lastQuad.smoothedTowards(fresh, CORNER_SMOOTH_ALPHA);
            } else {
                // If it jumps too far, accept new (or you could require N frames)
                lastQuad = fresh;
            }
            return lastQuad;
        }
    }

    /** Single-frame detection: quad detection + scoring */
    public static Quad findBestQuad(Bitmap bitmap) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        int frameW = rgba.cols();
        int frameH = rgba.rows();
        double frameArea = (double) frameW * frameH;

        // --- Preprocess: grayscale -> CLAHE -> morph gradient -> Canny ---
        Mat gray = new Mat();
        Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);

        // CLAHE boosts local contrast (helps faint borders)
        Mat claheOut = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(gray, claheOut);

        // Blur a bit to reduce noise
        Imgproc.GaussianBlur(claheOut, claheOut, new Size(5, 5), 0);

        // Morphological gradient emphasizes edges (dilate - erode)
        Mat grad = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(claheOut, grad, Imgproc.MORPH_GRADIENT, kernel);

        // Canny on gradient image
        Mat edges = new Mat();
        Imgproc.Canny(grad, edges, 60, 180);

        // Optional: close gaps in edges
        Mat closed = new Mat();
        Imgproc.morphologyEx(edges, closed, Imgproc.MORPH_CLOSE, kernel);

        // --- Find contours ---
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(closed, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.isEmpty()) return null;

        Quad best = null;
        double bestScore = -1;

        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < MIN_CONTOUR_AREA) continue;
            if (area < frameArea * MIN_AREA_RATIO) continue;

            MatOfPoint2f c2f = new MatOfPoint2f(contour.toArray());

            // Approx polygon
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, APPROX_EPS_RATIO * peri, true);

            Point[] pts = approx.toArray();
            if (pts.length != 4) continue;

            MatOfPoint approxInt = new MatOfPoint(pts);
            if (!Imgproc.isContourConvex(approxInt)) continue;

            // Rotated rect for aspect estimation (robust to tilt)
            RotatedRect rr = Imgproc.minAreaRect(approx);
            double w = rr.size.width;
            double h = rr.size.height;
            if (w <= 1 || h <= 1) continue;

            double aspect = Math.min(w, h) / Math.max(w, h); // normalize <= 1
            double target = TARGET_ASPECT;                   // also <= 1
            double minAsp = target * (1.0 - ASPECT_WIGGLE);
            double maxAsp = target * (1.0 + ASPECT_WIGGLE);
            if (aspect < minAsp || aspect > maxAsp) continue;

            // Rectangularity: contour area vs rotated-rect area
            double rrArea = w * h;
            if (rrArea <= 1) continue;
            double rectangularity = area / rrArea; // 1 = perfect
            if (rectangularity < 0.70) continue;   // reject very jagged shapes

            // Score:
            // - prefer larger area
            // - prefer rectangularity closer to 1
            // - prefer aspect closer to target
            double aspectScore = 1.0 - Math.min(1.0, Math.abs(aspect - target) / (target * ASPECT_WIGGLE));
            double score = (area / frameArea) * 3.0
                    + rectangularity * 2.0
                    + aspectScore * 2.0;

            if (score > bestScore) {
                bestScore = score;
                best = Quad.fromPoints(pts);
            }
        }

        return best;
    }

    // ---------- Helper types & math ----------

    public static final class Quad {
        public final PointF[] corners;  // 4 corners (unordered but consistent after smoothing)
        public final Rect boundingRect; // axis-aligned bounding rect

        private Quad(PointF[] corners, Rect rect) {
            this.corners = corners;
            this.boundingRect = rect;
        }

        static Quad fromPoints(Point[] pts) {
            PointF[] cs = new PointF[4];
            for (int i = 0; i < 4; i++) cs[i] = new PointF((float) pts[i].x, (float) pts[i].y);

            Rect r = bounds(cs);
            return new Quad(cs, r);
        }

        Quad smoothedTowards(Quad other, float alpha) {
            // simple per-corner EMA; assumes corner ordering is reasonably consistent
            // (it usually is for approxPolyDP over time; if not, we can add corner ordering)
            PointF[] out = new PointF[4];
            for (int i = 0; i < 4; i++) {
                float x = lerp(this.corners[i].x, other.corners[i].x, alpha);
                float y = lerp(this.corners[i].y, other.corners[i].y, alpha);
                out[i] = new PointF(x, y);
            }
            return new Quad(out, bounds(out));
        }

        private static Rect bounds(PointF[] cs) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
            for (PointF p : cs) {
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }
            return new Rect((int) minX, (int) minY, (int) maxX, (int) maxY);
        }

        private static float lerp(float a, float b, float t) {
            return a + (b - a) * t;
        }
    }

    private static float iou(Rect a, Rect b) {
        if (a == null || b == null) return 0f;
        int left = Math.max(a.left, b.left);
        int top = Math.max(a.top, b.top);
        int right = Math.min(a.right, b.right);
        int bottom = Math.min(a.bottom, b.bottom);

        int iw = Math.max(0, right - left);
        int ih = Math.max(0, bottom - top);
        float inter = iw * ih;

        float areaA = (a.right - a.left) * (float) (a.bottom - a.top);
        float areaB = (b.right - b.left) * (float) (b.bottom - b.top);
        float union = areaA + areaB - inter;

        return union <= 0 ? 0f : (inter / union);
    }
}