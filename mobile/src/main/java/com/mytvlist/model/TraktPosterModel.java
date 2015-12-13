package com.mytvlist.model;

import android.graphics.Bitmap;

/**
 * Created by ashish on 23/7/15.
 */
public class TraktPosterModel {

    private Bitmap mBitmap;

    private String mTraktId;

    public TraktPosterModel(Bitmap bitmap, String traktId) {
        mBitmap = bitmap;
        mTraktId = traktId;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public String getTraktId() {
        return mTraktId;
    }
}
