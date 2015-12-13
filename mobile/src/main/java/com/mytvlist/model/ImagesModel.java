package com.mytvlist.model;

/**
 * Created by ashish.jha on 7/10/2015.
 */
public class ImagesModel {

    /*
    * Banner:Full -> To display backgroung in "Add Show" Screen
    * Poster:Thumb -> To display as part of cards in "My Shows" Screen
    *              -> To display as background in "Show Detail" screen
    * Fanarts:Thumb -> To display in Show Detail Top View
    * */

    public class Fanart extends ImageCategory {

    }

    public class Poster extends ImageCategory {

    }

    public class Logo extends ImageCategory {

    }

    public class Clearart extends ImageCategory {

    }

    public class Banner extends ImageCategory {

    }

    public class Thumb extends ImageCategory {

    }

    private ImageCategory mFanart;

    private ImageCategory mPoster;

    private ImageCategory mLogo;

    private ImageCategory mClearart;

    private ImageCategory mBanner;

    private ImageCategory mThumb;

    public ImageCategory getFanartImageCategory() {
        return mFanart;
    }

    public void setFanartImageCategory(ImageCategory fanart) {
        mFanart = fanart;
    }

    public ImageCategory getPosterImageCategory() {
        return mPoster;
    }

    public void setPosterImageCategory(ImageCategory poster) {
        mPoster = poster;
    }

    public ImageCategory getClearartImageCategory() {
        return mClearart;
    }

    public void setClearartImageCategory(ImageCategory clearart) {
        mClearart = clearart;
    }

    public ImageCategory getBannerImageCategory() {
        return mBanner;
    }

    public void setBannerImageCategory(ImageCategory banner) {
        mBanner = banner;
    }

    public ImageCategory getLogoImageCategory() {
        return mLogo;
    }

    public void setLogoImageCategory(ImageCategory logo) {
        mLogo = logo;
    }

    public ImageCategory getThumbImageCategory() {
        return mThumb;
    }

    public void setThumbImageCategory(ImageCategory thumb) {
        mThumb = thumb;
    }

}
