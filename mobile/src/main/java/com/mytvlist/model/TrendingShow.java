package com.mytvlist.model;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class TrendingShow extends Show{

    private String mWatchers;

    public TrendingShow(String watchers, String title, String year, IDs ids) {
        super(title, year, ids);
        mWatchers = watchers;
    }

    public String getWatchers() {
        return mWatchers;
    }

    public void setWatchers(String watchers) {
        this.mWatchers = watchers;
    }
}
