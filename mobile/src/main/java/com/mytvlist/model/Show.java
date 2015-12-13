package com.mytvlist.model;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class Show {

    private String mTitle;

    private String mYear;

    private IDs mIds;

    private ImagesModel mImageModel;

    private String mOverview;

    private String mFirstAired;

    private String mAirsDay;

    private String mAirsTime;

    private String mAirsTimeZone;

    private String mRuntime;

    private String mNetwork;

    private String mCountry;

    private String mUpdatedAt;

    private String mTrailer;

    private String mHomepage;

    private String mStatus;

    private String[] mGenresArray;

    private String mGenres;

    private String mAiredEpisodes;

    private String mImdbRating;

    private String mImdbVotes;

    private String mTraktRating;

    private String mAwards;

    public Show(String title, String year, IDs ids) {
        mTitle = title;
        mYear = year;
        mIds = ids;
    }

    public Show() {

    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public String getYear() {
        return mYear;
    }

    public IDs getIDs() {
        return mIds;
    }

    public void setIDs(IDs ids) {
        mIds = ids;
    }

    public Show getShow() {
        return this;
    }

    public void setImageModel(ImagesModel model) {
        mImageModel = model;
    }

    public ImagesModel getImageModel() {
        return mImageModel;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        this.mOverview = overview;
    }

    public String getFirstAired() {
        return mFirstAired;
    }

    public void setFirstAired(String firstAired) {
        this.mFirstAired = firstAired;
    }

    public String getAirsDay() {
        return mAirsDay;
    }

    public void setAirsDay(String airs) {
        this.mAirsDay = airs;
    }

    public String getAirsTime() {
        return mAirsTime;
    }

    public void setAirsTime(String airsTime) {
        this.mAirsTime = airsTime;
    }

    public String getAirsTimeZone() {
        return mAirsTimeZone;
    }

    public void setAirsTimeZone(String airsTimeZone) {
        this.mAirsTimeZone = airsTimeZone;
    }

    public String getRuntime() {
        return mRuntime;
    }

    public void setRuntime(String runtime) {
        this.mRuntime = runtime;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public void setNetwork(String network) {
        this.mNetwork = network;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        this.mCountry = country;
    }

    public String getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.mUpdatedAt = updatedAt;
    }

    public String getTrailer() {
        return mTrailer;
    }

    public void setTrailer(String trailer) {
        this.mTrailer = trailer;
    }

    public String getHomepage() {
        return mHomepage;
    }

    public void setHomepage(String homepage) {
        this.mHomepage = homepage;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public String[] getGenresArray() {
        return mGenresArray;
    }

    public void setGenresArray(String[] genresArray) {
        this.mGenresArray = genresArray;
    }

    public void setGenres(String genres) {
        mGenres = genres;
    }

    public String getGenres() {
        return mGenres;
    }

    public String getAiredEpisodes() {
        return mAiredEpisodes;
    }

    public void setAiredEpisodes(String airedEpisodes) {
        this.mAiredEpisodes = airedEpisodes;
    }

    public String getIMDBVotes() {
        return mImdbVotes;
    }

    public void setImdbVotes(String votes) {
        mImdbVotes = votes;
    }

    public String getTraktRating() {
        return mTraktRating;
    }

    public void setTraktRating(String rating) {
        mTraktRating = rating;
    }

    public String getIMDBRating() {
        return mImdbRating;
    }

    public void setImdbRating(String rating) {
        mImdbRating = rating;
    }

    public String getAwards() {
        return mAwards;
    }

    public void setAwards(String awards) {
        mAwards = awards;
    }

}
