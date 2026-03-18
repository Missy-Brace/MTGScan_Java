package com.example.mtg_java;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public final class MtgGlideModule extends AppGlideModule {

    private static final int DISK_CACHE_SIZE_BYTES = 250 * 1024 * 1024;


    private static final int MEMORY_CACHE_SIZE_BYTES = 32 * 1024 * 1024;

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder

            .setDefaultRequestOptions(
                new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .format(DecodeFormat.PREFER_RGB_565) // halves memory vs ARGB_8888 for card images
            )
            .setDiskCache(new InternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE_BYTES))
            .setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE_BYTES));
    }


    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
