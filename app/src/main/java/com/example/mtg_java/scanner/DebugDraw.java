package com.example.mtg_java.scanner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public final class DebugDraw {
    private DebugDraw() {}

    public static Bitmap drawDebug(Bitmap src, Rect rect, int gateTop, int gateBottom,
                                   Rect cropRect) {
        Bitmap out = src.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(out);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);

        // Rectangle found by OpenCV
        if (rect != null) {
            p.setStrokeWidth(6f);
            p.setARGB(255, 0, 255, 0); // green
            c.drawRect(rect, p);
        }

        // Gate lines
        p.setStrokeWidth(4f);
        p.setARGB(255, 255, 255, 0); // yellow
        c.drawLine(0, gateTop, out.getWidth(), gateTop, p);
        c.drawLine(0, gateBottom, out.getWidth(), gateBottom, p);

        // Crop box (what you actually feed the model)
        if (cropRect != null) {
            p.setStrokeWidth(4f);
            p.setARGB(255, 0, 200, 255); // cyan
            c.drawRect(cropRect, p);
        }

        return out;
    }
}