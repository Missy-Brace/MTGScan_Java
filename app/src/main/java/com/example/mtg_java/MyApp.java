package com.example.mtg_java;

import android.app.Application;

import com.example.mtg_java.scanner.TFLiteImageClassifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyApp extends Application {

    private ExecutorService modelExecutor;
    private Future<?> initFuture;

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
            }
        });
    }

    public TFLiteImageClassifier getClassifierBlocking() {
        preloadModelIfNeeded();
        try {
            if (initFuture != null) initFuture.get();
        } catch (Exception ignored) {}
        return classifier;
    }

    public boolean isModelReady() {
        return classifier != null;
    }
}