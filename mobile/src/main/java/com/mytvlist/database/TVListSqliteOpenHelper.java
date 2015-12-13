package com.mytvlist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mytvlist.utils.Utils;

/**
 * Created by ashish.jha on 7/5/2015.
 */
public class TVListSqliteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "TVListSqliteOpenHelper";

    private static final String CATEGORY_SHOW_TABLE_CREATE = "create table "
            + Utils.TVLIST_SHOW_TABLE + "("
            + Utils.COLUMN_ID + " integer primary key autoincrement, "
            + Utils.CONTENT_TYPE + " text not null, "
            + Utils.CONTENT_CATEGORY + " text not null, "
            + Utils.TITLE + " text not null, "
            + Utils.WATCHERS + " text, "
            + Utils.YEAR + " text, "
            + Utils.TRAKT_ID + " text not null, "
            + Utils.IMDB_ID + " text, "
            + Utils.SLUG + " text not null, "
            + Utils.TVDB_ID + " text, "
            + Utils.TMDB_ID + " text, "
            + Utils.TVRAGE + " text, "
            + Utils.IMDB_RATING + " text, "
            + Utils.TRAKT_RATING + " text, "
            + Utils.IMDB_VOTES + " text, "
            + Utils.AWARDS + " text, "
            + Utils.FULL_IMAGE_URI + " text, "
            + Utils.OVERVIEW + " text, "
            + Utils.FIRST_AIRED + " text, "
            + Utils.AIRS_DAY + " text, "
            + Utils.AIRS_TIME + " text, "
            + Utils.AIRS_TIMEZONE + " text, "
            + Utils.RUNTINME + " text, "
            + Utils.NETWORK + " text, "
            + Utils.COUNTRY + " text, "
            + Utils.UPDATED_AT + " text, "
            + Utils.TRAILER + " text, "
            + Utils.HOMEPAGE + " text, "
            + Utils.STATUS + " text, "
            + Utils.GENRES + " text, "
            + Utils.AIRED_EPISODES + " text, "
            + Utils.POSTER_THUMB_URI + " text, " // My Show Screen card image
            + Utils.FANART_THUMB_URI + " text, " // My Show Screen card image
            + Utils.BANNER_THUMB_URI + " text, " // My Show Screen card image
            + Utils.POSTER + " text, " // Show Detail Page Background
            + Utils.FANART + " text, " // Show Detail Top Image
            + Utils.IS_LOADED + " text "
            + ");";

    private static final String CATEGORY_SEASON_TABLE_CREATE = "create table "
            + Utils.TVLIST_SEASON_TABLE + "("
            + Utils.COLUMN_ID + " integer primary key autoincrement, "
            + Utils.SHOW_TRAKT_ID + " text not null, "
            + Utils.NUMBER + " text not null, " // SEASON NUMBER
            + Utils.TRAKT_ID + " text not null, "
            + Utils.IMDB_ID + " text, "
            + Utils.TVDB_ID + " text, "
            + Utils.TMDB_ID + " text, "
            + Utils.TVRAGE + " text, "
            + Utils.OVERVIEW + " text, "
            + Utils.RATING + " text, "
            + Utils.VOTES + " text, "
            + Utils.EPISODE_COUNT + " text, "
            + Utils.AIRED_EPISODES + " text, "
            + Utils.FULL_IMAGE_URI + " text, "
            + Utils.POSTER_THUMB_URI + " text, "
            + Utils.POSTER + " text"
            + ");";

    private static final String CATEGORY_EPISODE_TABLE_CREATE = "create table "
            + Utils.TVLIST_EPISODE_TABLE + "("
            + Utils.COLUMN_ID + " integer primary key autoincrement, "
            + Utils.SHOW_TRAKT_ID + " text not null, "
            + Utils.SEASON_TRAKT_ID + " text not null, "
            + Utils.SEASON + " text not null, " // SEASON NUMBER
            + Utils.NUMBER + " text not null, " // EPISODE NUMBER
            + Utils.TITLE + " text not null, "
            + Utils.TRAKT_ID + " text not null, "
            + Utils.IMDB_ID + " text, "
            + Utils.TVDB_ID + " text, "
            + Utils.TMDB_ID + " text, "
            + Utils.TVRAGE + " text, "
            + Utils.OVERVIEW + " text, "
            + Utils.RATING + " text, "
            + Utils.VOTES + " text, "
            + Utils.FULL_IMAGE_URI + " text, "
            + Utils.POSTER_THUMB_URI + " text, "
            + Utils.POSTER + " text, "
            + Utils.FIRST_AIRED + " text, "
            + Utils.UPDATED_AT + " text"
            + ");";

    public TVListSqliteOpenHelper(Context context) {
        super(context, Utils.DATABASE_NAME, null,
                Utils.DATABASE_VERSION);
        // Log.d(TAG, "TVListSqliteOpenHelper Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CATEGORY_SHOW_TABLE_CREATE);
        sqLiteDatabase.execSQL(CATEGORY_SEASON_TABLE_CREATE);
        sqLiteDatabase.execSQL(CATEGORY_EPISODE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CATEGORY_SHOW_TABLE_CREATE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CATEGORY_SEASON_TABLE_CREATE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CATEGORY_EPISODE_TABLE_CREATE);
        onCreate(sqLiteDatabase);
    }
}
