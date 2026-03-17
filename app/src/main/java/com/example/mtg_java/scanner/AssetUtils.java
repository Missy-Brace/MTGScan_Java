package com.example.mtg_java.scanner;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public final class AssetUtils {
    private AssetUtils() {}

    public static MappedByteBuffer loadMappedFile(Context context, String assetName) throws IOException {
        try (AssetFileDescriptor afd = context.getAssets().openFd(assetName);
             FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
             FileChannel channel = fis.getChannel()) {

            long startOffset = afd.getStartOffset();
            long declaredLength = afd.getDeclaredLength();
            return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }
}