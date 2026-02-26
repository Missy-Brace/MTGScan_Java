package com.example.mtg_java.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class RectOverlayView extends View {

    private final Paint paintRect = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGate = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCrop = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect rect; // in ANALYSIS coords
    private int frameW, frameH;

    private int gateTop, gateBottom;
    private Rect cropRect; // in ANALYSIS coords

    public RectOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintRect.setStyle(Paint.Style.STROKE);
        paintRect.setStrokeWidth(6f);
        paintRect.setARGB(255, 0, 255, 0);

        paintGate.setStyle(Paint.Style.STROKE);
        paintGate.setStrokeWidth(4f);
        paintGate.setARGB(255, 255, 255, 0);

        paintCrop.setStyle(Paint.Style.STROKE);
        paintCrop.setStrokeWidth(4f);
        paintCrop.setARGB(255, 0, 200, 255);
    }

    public void update(Rect rect, int frameW, int frameH, int gateTop, int gateBottom, Rect cropRect) {
        this.rect = rect;
        this.frameW = frameW;
        this.frameH = frameH;
        this.gateTop = gateTop;
        this.gateBottom = gateBottom;
        this.cropRect = cropRect;
        postInvalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (frameW <= 0 || frameH <= 0) return;

        float sx = getWidth() / (float) frameW;
        float sy = getHeight() / (float) frameH;

        // Gate lines
        canvas.drawLine(0, gateTop * sy, getWidth(), gateTop * sy, paintGate);
        canvas.drawLine(0, gateBottom * sy, getWidth(), gateBottom * sy, paintGate);

        // Crop box
        if (cropRect != null) {
            canvas.drawRect(
                    cropRect.left * sx,
                    cropRect.top * sy,
                    cropRect.right * sx,
                    cropRect.bottom * sy,
                    paintCrop
            );
        }

        // Detected rect
        if (rect != null) {
            canvas.drawRect(
                    rect.left * sx,
                    rect.top * sy,
                    rect.right * sx,
                    rect.bottom * sy,
                    paintRect
            );
        }
    }
}