package com.example.mtg_java;

import android.app.Application;

import com.example.mtg_java.scanner.TFLiteImageClassifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// FIX 1: Reset initFuture to null on failure so preloadModelIfNeeded() can retry
//         instead of silently skipping because a failed future is still non-null.
// FIX 2: classifier field is volatile; reads/writes are now visibly ordered across
//         threads without needing a full synchronized block on the fast path.
public class MyApp extends Application {

    private ExecutorService modelExecutor;
    private volatile Future<?> initFuture;   // FIX: volatile so nulling is visible

    private volatile TFLiteImageClassifier classifier;

    @Override
    public void onCreate() {
        super.onCreate();
        modelExecutor = Executors.newSingleThreadExecutor();
    }

    public void preloadModelIfNeeded() {
        if (classifier != null) return;
        if (initFuture != null) return;

        initFuture = modelExecutor.submit(() -> {
            try {
                classifier = new TFLiteImageClassifier(
                        this,
                        "model.tflite",
                        "labels.txt",
                        564, 411,
                        false
                );
            } catch (Exception e) {
                e.printStackTrace();
                classifier = null;
                // FIX: null out the future so the next call to preloadModelIfNeeded()
                // is able to retry, rather than seeing a non-null completed future and
                // skipping silently forever.
                initFuture = null;
            }
        });
    }

    public TFLiteImageClassifier getClassifierBlocking() {
        preloadModelIfNeeded();
        try {
            Future<?> f = initFuture;   // local copy to avoid TOCTOU on volatile
            if (f != null) f.get();
        } catch (Exception ignored) {}
        return classifier;
    }

    public boolean isModelReady() {
        return classifier != null;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
        if (modelExecutor != null) {
            modelExecutor.shutdownNow();
            modelExecutor = null;
        }
    }
}
