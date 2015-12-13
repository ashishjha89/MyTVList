package com.mytvlist.model;

/**
 * Created by ashish on 20/9/15.
 */
public class Person {

    private String mPersonName;

    private IDs mIDs;

    private String mThumbImageUri;

    public void setPersonName(String name) {
        mPersonName = name;
    }

    public String getPersonName() {
        return mPersonName;
    }

    public void setIDs(IDs ids) {
        mIDs = ids;
    }

    public IDs getIDs() {
        return mIDs;
    }

    public void setThumbImageUri(String imageUri) {
        mThumbImageUri = imageUri;
    }

    public String getThumbImageUri() {
        return mThumbImageUri;
    }
}
