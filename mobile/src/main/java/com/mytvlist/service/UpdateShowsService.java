package com.mytvlist.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.mytvlist.database.TvListDataSource;
import com.mytvlist.json.SeasonParser;
import com.mytvlist.json.ShowParser;
import com.mytvlist.loaders.ContentLaoder;
import com.mytvlist.model.Episode;
import com.mytvlist.model.Season;
import com.mytvlist.model.Show;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish on 16/9/15.
 */
public class UpdateShowsService extends IntentService {

    private ArrayList<String> mTraktIdList;

    private ArrayList<String> mLastUpdateTime;

    private static final String TAG = "UpdateShowsService";

    public UpdateShowsService() {
        super("UpdateShowsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(Utils.UPDATE_SHOWS)) {
            if (!Utils.isNetworkAvailable(this)) {
                return;
            }
            getShowsFromDB();
            for (int i = 0; i < mTraktIdList.size(); i++) {
                updateSeasonDetails(i);
            }
            updateSharedPref();
        }

    }

    private void getShowsFromDB() {
        mTraktIdList = new ArrayList<>();
        mLastUpdateTime = new ArrayList<>();
        TvListDataSource tvListDataSource = new TvListDataSource(getBaseContext());
        String[] projection = {Utils.TRAKT_ID, Utils.UPDATED_AT, Utils.AIRED_EPISODES};
        Cursor cursor = null;
        try {
            String where = Utils.CONTENT_TYPE + "=? AND " + Utils.CONTENT_CATEGORY + "=?";
            String[] whereArgs = {Utils.SHOW, Utils.CATEGORY_FAVORITE};
            tvListDataSource.open();
            cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
                Log.d(TAG, "getShowsFromDB() cursor == null ? " + (cursor == null));
                if (cursor != null) {
                    cursor.close();
                }
                tvListDataSource.close();
                return;
            }
            tvListDataSource.close();
            cursor.moveToFirst();
            String traktId, lastUpdatedTime;
            do {
                traktId = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TRAKT_ID));
                lastUpdatedTime = cursor.getString(cursor.getColumnIndexOrThrow(Utils.UPDATED_AT));
                mTraktIdList.add(traktId);
                mLastUpdateTime.add(lastUpdatedTime);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            Log.d(TAG, "getPopularShowsFromDB() EXCEPTION in query " + (e.getMessage()));
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateSeasonDetails(int index) {
        String showTraktId = mTraktIdList.get(index);
        String lastUpdatedTime = mLastUpdateTime.get(index);
        String seasonDetailUrl = "https://api-v2launch.trakt.tv/shows/" + showTraktId + "/seasons?extended=full,images,episodes";
        String seasonResult = ContentLaoder.getContent(seasonDetailUrl);
        try {
            JSONArray seasonJSONArray = new JSONArray(seasonResult);
            SeasonParser seasonParser = new SeasonParser();
            ArrayList<Season> seasonList = seasonParser.getSeasonShowList(seasonJSONArray);
            if (isUpdateNeeded(showTraktId, lastUpdatedTime)) {
                updateSeasonDetailsInDB(showTraktId, seasonList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    private boolean isUpdateNeeded(String traktId, String prevLastUpdatedTime) {
        String traktUrl = "https://api-v2launch.trakt.tv/shows/" + traktId;
        String result = ContentLaoder.getContent(traktUrl);
        try {
            JSONObject showJson = new JSONObject(result);
            Show show = new ShowParser().getShow(showJson);
            if (prevLastUpdatedTime.equals(show.getUpdatedAt())) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void updateSeasonDetailsInDB(String showTraktId, ArrayList<Season> seasonList) {
        ContentValues values;
        ArrayList<ContentValues> episodeValues;
        String seasonTraktId;
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        //String where = Utils.TRAKT_ID + "=?";
        //String[] whereArgs = {showTraktId};
        String where = Utils.SHOW_TRAKT_ID + "=?";
        String[] whereArgs = {showTraktId};
        ArrayList<Episode> episodeList;
        tvListDataSource.deleteFromTable(Utils.TVLIST_SEASON_TABLE, where, whereArgs);
        tvListDataSource.deleteFromTable(Utils.TVLIST_EPISODE_TABLE, where, whereArgs);
        for (Season season : seasonList) {
            values = new ContentValues();
            values.put(Utils.SHOW_TRAKT_ID, showTraktId);
            values.put(Utils.NUMBER, season.getSeasonNumber());
            seasonTraktId = season.getIDs().getTracktId();
            values.put(Utils.TRAKT_ID, seasonTraktId);
            values.put(Utils.IMDB_ID, season.getIDs().getImdb());
            values.put(Utils.TVDB_ID, season.getIDs().getTvdb());
            values.put(Utils.TMDB_ID, season.getIDs().getTmDb());
            values.put(Utils.TVRAGE, season.getIDs().getTvrage());
            values.put(Utils.EPISODE_COUNT, season.getEpisodeCount());
            values.put(Utils.AIRED_EPISODES, season.getAiredEpisodeCount());
            //tvListDataSource.updateTable(Utils.TVLIST_SEASON_TABLE, values, where, whereArgs);
            tvListDataSource.insertIntoTable(Utils.TVLIST_SEASON_TABLE, values);
            episodeList = season.getEpisodeList();
            episodeValues = updateEpisodeDetailsInDB(showTraktId, seasonTraktId, episodeList);
            if (episodeValues == null) {
                return;
            }
            for (ContentValues episode : episodeValues) {
                //tvListDataSource.updateTable(Utils.TVLIST_EPISODE_TABLE, episode, where, whereArgs);
                tvListDataSource.insertIntoTable(Utils.TVLIST_EPISODE_TABLE, episode);
            }
        }
        tvListDataSource.close();
    }

    private ArrayList<ContentValues> updateEpisodeDetailsInDB(String showTraktId, String seasonTraktId, ArrayList<Episode> episodeList) {
        ArrayList<ContentValues> contentValueList = new ArrayList<>();
        if (episodeList == null) {
            return null;
        }
        ContentValues values;
        for (Episode episode : episodeList) {
            values = new ContentValues();
            values.put(Utils.SHOW_TRAKT_ID, showTraktId);
            values.put(Utils.SEASON_TRAKT_ID, seasonTraktId);
            values.put(Utils.SEASON, episode.getSeasonNumber());
            values.put(Utils.NUMBER, episode.getEpisodeNumber());
            values.put(Utils.TITLE, episode.getTitle());
            values.put(Utils.TRAKT_ID, episode.getIDs().getTracktId());
            values.put(Utils.IMDB_ID, episode.getIDs().getImdb());
            values.put(Utils.TVDB_ID, episode.getIDs().getTvdb());
            values.put(Utils.TMDB_ID, episode.getIDs().getTmDb());
            values.put(Utils.TVRAGE, episode.getIDs().getTvrage());
            values.put(Utils.OVERVIEW, episode.getOverview());
            values.put(Utils.POSTER, episode.getThumbImageUrl());
            values.put(Utils.RATING, episode.getRating());
            values.put(Utils.VOTES, episode.getVoteCount());
            values.put(Utils.FIRST_AIRED, episode.getFirstAiredTime());
            contentValueList.add(values);
        }
        return contentValueList;
    }

    private void updateSharedPref() {
        SharedPreferences.Editor editor = getSharedPreferences(Utils.MY_TV_LIST_PREFS, MODE_PRIVATE).edit();
        editor.putLong(Utils.LAST_UPDATE_TIME, System.currentTimeMillis());
        editor.apply();
    }
}
