package com.mytvlist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mytvlist.utils.Utils;

public class TvListDataSource {

    private SQLiteDatabase mDatabase;
    private TVListSqliteOpenHelper mTVListSQLiteOpenHelper;
    private static final String TAG = "TvListDataSource";

    public TvListDataSource(Context context) {
        mTVListSQLiteOpenHelper = new TVListSqliteOpenHelper(
                context);
    }

    public void open() throws SQLException {
        // Log.d(TAG, "open()");
        mDatabase = mTVListSQLiteOpenHelper.getWritableDatabase();
    }

    public void close() {
        // Log.d(TAG, "close()");
        mTVListSQLiteOpenHelper.close();
    }

    public long insertIntoShowTable(String contentType, String contentCategory, String title, String watchers, String year, String tractId, String imdbId, String slug, String tvdb, String tmdb, String tvrage) {
        ContentValues values = new ContentValues();
        values.put(Utils.CONTENT_TYPE, contentType);
        values.put(Utils.CONTENT_CATEGORY, contentCategory);
        values.put(Utils.TITLE, title);
        values.put(Utils.WATCHERS, watchers);
        values.put(Utils.YEAR, year);
        values.put(Utils.TRAKT_ID, tractId);
        values.put(Utils.IMDB_ID, imdbId);
        values.put(Utils.SLUG, slug);
        values.put(Utils.TVDB_ID, tvdb);
        values.put(Utils.TMDB_ID, tmdb);
        values.put(Utils.TVRAGE, tvrage);
        return mDatabase.insert(Utils.TVLIST_SHOW_TABLE, null, values);
    }

    public long insertIntoTable(String tableName, ContentValues values) {
        return mDatabase.insert(tableName, null, values);
    }

    public long deleteFromTable(String tableName, String where, String[] whereArgs) {
        return mDatabase.delete(tableName, where, whereArgs);
    }

    public long updateTable(String tableName, ContentValues contentValues, String where, String[] whereArgs) {
        return mDatabase.update(tableName, contentValues, where, whereArgs);
    }

    public Cursor getContents(String tableName, String[] projection, String where, String[] whereArgs) {
        return mDatabase.query(tableName, projection, where, whereArgs, null, null, null);
    }

}
