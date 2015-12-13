package com.mytvlist.model;

/**
 * Created by ashish on 9/8/15.
 */
public class Episode {

    private String mNumber;

    private String mSeasonNumber;

    private IDs mIDs;

    private String mTitle;

    private String mRating;

    private String mVotes;

    private String mOverview;

    private String mFirstAired;

    private String mLastUpdated;

    private String mThumbImageUrl;

    private String mShowTraktId;

    private String mSeasonTraktId;

    public String getEpisodeNumber() {
        return mNumber;
    }

    public void setEpisodeNumber(String number) {
        mNumber = number;
    }


    public String getSeasonNumber() {
        return mSeasonNumber;
    }

    public void setSeasonNumber(String number) {
        mSeasonNumber = number;
    }

    public IDs getIDs() {
        return mIDs;
    }

    public void setIDs(IDs ids) {
        mIDs = ids;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getRating() {
        return mRating;
    }

    public void setRating(String rating) {
        mRating = rating;
    }

    public String getVoteCount() {
        return mVotes;
    }

    public void setVoteCount(String votes) {
        mVotes = votes;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public String getFirstAiredTime() {
        return mFirstAired;
    }

    public void setFirstAiredTime(String firstAired) {
        mFirstAired = firstAired;
    }

    public String getLastUpdatedTime() {
        return mLastUpdated;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        mLastUpdated = lastUpdatedTime;
    }

    public String getThumbImageUrl() {
        return mThumbImageUrl;
    }

    public void setThumbImageUrl(String thumbImageUrl) {
        mThumbImageUrl = thumbImageUrl;
    }

    public String getShowTraktId() {
        return mShowTraktId;
    }

    public void setShowTraktId(String showId) {
        mShowTraktId = showId;
    }

    public String getSeasonTraktId() {
        return mSeasonTraktId;
    }

    public void setSeasonTraktId(String seasonId) {
        mSeasonTraktId = seasonId;
    }

}
