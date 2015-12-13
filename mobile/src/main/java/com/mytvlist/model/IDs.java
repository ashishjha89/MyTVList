package com.mytvlist.model;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class IDs {

    private String mTracktId;
    private String mSlug;
    private String mTvdb;
    private String mImdb;
    private String mTmdb;
    private String mTvrage;

    public IDs(String tracktId, String slug, String tvdb, String imdb, String tmdb, String tvrage) {
        mTracktId = tracktId;
        mSlug = slug;
        mTvdb = tvdb;
        mImdb = imdb;
        mTmdb = tmdb;
        mTvrage = tvrage;
    }

    public IDs(){

    }

    public String getTracktId() {
        return mTracktId;
    }

    public void setTracktId(String tracktId) {
        this.mTracktId = tracktId;
    }

    public String getSlug() {
        return mSlug;
    }

    public void setSlug(String slug) {
        this.mSlug = slug;
    }

    public String getTvdb() {
        return mTvdb;
    }

    public void setTvdb(String tvdb) {
        this.mTvdb = tvdb;
    }

    public String getImdb() {
        return mImdb;
    }

    public void setImdb(String imdb) {
        this.mImdb = imdb;
    }

    public String getTmDb() {
        return mTmdb;
    }

    public void setTmDb(String tmdb) {
        mTmdb = tmdb;
    }

    public String getTvrage() {
        return mTvrage;
    }

    public void setTvrage(String tvrage) {
        this.mTvrage = tvrage;
    }
}
