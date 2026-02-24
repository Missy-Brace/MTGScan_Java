package com.example.mtg_java;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static File getFile(Context context, Uri uri) throws IOException {

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("upload", ".jpg", context.getCacheDir());
        tempFile.deleteOnExit();

        FileOutputStream out = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        out.flush();
        out.close();
        inputStream.close();

        return tempFile;
    }
}