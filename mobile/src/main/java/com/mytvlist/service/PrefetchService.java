package com.mytvlist.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.mytvlist.R;
import com.mytvlist.activity.AddShowActivity;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.json.PopularShowsParser;
import com.mytvlist.json.SeasonParser;
import com.mytvlist.json.TrendingShowsParser;
import com.mytvlist.loaders.ContentLaoder;
import com.mytvlist.model.Episode;
import com.mytvlist.model.Season;
import com.mytvlist.model.Show;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ashish.jha on 24/07/2015.
 */
public class PrefetchService extends IntentService {

    private boolean mIsNetworkAvailable = false;

    private static final String TAG = "PrefetchService";

    public PrefetchService() {
        super("PrefetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIsNetworkAvailable = Utils.isNetworkAvailable(this);
        if (intent.getAction().equals(Utils.LOAD_INTERESTING_SHOWS)) {
            Log.d(TAG, "onHandleIntent() LOAD_INTERESTING_SHOWS");
            prefetchInterestingShows();
        } else if (intent.getAction().equals((Utils.LOAD_SHOW_IMAGES))) {
            Log.d(TAG, "onHandleIntent() LOAD_SHOW_IMAGES");
            String traktId = intent.getStringExtra(Utils.TRAKT_ID);
            String posterImageUrl = intent.getStringExtra(Utils.POSTER_THUMB_URI);
            String fanartImageUrl = intent.getStringExtra(Utils.FANART_THUMB_URI);
            String bannerImageUrl = intent.getStringExtra(Utils.BANNER_THUMB_URI);
            prefetchShowImages(traktId, posterImageUrl, fanartImageUrl, bannerImageUrl);
        } else if (intent.getAction().equals(Utils.LOAD_IMDB_DETAILS)) {
            String imdbId = intent.getStringExtra(Utils.IMDB_ID);
            Log.d(TAG, "onHandleIntent() LOAD_IMDB_DETAILS imdbId=" + imdbId);
            loadImdbDetails(imdbId);
        } else if (intent.getAction().equals(Utils.LOAD_SEASON_DETAILS)) {
            String traktId = intent.getStringExtra(Utils.TRAKT_ID);
            Log.d(TAG, "onHandleIntent() LOAD_SEASON_DETAILS traktId=" + traktId);
            loadSeasonDetails(traktId);
            setShowLoadComplete(traktId);
        }

    }

    private void prefetchInterestingShows() {
        if (mIsNetworkAvailable) {
            AddShowActivity.clearINTERESTING_SHOW_LIST();
            // 1. Fetch 10 trending items
            Log.d(TAG, "PrefetchImageService prefetchInterestingShows() Loading TRENDING");
            String urlTrending = "https://api-v2launch.trakt.tv/shows/trending?extended=images&" + "page=" + 1 + "&limit=" + 10;
            String trendingShowResult = ContentLaoder.getContent(urlTrending);
            addTrendingShows(trendingShowResult);
            // 2 . Fetch 20 popular items
            Log.d(TAG, "PrefetchImageService prefetchInterestingShows() Loading POPULAR");
            String urlPopular = "https://api-v2launch.trakt.tv/shows/popular?extended=images&" + "page=" + 1 + "&limit=" + 20;
            String popularShowResult = ContentLaoder.getContent(urlPopular);
            addPopularShows(popularShowResult);
            broadcastAddShowIntent();
        }
    }

    private void prefetchShowImages(String traktId, String posterImageUrl, String fanartImageUrl, String bannerImageUrl) {
        try {
            // Load Poster thumb image
            //String posterImageUrl = show.getImageModel().getPosterImageCategory().getThumbImage();
            Bitmap posterBitmap = Utils.getBitmapFromURL(posterImageUrl);
            int width = Utils.dpToPx(this, (int) getResources().getDimension(R.dimen.show_poster_thumb_width));
            int height = Utils.dpToPx(this, (int) getResources().getDimension(R.dimen.show_poster_thumb_height));
            Bitmap cardCompressedBitmap = Utils.getResizedBitmap(posterBitmap, height, width);
            final String fileNamePosterThumb = getFileNameFromBitmap(traktId, cardCompressedBitmap, "poster_thumb", true);
            updateShowImagesInDB(traktId, fileNamePosterThumb, Utils.POSTER_THUMB_URI);
            broadcastUpdatedPosterImageIntent(fileNamePosterThumb, traktId);
            // Load Fanart thumb image
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            width = displayMetrics.widthPixels;
            height = (int) (width / 1.77);
            Bitmap fanartBitmap = Utils.getBitmapFromURL(fanartImageUrl);
            Bitmap resizedFanart = Utils.getResizedBitmap(fanartBitmap, height, width);
            final String fileNameFanartUrl = getFileNameFromBitmap(traktId, resizedFanart, "fanart_thumb", false);
            updateShowImagesInDB(traktId, fileNameFanartUrl, Utils.FANART_THUMB_URI);
            height = (int) (width / 5.4);
            Bitmap bannerBitmap = Utils.getBitmapFromURL(bannerImageUrl);
            Bitmap resizedBanner = Utils.getResizedBitmap(bannerBitmap, height, width);
            final String fileNameBannerUrl = getFileNameFromBitmap(traktId, resizedBanner, "banner_thumb", false);
            updateShowImagesInDB(traktId, fileNameBannerUrl, Utils.BANNER_THUMB_URI);
            broadcastUpdatedBannerImageIntent(fileNameBannerUrl, traktId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTrendingShows(String result) {
        ArrayList<Show> trendingShowList = null;
        if (result == null) {
            return;
        }
        try {
            JSONArray trendingShowJsonArray = new JSONArray(result);
            trendingShowList = new TrendingShowsParser().getTrendingShowList(trendingShowJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (trendingShowList != null) {
            // First Add the Trending Items
            for (int i = 0; i < trendingShowList.size(); i++) {
                AddShowActivity.addToINTERESTING_SHOW_LIST(trendingShowList.get(i));
                // broadcastAddShowIntent();
            }
        }
    }

    private void addPopularShows(String result) {
        ArrayList<Show> popularShowList = null;
        if (result == null) {
            return;
        }
        try {
            JSONArray popularShowJsonArray = new JSONArray(result);
            popularShowList = new PopularShowsParser().getPopularShowList(popularShowJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (popularShowList != null) {
            // Now Add Popular Items which are not trending.
            ArrayList<Show> showList = AddShowActivity.getINTERESTING_SHOW_LIST();
            if (showList == null) {
                return;
            }
            int prevSize = showList.size(); // Trending Items size
            for (int i = 0; i < popularShowList.size(); i++) {
                boolean isPresentInTrending = false;
                for (int j = 0; j < prevSize; j++) {
                    if (showList.get(j).getIDs().getTracktId().equals(popularShowList.get(i).getIDs().getTracktId())) {
                        isPresentInTrending = true;
                        break;
                    }
                }
                if (!isPresentInTrending) {
                    AddShowActivity.addToINTERESTING_SHOW_LIST(popularShowList.get(i));
                    // broadcastAddShowIntent();

                }
            }
        }
    }

    private void broadcastAddShowIntent() {
        Intent intent = new Intent();
        intent.setAction(Utils.LOAD_INTERESTING_SHOWS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdatedPosterImageIntent(String fileUrl, String tId) {
        Intent intent = new Intent();
        intent.setAction(Utils.LOAD_SHOW_IMAGES);
        intent.putExtra(Utils.POSTER_THUMB_URI, fileUrl);
        intent.putExtra(Utils.TRAKT_ID, tId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdatedBannerImageIntent(String fileUrl, String tId) {
        Intent intent = new Intent();
        intent.setAction(Utils.LOAD_SHOW_IMAGES);
        intent.putExtra(Utils.BANNER_THUMB_URI, fileUrl);
        intent.putExtra(Utils.TRAKT_ID, tId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getFileNameFromBitmap(String traktId, Bitmap bm, String fNameType, boolean isPoster) {
        String directory = traktId;
        File showDir = new File(this.getFilesDir(), directory);
        if (!showDir.exists()) {
            showDir.mkdir();
        }
        String filename = fNameType + "_" + System.currentTimeMillis() + ".JPEG";
        File imageFile = new File(showDir, filename);
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(imageFile);
            Log.d(TAG, "BEFORE COMPRESSING Byte Count=" + bm.getByteCount() + "width=" + bm.getWidth() + " height=" + bm.getHeight());
            if (isPoster) {
                bm.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
            } else {
                bm.compress(Bitmap.CompressFormat.JPEG, 20, outputStream);
            }
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
            return null;
        }
        return imageFile.getAbsolutePath();
    }

    private void updateShowImagesInDB(String traktId, String fileName, String modeColumn) {
        String where = Utils.TRAKT_ID + "=?";
        String[] whereArgs = {traktId};
        ContentValues values = new ContentValues();
        values.put(modeColumn, fileName);
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        tvListDataSource.updateTable(Utils.TVLIST_SHOW_TABLE, values, where, whereArgs);
        tvListDataSource.close();
    }

    private void loadImdbDetails(String imdbId) {
        String imdbRatingUrl = "http://www.omdbapi.com/?i=" + imdbId;
        String result = ContentLaoder.getIMDBDetails(imdbRatingUrl);
        if (result == null) {
            return;
        }
        try {
            ContentValues values = new ContentValues();
            JSONObject imdbJson = new JSONObject(result);
            if (imdbJson.has(Utils.IMDB_RATING)) {
                values.put(Utils.IMDB_RATING, imdbJson.getString(Utils.IMDB_RATING));
            }
            if (imdbJson.has(Utils.AWARDS)) {
                values.put(Utils.AWARDS, imdbJson.getString(Utils.AWARDS));
            }
            if (imdbJson.has(Utils.IMDB_VOTES)) {
                values.put(Utils.IMDB_VOTES, imdbJson.getString(Utils.IMDB_VOTES));
            }
            if (values.size() > 0) {
                String where = Utils.IMDB_ID + "=?";
                String[] whereArgs = {imdbId};
                TvListDataSource tvListDataSource = new TvListDataSource(this);
                tvListDataSource.open();
                tvListDataSource.updateTable(Utils.TVLIST_SHOW_TABLE, values, where, whereArgs);
                tvListDataSource.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void loadSeasonDetails(String showTraktId) {
        String seasonDetailUrl = "https://api-v2launch.trakt.tv/shows/" + showTraktId + "/seasons?extended=full,images,episodes";
        String seasonResult = ContentLaoder.getContent(seasonDetailUrl);
        try {
            JSONArray seasonJSONArray = new JSONArray(seasonResult);
            Log.d(TAG, "loadSeasonDetails() seasonJSONArray size=" + seasonJSONArray.length());
            SeasonParser seasonParser = new SeasonParser();
            ArrayList<Season> seasonList = seasonParser.getSeasonShowList(seasonJSONArray);
            insertSeasonDetailsInDB(showTraktId, seasonList);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    private void insertSeasonDetailsInDB(String showTraktId, ArrayList<Season> seasonList) {
        ContentValues values;
        ArrayList<ContentValues> episodeValues;
        String seasonTraktId;
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        ArrayList<Episode> episodeList;
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
            tvListDataSource.insertIntoTable(Utils.TVLIST_SEASON_TABLE, values);
            episodeList = season.getEpisodeList();
            episodeValues = insertEpisodeDetailsInDB(showTraktId, seasonTraktId, episodeList);
            if (episodeValues == null) {
                return;
            }
            for (ContentValues episode : episodeValues) {
                tvListDataSource.insertIntoTable(Utils.TVLIST_EPISODE_TABLE, episode);
            }
        }
        tvListDataSource.close();
    }

    private ArrayList<ContentValues> insertEpisodeDetailsInDB(String showTraktId, String seasonTraktId, ArrayList<Episode> episodeList) {
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

    private void setShowLoadComplete(String showTraktId) {
        String where = Utils.TRAKT_ID + "=?";
        String[] whereArgs = {showTraktId};
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        ContentValues values = new ContentValues();
        values.put(Utils.IS_LOADED, "true");
        tvListDataSource.updateTable(Utils.TVLIST_SHOW_TABLE, values, where, whereArgs);
        tvListDataSource.close();
    }
}