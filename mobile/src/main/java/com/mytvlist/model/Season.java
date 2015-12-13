package com.mytvlist.model;

import java.util.ArrayList;

/**
 * Created by ashish on 9/8/15.
 */
public class Season {

    private String mNumber;

    private IDs mIDs;

    private String mRating;

    private String mVotes;

    private String mEpisodeCount;

    private String mAiredEpisodesCount;

    private String mOverview;

    private ArrayList<Episode> mEpisodeList;

    private ImagesModel mImageModel;

    public String getSeasonNumber() {
        return mNumber;
    }

    public void setSeasonNumber(String number) {
        mNumber = number;
    }

    public IDs getIDs() {
        return mIDs;
    }

    public void setIDs(IDs ids) {
        mIDs = ids;
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

    public String getEpisodeCount() {
        return mEpisodeCount;
    }

    public void setEpisodeCount(String episodeCount) {
        mEpisodeCount = episodeCount;
    }

    public String getAiredEpisodeCount() {
        return mAiredEpisodesCount;
    }

    public void setAiredEpisodeCount(String airedEpisodeCount) {
        mAiredEpisodesCount = airedEpisodeCount;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public ArrayList<Episode> getEpisodeList() {
        return mEpisodeList;
    }

    public void setEpisodeList(ArrayList<Episode> episodeList) {
        mEpisodeList = episodeList;
    }

    public void setImageModel(ImagesModel model) {
        mImageModel = model;
    }

    public ImagesModel getImageModel() {
        return mImageModel;
    }
}
