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
        AssetFileDescriptor afd = context.getAssets().openFd(assetName);
        try {
            FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
            try {
                FileChannel channel = fis.getChannel();
                try {
                    long startOffset = afd.getStartOffset();
                    long declaredLength = afd.getDeclaredLength();
                    return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
                } finally {
                    channel.close();
                }
            } finally {
                fis.close();
            }
        } finally {
            afd.close();
        }
    }
}