package com.example.mtg_java.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class LiveDebugOverlay extends View {

    private Rect rect;
    private Rect cropRect;
    private int gateTop;
    private int gateBottom;
    private int frameW;
    private int frameH;

    private final Paint rectPaint = new Paint();
    private final Paint gatePaint = new Paint();
    private final Paint cropPaint = new Paint();

    public LiveDebugOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(6f);
        rectPaint.setAntiAlias(true);

        gatePaint.setColor(Color.YELLOW);
        gatePaint.setStyle(Paint.Style.STROKE);
        gatePaint.setStrokeWidth(4f);

        cropPaint.setColor(Color.RED);
        cropPaint.setStyle(Paint.Style.STROKE);
        cropPaint.setStrokeWidth(4f);
    }

    /**
     * Called from ScanFragment every tick
     */
    public void update(Rect detectedRect,
                       int frameWidth,
                       int frameHeight,
                       int gateTop,
                       int gateBottom,
                       Rect cropRect) {

        this.rect = detectedRect;
        this.frameW = frameWidth;
        this.frameH = frameHeight;
        this.gateTop = gateTop;
        this.gateBottom = gateBottom;
        this.cropRect = cropRect;

        invalidate(); // 🔥 force redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (frameW <= 0 || frameH <= 0) return;

        float scaleX = getWidth() / (float) frameW;
        float scaleY = getHeight() / (float) frameH;

        // --- Draw detected rectangle ---
        if (rect != null) {
            canvas.drawRect(
                    rect.left * scaleX,
                    rect.top * scaleY,
                    rect.right * scaleX,
                    rect.bottom * scaleY,
                    rectPaint
            );
        }

        // --- Draw gate lines ---
        canvas.drawLine(
                0,
                gateTop * scaleY,
                getWidth(),
                gateTop * scaleY,
                gatePaint
        );

        canvas.drawLine(
                0,
                gateBottom * scaleY,
                getWidth(),
                gateBottom * scaleY,
                gatePaint
        );

        // --- Draw crop rectangle ---
        if (cropRect != null) {
            canvas.drawRect(
                    cropRect.left * scaleX,
                    cropRect.top * scaleY,
                    cropRect.right * scaleX,
                    cropRect.bottom * scaleY,
                    cropPaint
            );
        }
    }
}