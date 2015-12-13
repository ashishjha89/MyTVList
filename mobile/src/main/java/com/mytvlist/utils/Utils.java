package com.mytvlist.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class Utils {

    public static final String TITLE = "title";

    public static final String YEAR = "year";

    public static final String IDS = "ids";

    public static final String TRAKT_ID = "trakt";

    public static final String SLUG = "slug";

    public static final String TVDB_ID = "tvdb";

    public static final String IMDB_ID = "imdb";

    public static final String TMDB_ID = "tmdb";

    public static final String TVRAGE = "tvrage";

    public static final String WATCHERS = "watchers";

    public static final String SHOW = "show";

    public static final String DATABASE_NAME = "mytv_db";

    public static final int DATABASE_VERSION = 1;

    public static final String TVLIST_SHOW_TABLE = "tvlist_show_table";

    public static final String TVLIST_SEASON_TABLE = "tvlist_season_table";

    public static final String TVLIST_EPISODE_TABLE = "tvlist_episode_table";

    public static final String CONTENT_TYPE = "content_type";

    public static final String CONTENT_CATEGORY = "content_category";

    public static final String CATEGORY_TRENDING = "category_trending";

    public static final String CATEGORY_POPULAR = "category_popular";

    public static final String CATEGORY_FAVORITE = "category_favorite";

    public static final String POSTER_THUMB_URI = "poster_thumb_image_uri";

    public static final String FANART_THUMB_URI = "fanart_thumb_uri";

    public static final String BANNER_THUMB_URI = "banner_thumb_uri";

    public static final String FULL_IMAGE_URI = "full_image_uri";

    public static final String THUMB_BLOB = "thumb_blob";

    public static final String COLUMN_ID = "_id";

    public static final String FANART = "fanart";

    public static final String POSTER = "poster";

    public static final String LOGO = "logo";

    public static final String CLEARART = "clearart";

    public static final String BANNER = "banner";

    public static final String THUMB = "thumb";

    public static final String FULL = "full";

    public static final String MEDIUM = "medium";

    public static final String IMAGES = "images";

    public static final String OVERVIEW = "overview";

    public static final String FIRST_AIRED = "first_aired";

    public static final String AIRS = "airs";

    public static final String AIRS_DAY = "day";

    public static final String AIRS_TIME = "time";

    public static final String AIRS_TIMEZONE = "timezone";

    public static final String RUNTINME = "runtime";

    public static final String NETWORK = "network";

    public static final String COUNTRY = "country";

    public static final String UPDATED_AT = "updated_at";

    public static final String TRAILER = "trailer";

    public static final String HOMEPAGE = "homepage";

    public static final String STATUS = "status";

    public static final String STATUS_RETURNING_SERIES = "returning series";

    public static final String STATUS_IN_PRODUCTION = "in production";

    public static final String STATUS_CANCELED = "canceled";

    public static final String STATUS_ENDED = "ended";

    public static final String GENRES = "genres";

    public static final String AIRED_EPISODES = "aired_episodes";

    public static final String MY_TV_LIST_PREFS = "my_tv_list_prefs";

    public static final String IS_CLEAN_LAUNCH = "is_clean_launch";

    public static final String IS_CALLED_FROM_APP = "is_called_from_app";

    public static final String IS_CALLED_FROM_SPLASH = "is_called_from_splash";

    public static final String LOAD_INTERESTING_SHOWS = "load_interesting_shows";

    public static final String UPDATE_SHOWS = "update_shows";

    public static final String LOAD_SHOW_IMAGES = "load_show_images";

    public static final String LOAD_SEASON_DETAILS = "load_season_details";

    public static final String LOAD_IMDB_DETAILS = "load_imdb_details";

    public static final String LOAD_SINGLE_EPISODE = "load_single_episode";

    public static final String LOAD_SEASON_EPISODES = "load_season_episodes";

    public static final String SHOW_TRAKT_ID = "show_trakt_id";

    public static final String SEASON_TRAKT_ID = "season_trakt_id";

    public static final String NUMBER = "number";

    public static final String EPISODE_NUMBER = "episode_number";

    public static final String RATING = "rating";

    public static final String VOTES = "votes";

    public static final String EPISODE_COUNT = "episode_count";

    public static final String SEASON = "season";

    public static final String SCREENSHOT = "screenshot";

    public static final String EPISODES = "episodes";

    public static final String TYPE = "type";

    public static final String CHARACTER = "character";

    public static final String PERSON = "person";

    public static final String NAME = "name";

    public static final String HEADSHOT = "headshot";

    public static final String CAST = "cast";

    public static final int ANIMATION_DURATION = 1100;

    public static final String IMDB_RATING = "imdbRating";

    public static final String TRAKT_RATING = "rating";

    public static final String IMDB_VOTES = "imdbVotes";

    public static final String AWARDS = "Awards";

    public static final String JANUARY = "Jan";

    public static final String FEBRUARY = "Feb";

    public static final String MARCH = "Mar";

    public static final String APRIL = "Apr";

    public static final String MAY = "May";

    public static final String JUNE = "June";

    public static final String JULY = "July";

    public static final String AUGUST = "Aug";

    public static final String SEPTEMBER = "Sept";

    public static final String OCTOBER = "Oct";

    public static final String NOVEMBER = "Nov";

    public static final String DECEMBER = "Dec";

    public static final String IS_LOADED = "false";

    public static final String ADDED = "added";

    public static final long THRESHOLD_TIME_BETWEEN_REFRESH = (7 * 24 * 60 * 60 * 1000);// / (24 * 60 * 60 * 1000); // 7 days - expressed in milliseconds

    public static final String LAST_UPDATE_TIME = "last_update_time";

    public static final int MAX_SHOW_LIMIT = 20;

    public enum CONTENT_TYPE_ENUM {
        POPULAR, TRENDING, SINGLE_SHOW_SUMMARY, IMAGE_POSTER_THUMB, IMAGE_FANART_THUMB, SEARCH_SHOW;
    }

    public static final String ADD_SHOW_SCREEN = "Add Show";

    public static final String MY_SHOWS_SCREEN = "My Shows";

    public static final String SHOW_DETAIL_SCREEN = "Show Detail";

    public static final String CAST_LIST_SCREEN = "Cast List";

    public static final String EPISODE_DETAIL_SCREEN = "Episode Detail";

    public static final String SHOW_DELETE_EVENT = "Show Deleted";

    public static final String LAUNCH_YOUTUBE_EVENT = "Launch YouTube Event";

    public static final String LAUNCH_IMDB_EVENT = "Launch Imdb Event";

    public static final String LAUNCH_TRAKT_EVENT = "Launch Trakt Event";

    public static final String SEARCH_SHOW_EVENT = "Search Show Event";

    public static final String ADD_SHOW_EVENT = "Add Show Event";

    public static final String SHOWS_COUNT = "Shows Count";

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        if (!(width > 0 && height > 0)) {
            return null;
        }
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_XHIGH));
        // Log.d("ashish","################ dpToPx dp="+dp+" px="+px + " displayMetrics.xdpi="+(displayMetrics.xdpi) + "DisplayMetrics.DENSITY_DEFAULT=" + (DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
