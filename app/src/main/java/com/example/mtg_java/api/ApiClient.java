package com.example.mtg_java.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    private static volatile Retrofit retrofit = null;
    private static volatile OkHttpClient okHttpClient = null;
    private static final String BASE_URL = "https://mtgscan1.onrender.com/";

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            synchronized (ApiClient.class) {
                if (okHttpClient == null) {

                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(15, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return okHttpClient;
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(getOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
