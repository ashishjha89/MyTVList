package com.mytvlist.model;

/**
 * Created by ashish.jha on 7/10/2015.
 */
public class ImageCategory {

    private String mFull, mMedium, mThumb;

    /*public ImageCategory(String full, String medium, String thumb) {
        mFull = full;
        mMedium = medium;
        mThumb = thumb;
    }*/

    public String getFullImage() {
        return mFull;
    }

    public void setFullImages(String full) {
        mFull = full;
    }


    public String getMediumImage() {
        return mMedium;
    }

    public void setMediumImages(String medium) {
        mMedium = medium;
    }


    public String getThumbImage() {
        return mThumb;
    }

    public void setThumbImages(String thumb) {
        mThumb = thumb;
    }

}
