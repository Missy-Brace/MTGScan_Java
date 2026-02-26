package com.example.mtg_java.scanner;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public final class ImageProxyUtils {
    private ImageProxyUtils() {}

    public static Bitmap toRgbaBitmapUpright(ImageProxy image) {
        Bitmap bmp = toRgbaBitmap(image);

        int rot = image.getImageInfo().getRotationDegrees();
        if (rot == 0) return bmp;

        Matrix m = new Matrix();
        m.postRotate(rot);

        Bitmap rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        bmp.recycle();
        return rotated;
    }

    private static Bitmap toRgbaBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        buffer.rewind();

        int width = image.getWidth();
        int height = image.getHeight();

        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride(); // should be 4 for RGBA_8888

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        if (pixelStride == 4 && rowStride == width * 4) {
            bitmap.copyPixelsFromBuffer(buffer);
            return bitmap;
        }

        byte[] rgba = new byte[width * height * 4];
        int dstOffset = 0;

        byte[] row = new byte[rowStride];
        for (int y = 0; y < height; y++) {
            buffer.get(row, 0, rowStride);
            System.arraycopy(row, 0, rgba, dstOffset, width * 4);
            dstOffset += width * 4;
        }

        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return bitmap;
    }
}