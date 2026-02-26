package com.example.mtg_java.scanner;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TFLiteImageClassifier implements Closeable {

    public static class Result {
        public final String label;
        public final float prob;
        public Result(String label, float prob) { this.label = label; this.prob = prob; }
    }

    private final List<String> labels;
    private final Interpreter interpreter;
    private final int inputW;
    private final int inputH;
    private final boolean useUint8;

    public TFLiteImageClassifier(
            Context context,
            String modelAssetName,
            String labelsAssetName,
            int inputWidth,
            int inputHeight,
            boolean useUint8Model
    ) throws IOException {
        this.inputW = inputWidth;
        this.inputH = inputHeight;
        this.useUint8 = useUint8Model;

        this.labels = loadLabels(context, labelsAssetName);

        Interpreter.Options opts = new Interpreter.Options();
        opts.setNumThreads(4);
        this.interpreter = new Interpreter(AssetUtils.loadMappedFile(context, modelAssetName), opts);
    }

    private static List<String> loadLabels(Context ctx, String asset) throws IOException {
        List<String> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getAssets().open(asset)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) out.add(line);
            }
        }
        return out;
    }

    public Result classifyTop1(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputW, inputH, true);

        int numClasses = labels.size();

        if (!useUint8) {
            ByteBuffer input = bitmapToFloatInput(resized);
            float[][] output = new float[1][numClasses];
            interpreter.run(input, output);

            int bestIdx = 0;
            float best = output[0][0];
            for (int i = 1; i < numClasses; i++) {
                if (output[0][i] > best) { best = output[0][i]; bestIdx = i; }
            }
            return new Result(labels.get(bestIdx), best);
        } else {
            // If you truly have a uint8 model, implement dequant like scan.zip did.
            throw new UnsupportedOperationException("uint8 path not implemented here");
        }
    }

    private ByteBuffer bitmapToFloatInput(Bitmap bmp) {
        ByteBuffer buf = ByteBuffer.allocateDirect(4 * inputW * inputH * 3);
        buf.order(ByteOrder.nativeOrder());

        int[] pixels = new int[inputW * inputH];
        bmp.getPixels(pixels, 0, inputW, 0, 0, inputW, inputH);

        for (int px : pixels) {
            int r = (px >> 16) & 0xFF;
            int g = (px >> 8) & 0xFF;
            int b = (px) & 0xFF;
            buf.putFloat(r / 255f);
            buf.putFloat(g / 255f);
            buf.putFloat(b / 255f);
        }
        buf.rewind();
        return buf;
    }

    @Override public void close() {
        interpreter.close();
    }
}
