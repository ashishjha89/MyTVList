package com.mytvlist.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by ashish.jha on 24/07/2015.
 */
public class ImageCaches {

    private static LruCache<String, Bitmap> POSTER_MEMORY_CACHE;

    private static LruCache<String, Bitmap> BANNER_MEMORY_CACHE;

    private static LruCache<String, Bitmap> ADD_SHOW_MEMORY_CACHE;

    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int showCacheSize = maxMemory / 8;
        final int addShowCacheSize = maxMemory / 16;
        POSTER_MEMORY_CACHE = new LruCache<String, Bitmap>(showCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        BANNER_MEMORY_CACHE = new LruCache<String, Bitmap>(showCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        ADD_SHOW_MEMORY_CACHE = new LruCache<String, Bitmap>(addShowCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static void addPosterBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getPosterBitmapFromMemCache(key) == null && bitmap != null) {
            POSTER_MEMORY_CACHE.put(key, bitmap);
        }
    }

    public static Bitmap getPosterBitmapFromMemCache(String key) {
        return POSTER_MEMORY_CACHE.get(key);
    }

    public static void addBannerBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBannerBitmapFromMemCache(key) == null && bitmap != null) {
            BANNER_MEMORY_CACHE.put(key, bitmap);
        }
    }

    public static Bitmap getBannerBitmapFromMemCache(String key) {
        return BANNER_MEMORY_CACHE.get(key);
    }

    public static void addNewShowBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBannerBitmapFromMemCache(key) == null && bitmap != null) {
            ADD_SHOW_MEMORY_CACHE.put(key, bitmap);
        }
    }

    public static Bitmap getNewShowBitmapFromMemCache(String key) {
        return ADD_SHOW_MEMORY_CACHE.get(key);
    }


}
