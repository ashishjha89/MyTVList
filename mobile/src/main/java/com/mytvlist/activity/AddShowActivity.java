package com.mytvlist.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mytvlist.MyTvListAnalyticsApplication;
import com.mytvlist.R;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.json.ShowParser;
import com.mytvlist.list.AddShowAdapter;
import com.mytvlist.list.SearchContentAdapter;
import com.mytvlist.listener.ChooseItemListener;
import com.mytvlist.listener.TaskFetchListener;
import com.mytvlist.model.Show;
import com.mytvlist.service.PrefetchService;
import com.mytvlist.tasks.ShowFetcherTask;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by ashish.jha on 7/9/2015.
 */
public class AddShowActivity extends Activity implements TaskFetchListener, ChooseItemListener {
    private static ArrayList<Show> INTERESTING_SHOW_LIST;
    private ArrayList<Show> mSearchList;
    private BroadcastReceiver mInterestingShowAdditionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "onReceive()");
            if (intent.getAction() != null && intent.getAction().equals(Utils.LOAD_INTERESTING_SHOWS) && mAdapter != null) {
                if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing()) {
                    mLoadingProgressDialog.dismiss();
                }
                setAdapter();
            }
        }
    };
    private ArrayList<Show> mSelectedShowList;
    private AddShowAdapter mAdapter;
    private SearchContentAdapter mSearchAdapter;
    private SearchView mSearchView;
    private ListView mListView;
    private ListView mSearchListView;
    private TextView mLoadingText, mTitleText;
    private ImageView mDoneButton;
    private ProgressDialog mSearchProgressDialog;
    private ProgressDialog mLoadingProgressDialog;
    private Context mContext;
    private boolean mIsLaunchFromSplash;
    private Intent mPrefetchIntent;
    private Tracker mTracker;
    private static final String TAG = "AddShowActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate()");

        // Obtain the shared Tracker instance.
        MyTvListAnalyticsApplication application = (MyTvListAnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mIsLaunchFromSplash = getIntent().getBooleanExtra(Utils.IS_CALLED_FROM_SPLASH, false);

        init();

        mTitleText.setText("Add new Shows");
        mSearchView.setEnabled(false);
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.LOAD_INTERESTING_SHOWS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mInterestingShowAdditionReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        //Log.d(TAG, "onDestroy()");
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mInterestingShowAdditionReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                //Log.w(TAG, "Tried to unregister the receiver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    @Override
    public void onBackPressed() {
        //Log.d(TAG, "onBackPressed()");
        finish();
        //super.onBackPressed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Log.d(TAG, "onNewIntent()");
        setAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSearchView.clearFocus();
        mDoneButton.setVisibility(View.VISIBLE);
        mPrefetchIntent = new Intent(this, PrefetchService.class);
        if (!Utils.isNetworkAvailable(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Network not Available")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            if (INTERESTING_SHOW_LIST.size() == 0) {
                mPrefetchIntent.setAction(Utils.LOAD_INTERESTING_SHOWS);
                startService(mPrefetchIntent);
                mLoadingProgressDialog = ProgressDialog.show(this, "Loading Shows ...", "", true);
                mLoadingProgressDialog.setCancelable(true);
                mLoadingProgressDialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, Utils.ADD_SHOW_SCREEN);
        mTracker.setScreenName(Utils.ADD_SHOW_SCREEN);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /* If activity is not started yet, then the list prepared here will be used for adapter directly, by init()
            *  If activity is already started, then the adapter needs to update to reflect change in list.
            *  Broadcast is sent by service, to which Activity responds and notify adapter.
            *  */
    public static void addToINTERESTING_SHOW_LIST(Show show) {
        if (INTERESTING_SHOW_LIST == null) {
            INTERESTING_SHOW_LIST = new ArrayList<>();
        }
        INTERESTING_SHOW_LIST.add(show);
    }

    public static void clearINTERESTING_SHOW_LIST() {
        if (INTERESTING_SHOW_LIST == null) {
            INTERESTING_SHOW_LIST = new ArrayList<>();
        }
        INTERESTING_SHOW_LIST.clear();
    }

    public static ArrayList<Show> getINTERESTING_SHOW_LIST() {
        if (INTERESTING_SHOW_LIST == null) {
            return null;
        }
        return INTERESTING_SHOW_LIST;
    }

    private void init() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.add_content);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mDoneButton.setVisibility(View.VISIBLE);
                searchShows(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //mDoneButton.setVisibility(View.GONE);
                return false;
            }
        });
        mListView = (ListView) findViewById(R.id.interesting_list);
        mSearchListView = (ListView) findViewById(R.id.searchList);
        mLoadingText = (TextView) findViewById(R.id.loading_text);
        mSearchList = new ArrayList<>();
        mDoneButton = (ImageView) findViewById(R.id.done_button);
        mTitleText = (TextView) findViewById(R.id.add_search_show_title);
        mContext = this;
        mDoneButton.setVisibility(View.VISIBLE);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMyShowActivity();
            }
        });
        if (INTERESTING_SHOW_LIST == null) {
            INTERESTING_SHOW_LIST = new ArrayList<>();
        }
        mAdapter = new AddShowAdapter(this, this);
        mSearchAdapter = new SearchContentAdapter(this, this);
        setAdapter();
    }

    private void setIsCleanLaunchPreference() {
        SharedPreferences.Editor editor = getSharedPreferences(Utils.MY_TV_LIST_PREFS, MODE_PRIVATE).edit();
        editor.putBoolean(Utils.IS_CLEAN_LAUNCH, false);
        editor.apply();
    }

    private void insertShowDetails(Show show) {
        if (getShowCount() >= Utils.MAX_SHOW_LIMIT) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    mContext);
            alertDialogBuilder.setTitle("Maximum reached");
            alertDialogBuilder
                    .setMessage(Utils.MAX_SHOW_LIMIT + " shows already added")
                    .setCancelable(true);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else if (!isAlreadyExisting(show)) {
            ShowFetcherTask showFetcherTask = new ShowFetcherTask(this, Utils.CONTENT_TYPE_ENUM.SINGLE_SHOW_SUMMARY);
            String tid = show.getIDs().getTracktId();
            String traktUrl = "https://api-v2launch.trakt.tv/shows/" + tid + "?extended=full,images";
            showFetcherTask.execute(traktUrl);
        }
    }

    private int getShowCount() {
        TvListDataSource tvListDataSource = new TvListDataSource(getBaseContext());
        String[] projection = {Utils.TRAKT_ID};
        Cursor cursor = null;
        try {
            String where = Utils.CONTENT_TYPE + "=? AND " + Utils.CONTENT_CATEGORY + "=?";
            String[] whereArgs = {Utils.SHOW, Utils.CATEGORY_FAVORITE};
            tvListDataSource.open();
            cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
                //Log.d(TAG, "getShowsFromDB() cursor == null ? " + (cursor == null));
                if (cursor != null) {
                    cursor.close();
                }
                tvListDataSource.close();
                return 0;
            }
            tvListDataSource.close();
            int count = cursor.getCount();
            return count;
        } catch (Exception e) {
            //Log.d(TAG, "getShowCount() EXCEPTION in query " + (e.getMessage()));
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private boolean isAlreadyExisting(Show show) {
        TvListDataSource tvListDataSource = new TvListDataSource(mContext);
        tvListDataSource.open();
        String where = Utils.TRAKT_ID + "=?";
        String[] whereArgs = {show.getIDs().getTracktId()};
        Cursor cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, null, where, whereArgs);
        if (cursor == null || cursor.getCount() == 0) {
            tvListDataSource.close();
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } else {
            tvListDataSource.close();
            if (cursor != null) {
                cursor.close();
            }
            return true;
        }
    }

    private void searchShows(String queryString) {
        ShowFetcherTask showSearchTask = new ShowFetcherTask(this, Utils.CONTENT_TYPE_ENUM.SEARCH_SHOW);
        String query = getProcessedQueryString(queryString);
        String traktUrl = "https://api-v2launch.trakt.tv/search?extended=images&query=" + query + "&type=show";
        //Log.d(TAG, "searchShows() traktUrl=" + traktUrl);
        showSearchTask.execute(traktUrl);
        mSearchList.clear();
        mSearchProgressDialog = ProgressDialog.show(this, "Searching ...", "", true);
        mSearchProgressDialog.setCancelable(true);
    }

    private String getProcessedQueryString(String queryString) {
        String processedQuery = "";
        queryString = queryString.trim();
        for (int i = 0; i < queryString.length(); i++) {
            if (queryString.charAt(i) == ' ') {
                if (processedQuery.length() > 0 && processedQuery.charAt(processedQuery.length() - 1) == '+') {
                    continue;
                } else {
                    processedQuery = processedQuery + "+";
                }
            } else {
                processedQuery = processedQuery + queryString.charAt(i);
            }
        }
        return processedQuery;
    }

    @Override
    public void setResult(Object result, Utils.CONTENT_TYPE_ENUM cType) {
        if (mSearchProgressDialog != null) {
            mSearchProgressDialog.dismiss();
        }
        if (result == null || !(result instanceof String)) {
            return;
        }
        String res = (String) result;
        if (res.isEmpty()) {
            return;
        }
        if (cType == Utils.CONTENT_TYPE_ENUM.SINGLE_SHOW_SUMMARY) {
            new InsertShowSummaryTask().execute(res);
        } else if (cType == Utils.CONTENT_TYPE_ENUM.SEARCH_SHOW) {
            Show show;
            ShowParser showParser;
            JSONObject typeJson;
            String type;
            JSONObject showJson;
            String imdb;
            try {
                JSONArray jsonSearchList = new JSONArray(res);
                for (int i = 0; i < jsonSearchList.length(); i++) {
                    typeJson = jsonSearchList.getJSONObject(i);
                    type = typeJson.getString(Utils.TYPE);
                    showJson = typeJson.getJSONObject(type);
                    showParser = new ShowParser();
                    show = showParser.getShow(showJson);
                    imdb = show.getIDs().getImdb();
                    if (imdb != null
                            && !imdb.isEmpty()
                            && !imdb.equals("null")
                            && show.getImageModel() != null
                            && show.getImageModel().getPosterImageCategory() != null
                            && show.getImageModel().getPosterImageCategory().getThumbImage() != null
                            && !show.getImageModel().getPosterImageCategory().getThumbImage().equals("null")) {
                        mSearchList.add(show);
                    }
                }
                mListView.setVisibility(View.GONE);
                if (mSearchList.size() == 0) {
                    hideSearchMode();
                } else {
                    mSearchAdapter.setSearchList(mSearchList);
                    mSearchListView.setAdapter(mSearchAdapter);
                    mSearchListView.setVisibility(View.VISIBLE);
                    mSearchAdapter.notifyDataSetChanged();
                    //mSearchView.setQuery("", false);
                    mSearchView.clearFocus();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Utils.ADD_SHOW_SCREEN)
                    .setAction(Utils.SEARCH_SHOW_EVENT)
                    .setLabel(Utils.SHOWS_COUNT)
                    .setValue(mAdapter.getCount())
                    .build());
        }
    }

    private void setAdapter() {
        //Log.d(TAG, "setAdapter() INTERESTING_SHOW_LIST.size=" + (INTERESTING_SHOW_LIST.size()));
        mLoadingText.setVisibility(View.GONE);
        if (mIsLaunchFromSplash) {
            mAdapter.setInterestingList(INTERESTING_SHOW_LIST);
            mListView.setAdapter(mAdapter);
        } else {
            new RemoveExistingShowsTask().execute();
        }
    }

    private class RemoveExistingShowsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            HashSet<String> traktSet = getTraktIdFromDB();
            if (traktSet == null) {
                return null;
            } else {
                String traktId;
                Show show;
                for (int i = 0; i < INTERESTING_SHOW_LIST.size(); i++) {
                    show = INTERESTING_SHOW_LIST.get(i);
                    traktId = show.getIDs().getTracktId();
                    if (traktSet.contains(traktId)) {
                        INTERESTING_SHOW_LIST.remove(i);
                        if (i != 0) {
                            i = i - 1;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.setInterestingList(INTERESTING_SHOW_LIST);
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }
    }

    private HashSet<String> getTraktIdFromDB() {
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        String[] projection = {Utils.TRAKT_ID};
        HashSet<String> mTraktIdSet = new HashSet<>();
        String traktId;
        Cursor cursor = null;
        try {
            tvListDataSource.open();
            cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, null, null);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                tvListDataSource.close();
                return null;
            }
            tvListDataSource.close();
            cursor.moveToFirst();
            do {
                traktId = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TRAKT_ID));
                mTraktIdSet.add(traktId);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mTraktIdSet;
    }

    @Override
    public void onListItemSelected(boolean isSearch, int position, boolean isDeleteAction) {
        if (!isSearch && !isDeleteAction) {
            Show show = INTERESTING_SHOW_LIST.get(position);
            insertShowDetails(show);
        } else if (!isDeleteAction && isSearch) {
            Show show = mSearchList.get(position);
            insertShowDetails(show);
            hideSearchMode();
        } else if (isDeleteAction && !isSearch) {
            // Delete the show
            Show show = INTERESTING_SHOW_LIST.get(position);
            removeEntries(show);
            Toast.makeText(this, show.getTitle() + " removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeEntries(Show show) {
        // Delete entry from DB
        TvListDataSource tvListDataSource = new TvListDataSource(mContext);
        tvListDataSource.open();
        String where = Utils.TRAKT_ID + "=?";
        String[] whereArgs = {show.getIDs().getTracktId()};
        tvListDataSource.deleteFromTable(Utils.TVLIST_SHOW_TABLE, where, whereArgs);
        where = Utils.SHOW_TRAKT_ID + "=?";
        long r = tvListDataSource.deleteFromTable(Utils.TVLIST_SEASON_TABLE, where, whereArgs);
        tvListDataSource.deleteFromTable(Utils.TVLIST_EPISODE_TABLE, where, whereArgs);
        tvListDataSource.close();
        // Delete Directory
        File showDir = new File(mContext.getFilesDir(), show.getIDs().getTracktId());
        if (showDir.exists()) {
            deleteFiles(showDir);
        }
    }

    private void deleteFiles(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                //Log.d(TAG, "Delete File - " + child.getAbsolutePath());
                deleteFiles(child);
            }
        }
        fileOrDirectory.delete();
        //Log.d(TAG, "Delete Directory - " + fileOrDirectory.getAbsolutePath());
    }

    private void hideSearchMode() {
        mSearchListView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mDoneButton.setVisibility(View.VISIBLE);
        mSearchView.setQuery("", false);
        mSearchView.clearFocus();
    }

    class InsertShowSummaryTask extends AsyncTask<String, Void, Void> {

        private String traktId;

        private String imdbId;

        private String title;

        private String posterImageUrl;

        private String fanartImageUrl;

        private String bannerImageUrl;

        @Override
        protected Void doInBackground(String... resultData) {
            Show show = insertShowSummaryInDB(resultData[0]);
            if (show != null) {
                traktId = show.getIDs().getTracktId();
                imdbId = show.getIDs().getImdb();
                title = show.getTitle();
                posterImageUrl = show.getImageModel().getPosterImageCategory().getThumbImage();
                fanartImageUrl = show.getImageModel().getFanartImageCategory().getThumbImage();
                bannerImageUrl = show.getImageModel().getBannerImageCategory().getFullImage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (traktId != null) {
                Toast.makeText(mContext, title + " Added", Toast.LENGTH_SHORT).show();
                // Load Show Images
                //Intent intent = new Intent(mContext, PrefetchImagesService.class);
                mPrefetchIntent.setAction(Utils.LOAD_SHOW_IMAGES);
                mPrefetchIntent.putExtra(Utils.TRAKT_ID, traktId);
                mPrefetchIntent.putExtra(Utils.POSTER_THUMB_URI, posterImageUrl);
                mPrefetchIntent.putExtra(Utils.FANART_THUMB_URI, fanartImageUrl);
                mPrefetchIntent.putExtra(Utils.BANNER_THUMB_URI, bannerImageUrl);
                startService(mPrefetchIntent);
                //Load Imdb Rating
                mPrefetchIntent.setAction(Utils.LOAD_IMDB_DETAILS);
                mPrefetchIntent.putExtra(Utils.IMDB_ID, imdbId);
                startService(mPrefetchIntent);
                // Load Season Details
                mPrefetchIntent.setAction(Utils.LOAD_SEASON_DETAILS);
                mPrefetchIntent.putExtra(Utils.TRAKT_ID, traktId);
                mPrefetchIntent.putExtra(Utils.TITLE, title);
                startService(mPrefetchIntent);
            }
        }
    }

    private void launchMyShowActivity() {
        if (mIsLaunchFromSplash) {
            setIsCleanLaunchPreference();
            //Intent intent = new Intent(mContext, MyShowActivity.class);
            Intent intent = new Intent(mContext, ShowsActivity.class);
            intent.putExtra(ShowsActivity.FROM_ADD_SHOW, true);
            mContext.startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    private Show insertShowSummaryInDB(String result) {
        JSONObject showJson;
        Show showWithSummary = null;
        try {
            showJson = new JSONObject(result);
            showWithSummary = new ShowParser().getShow(showJson);
            if (mSelectedShowList == null) {
                mSelectedShowList = new ArrayList<>();
            }
            mSelectedShowList.add(showWithSummary);

            ContentValues cv = getContentValuesForShowSummary(showWithSummary);
            TvListDataSource tvListDataSource = new TvListDataSource(this);
            tvListDataSource.open();
            tvListDataSource.insertIntoTable(Utils.TVLIST_SHOW_TABLE, cv);
            tvListDataSource.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return showWithSummary;
    }

    private ContentValues getContentValuesForShowSummary(Show show) {
        ContentValues values = new ContentValues();
        values.put(Utils.CONTENT_TYPE, Utils.SHOW);
        values.put(Utils.CONTENT_CATEGORY, Utils.CATEGORY_FAVORITE);
        values.put(Utils.TITLE, show.getTitle());
        values.put(Utils.YEAR, show.getYear());
        values.put(Utils.TRAKT_ID, show.getIDs().getTracktId());
        values.put(Utils.IMDB_ID, show.getIDs().getImdb());
        values.put(Utils.SLUG, show.getIDs().getSlug());
        values.put(Utils.TVDB_ID, show.getIDs().getTvdb());
        values.put(Utils.TMDB_ID, show.getIDs().getTmDb());
        values.put(Utils.TVRAGE, show.getIDs().getTvrage());
        values.put(Utils.OVERVIEW, show.getOverview());
        values.put(Utils.FIRST_AIRED, show.getFirstAired());
        values.put(Utils.AIRS_DAY, show.getAirsDay());
        values.put(Utils.AIRS_TIME, show.getAirsTime());
        values.put(Utils.AIRS_TIMEZONE, show.getAirsTimeZone());
        values.put(Utils.RUNTINME, show.getRuntime());
        values.put(Utils.NETWORK, show.getNetwork());
        values.put(Utils.COUNTRY, show.getCountry());
        values.put(Utils.UPDATED_AT, show.getUpdatedAt());
        values.put(Utils.TRAILER, show.getTrailer());
        values.put(Utils.HOMEPAGE, show.getHomepage());
        values.put(Utils.STATUS, show.getStatus());
        values.put(Utils.TRAKT_RATING, show.getTraktRating());
        values.put(Utils.IS_LOADED, "false");
        String[] genreArray = show.getGenresArray();
        if (genreArray.length == 1) {
            values.put(Utils.GENRES, genreArray[0]);
        } else {
            String genres = "";
            for (int i = 0; i < genreArray.length - 1; i++) {
                genres = genres + genreArray[i] + ",";
            }
            if (genreArray.length >= 1) {
                genres = genres + genreArray[genreArray.length - 1];
            }
            values.put(Utils.GENRES, genres);
        }
        values.put(Utils.AIRED_EPISODES, show.getAiredEpisodes());
        return values;
    }
}
