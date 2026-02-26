package com.example.mtg_java.scanner;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;

import org.opencv.android.Utils;
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
import java.util.List;

public final class RectangleDetector {
    private RectangleDetector() {}

    // --- tuning knobs ---
    private static final double SCALE = 0.60;          // 0.5–0.7
    private static final int TOP_N = 10;
    private static final double MIN_AREA_RATIO = 0.08; // vs ROI area (small-image ROI area)

    // Target card ratio (short/long because minAreaRect gives rotated rect)
    private static final float TARGET_ASPECT = 5f / 7f; // ~0.714
    private static final float ASPECT_WIGGLE = 0.35f;   // loose at detection stage

    /** Backwards-compatible: full-frame detection */
    public static Quad findBestQuad(Bitmap bitmap) {
        return findBestQuad(bitmap, 0, Integer.MAX_VALUE);
    }

    /** ROI detection: gateTop/gateBottom are full-frame Y coords */
    public static Quad findBestQuad(Bitmap bitmap, int gateTop, int gateBottom) {
        if (bitmap == null) return null;

        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        final int frameW = rgba.cols();
        final int frameH = rgba.rows();

        int top = clamp(gateTop, 0, frameH - 1);
        int bot = clamp(gateBottom, top + 1, frameH);

        Mat roi = rgba.submat(top, bot, 0, frameW);

        // Downscale ROI
        Mat small = new Mat();
        Imgproc.resize(roi, small, new Size(), SCALE, SCALE, Imgproc.INTER_AREA);

        int smallW = small.cols();
        int smallH = small.rows();
        double roiArea = (double) smallW * smallH;

        // gray -> CLAHE -> blur -> morph gradient -> Canny -> close/dilate
        Mat gray = new Mat();
        Imgproc.cvtColor(small, gray, Imgproc.COLOR_RGBA2GRAY);

        Mat claheOut = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(gray, claheOut);

        Imgproc.GaussianBlur(claheOut, claheOut, new Size(5, 5), 0);

        Mat grad = new Mat();
        Mat k3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(claheOut, grad, Imgproc.MORPH_GRADIENT, k3);

        Mat edges = new Mat();
        Imgproc.Canny(grad, edges, 60, 180);

        Mat closed = new Mat();
        Imgproc.morphologyEx(edges, closed, Imgproc.MORPH_CLOSE, k3);
        Imgproc.dilate(closed, closed, k3, new Point(-1, -1), 1);

        // Contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(closed, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        if (contours.isEmpty()) return null;

        // Sort by area descending
        Collections.sort(contours, (a, b) ->
                Double.compare(Imgproc.contourArea(b), Imgproc.contourArea(a))
        );

        Quad best = null;
        double bestScore = -1;

        int inspected = 0;
        for (MatOfPoint c : contours) {
            if (inspected++ >= TOP_N) break;

            double contourArea = Imgproc.contourArea(c);
            if (contourArea < roiArea * MIN_AREA_RATIO) continue;

            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());

            QuadCandidate qc = quadFromApprox(c2f, contourArea, roiArea);
            if (qc == null) {
                qc = quadFromMinAreaRect(c2f, contourArea, roiArea);
                if (qc == null) continue;
            }

            // Score: size + rectangularity + aspect proximity
            double score =
                    (qc.areaNorm * 3.0) +
                            (qc.rectangularity * 2.0) +
                            (qc.aspectScore * 2.0);

            if (score > bestScore) {
                bestScore = score;
                best = qc.toQuadScaledBack(SCALE, top, score);
            }

            // Early exit: huge + good aspect => accept immediately
            if (qc.areaNorm > 0.35 && qc.aspectScore > 0.85) {
                return qc.toQuadScaledBack(SCALE, top, score);
            }
        }

        return best;
    }

    // --------------------- Public Quad type ---------------------

    public static final class Quad {
        public final PointF[] corners;     // 4 corners (full-frame)
        public final Rect boundingRect;    // axis-aligned rect (full-frame)

        // Scoring metadata (for fast accept/reject)
        public final double score;         // combined score
        public final double areaNorm;      // rectArea / roiArea (0..1)
        public final double rectangularity; // contourArea / rectArea (0..1)
        public final float aspectScore;    // 0..1

        private Quad(PointF[] corners, Rect r,
                     double score, double areaNorm, double rectangularity, float aspectScore) {
            this.corners = corners;
            this.boundingRect = r;
            this.score = score;
            this.areaNorm = areaNorm;
            this.rectangularity = rectangularity;
            this.aspectScore = aspectScore;
        }
    }

    // --------------------- Candidate extraction ---------------------

    private static QuadCandidate quadFromApprox(MatOfPoint2f contour, double contourArea, double roiArea) {
        double peri = Imgproc.arcLength(contour, true);
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(contour, approx, 0.02 * peri, true);

        Point[] pts = approx.toArray();
        if (pts.length != 4) return null;

        MatOfPoint approxInt = new MatOfPoint(pts);
        if (!Imgproc.isContourConvex(approxInt)) return null;

        RotatedRect rr = Imgproc.minAreaRect(approx);
        return buildCandidateFromPoints(pts, rr, contourArea, roiArea);
    }

    private static QuadCandidate quadFromMinAreaRect(MatOfPoint2f contour, double contourArea, double roiArea) {
        RotatedRect rr = Imgproc.minAreaRect(contour);
        if (rr.size.width <= 1 || rr.size.height <= 1) return null;

        Point[] box = new Point[4];
        rr.points(box);

        return buildCandidateFromPoints(box, rr, contourArea, roiArea);
    }

    private static QuadCandidate buildCandidateFromPoints(
            Point[] pts, RotatedRect rr, double contourArea, double roiArea
    ) {
        double w = rr.size.width;
        double h = rr.size.height;
        if (w <= 1 || h <= 1) return null;

        double rectArea = Math.abs(w * h);
        if (rectArea <= 1.0) return null;

        double rectangularity = clamp01(contourArea / rectArea);
        double areaNorm = clamp01(rectArea / roiArea);

        // short/long aspect in [0..1]
        float aspect = (float) (Math.min(w, h) / Math.max(w, h));
        float min = TARGET_ASPECT * (1f - ASPECT_WIGGLE);
        float max = TARGET_ASPECT * (1f + ASPECT_WIGGLE);

        float aspectScore;
        if (aspect < min || aspect > max) {
            aspectScore = 0.0f;
        } else {
            float dist = Math.abs(aspect - TARGET_ASPECT) / (TARGET_ASPECT * ASPECT_WIGGLE);
            aspectScore = clamp01(1f - dist);
        }

        return new QuadCandidate(pts, areaNorm, rectangularity, aspectScore);
    }

    private static final class QuadCandidate {
        final Point[] ptsSmall;
        final double areaNorm;
        final double rectangularity;
        final float aspectScore;

        QuadCandidate(Point[] ptsSmall,
                      double areaNorm,
                      double rectangularity,
                      float aspectScore) {
            this.ptsSmall = ptsSmall;
            this.areaNorm = areaNorm;
            this.rectangularity = rectangularity;
            this.aspectScore = aspectScore;
        }

        Quad toQuadScaledBack(double scale, int roiTopFull, double score) {
            PointF[] cs = new PointF[4];
            float inv = (float) (1.0 / scale);

            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

            for (int i = 0; i < 4; i++) {
                float xFull = (float) (ptsSmall[i].x * inv);
                float yFull = (float) (ptsSmall[i].y * inv) + roiTopFull;

                cs[i] = new PointF(xFull, yFull);

                minX = Math.min(minX, xFull);
                minY = Math.min(minY, yFull);
                maxX = Math.max(maxX, xFull);
                maxY = Math.max(maxY, yFull);
            }

            Rect r = new Rect((int) minX, (int) minY, (int) maxX, (int) maxY);

            return new Quad(cs, r, score, areaNorm, rectangularity, aspectScore);
        }
    }

    // --------------------- Utils ---------------------

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float clamp01(double v) {
        return (float) Math.max(0.0, Math.min(1.0, v));
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}