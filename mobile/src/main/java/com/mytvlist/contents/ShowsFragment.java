package com.mytvlist.contents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mytvlist.MyTvListAnalyticsApplication;
import com.mytvlist.R;
import com.mytvlist.activity.AddShowActivity;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.list.ShowsAdapter;
import com.mytvlist.model.IDs;
import com.mytvlist.model.ImageCategory;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.model.Show;
import com.mytvlist.service.UpdateShowsService;
import com.mytvlist.utils.Utils;

import java.util.ArrayList;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class ShowsFragment extends Fragment implements ShowsAdapter.OnShowItemSelectedListener {

    private TextView mLoadingTextView;

    private ListView mListView;

    private ArrayList<Show> mMyShowList;

    private ShowsAdapter mShowsAdapter;

    private Context mContext;

    private View mShowList;

    private View mMyShowListBackground;

    private ImageView mAddShowImageView;

    private boolean mIsAnimationInProgress = false;

    private OnShowSelectedListener mOnShowSelectedListener;

    private Tracker mTracker;

    private static final String TAG = "ShowsFragment";

    private BroadcastReceiver mShowUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.d(TAG, "onReceive()");
            if (intent.getAction() != null && intent.getAction().equals(Utils.LOAD_SHOW_IMAGES) && mShowsAdapter != null) {
                if (intent.hasExtra(Utils.POSTER_THUMB_URI)) {
                    String fileNamePosterThumb = intent.getStringExtra(Utils.POSTER_THUMB_URI);
                    String traktId = intent.getStringExtra(Utils.TRAKT_ID);
                    setShowPosterModel(fileNamePosterThumb, traktId);
                } else if (intent.hasExtra(Utils.BANNER_THUMB_URI)) {
                    String fileNameBanner = intent.getStringExtra(Utils.BANNER_THUMB_URI);
                    String traktId = intent.getStringExtra(Utils.TRAKT_ID);
                    setShowBannerModel(fileNameBanner, traktId);
                }
            }
        }
    };

    /*
    Interface to be used by Callback (Parent) Activity to handle DetailFragments
    attaching and detaching during the animation when a show item is selected
    */
    public interface OnShowSelectedListener {
        void initializeDetail(String traktId);

        void displayDetailInBackground(String traktId);

        void displayDetailComplete(String traktId);

        void removeDetailPage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        // Obtain the shared Tracker instance.
        MyTvListAnalyticsApplication application = (MyTvListAnalyticsApplication) (getActivity().getApplication());
        mTracker = application.getDefaultTracker();

        mShowsAdapter = new ShowsAdapter(getActivity().getBaseContext(), this, mTracker);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.LOAD_SHOW_IMAGES);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mShowUpdatedReceiver, filter);
        checkForUpdatesInShows();
    }

    @Override
    public void onAttach(Activity activity) {
        // Log.d(TAG, "onAttach()");
        super.onAttach(activity);
        mOnShowSelectedListener = (OnShowSelectedListener) activity;
    }

    @Override
    public void onStart() {
        // Log.d(TAG, "onStart()");
        super.onStart();
        mShowList.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.VISIBLE);
        mAddShowImageView.setVisibility(View.VISIBLE);
        mShowsAdapter.setIsAnimationInProgress(false, 0, 0, 0);
        loadShowsFromDB();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, Utils.MY_SHOWS_SCREEN);
        mTracker.setScreenName(Utils.MY_SHOWS_SCREEN);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Start a new session with the hit.
        /*mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setNewSession()
                .build());*/
    }

    @Override
    public void onDestroy() {
        // Log.d(TAG, "onDestroy()");
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mShowUpdatedReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
                // Log.w(TAG, "Tried to unregister the receiver when it's not registered");
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.shows_list, container, false);
        mShowList = v.findViewById(R.id.my_show_list);
        mListView = (ListView) (v.findViewById(R.id.list));
        mLoadingTextView = ((TextView) (v.findViewById(R.id.loading_text)));
        mMyShowListBackground = v.findViewById(R.id.myShowPageBackground);
        mMyShowListBackground.setVisibility(View.VISIBLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectShow(view, i, adapterView.getCount());
            }
        });
        mAddShowImageView = (ImageView) v.findViewById(R.id.add_show);
        mAddShowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.isNetworkAvailable(mContext)) {
                    Toast.makeText(mContext, "No Network Connection", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mMyShowList != null && mMyShowList.size() >= Utils.MAX_SHOW_LIMIT) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            mContext);
                    alertDialogBuilder.setTitle("Maximum reached");
                    alertDialogBuilder
                            .setMessage(Utils.MAX_SHOW_LIMIT + " shows already added")
                            .setCancelable(true);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return;
                }

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.MY_SHOWS_SCREEN)
                        .setAction(Utils.ADD_SHOW_EVENT)
                        .setLabel(Utils.SHOWS_COUNT)
                        .setValue(mShowsAdapter.getCount())
                        .build());

                mOnShowSelectedListener.removeDetailPage();
                Intent intent = new Intent(mContext, AddShowActivity.class);
                intent.putExtra(Utils.IS_CALLED_FROM_APP, true);
                mContext.startActivity(intent);
            }
        });
        mLoadingTextView.setVisibility(View.VISIBLE);
        return v;
    }

    private void checkForUpdatesInShows() {
        long lastUpdateTime = getActivity().getSharedPreferences(Utils.MY_TV_LIST_PREFS, Context.MODE_PRIVATE).getLong(Utils.LAST_UPDATE_TIME, 0);
        if (lastUpdateTime == 0) {
            updateSharedPref();
        } else if (System.currentTimeMillis() - lastUpdateTime > Utils.THRESHOLD_TIME_BETWEEN_REFRESH) {
            Intent updateShowIntent = new Intent(getActivity(), UpdateShowsService.class);
            updateShowIntent.setAction(Utils.UPDATE_SHOWS);
            getActivity().startService(updateShowIntent);
        }
    }

    private void updateSharedPref() {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(Utils.MY_TV_LIST_PREFS, Context.MODE_PRIVATE).edit();
        editor.putLong(Utils.LAST_UPDATE_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public boolean isShowLoadComplete(String traktId) {
        TvListDataSource tvListDataSource = new TvListDataSource(getActivity());
        tvListDataSource.open();
        String[] projection = {Utils.IS_LOADED};
        String where = Utils.TRAKT_ID + "=?";
        String[] whereArgs = {traktId};
        Cursor cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            tvListDataSource.close();
            return false;
        } else {
            cursor.moveToFirst();
            String isLoaded = cursor.getString(cursor.getColumnIndexOrThrow(Utils.IS_LOADED));
            cursor.close();
            tvListDataSource.close();
            if (isLoaded.equals("true")) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void setShowPosterModel(String fileNamePosterThumb, String traktId) {
        String tid;
        Show s;
        if (mMyShowList == null) {
            return;
        }
        for (int i = 0; i < mMyShowList.size(); i++) {
            s = mMyShowList.get(i);
            tid = s.getIDs().getTracktId();
            if (tid.equals(traktId)) {
                ImagesModel imagesModel = new ImagesModel();
                ImageCategory posterCategory = new ImageCategory();
                posterCategory.setThumbImages(fileNamePosterThumb);
                imagesModel.setPosterImageCategory(posterCategory);
                s.setImageModel(imagesModel);
                mShowsAdapter.setShowList(mMyShowList);
                mShowsAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void setShowBannerModel(String fileNameThumb, String traktId) {
        String tid;
        Show s;
        if (mMyShowList == null) {
            return;
        }
        for (int i = 0; i < mMyShowList.size(); i++) {
            s = mMyShowList.get(i);
            tid = s.getIDs().getTracktId();
            if (tid.equals(traktId)) {
                ImagesModel imagesModel = new ImagesModel();
                ImageCategory bannerCategory = new ImageCategory();
                bannerCategory.setFullImages(fileNameThumb);
                imagesModel.setBannerImageCategory(bannerCategory);
                s.setImageModel(imagesModel);
                mShowsAdapter.setShowList(mMyShowList);
                mShowsAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void loadShowsFromDB() {
        new ShowListFetcherTaskFromDB().execute();
    }

    private class ShowListFetcherTaskFromDB extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (mMyShowList != null) {
                mMyShowList.clear();
            }
            getShowsFromDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mMyShowList == null || mMyShowList.size() == 0) {
                return;
            }
            setAdapter();
        }
    }

    /*
    * If MY_SHOW_LIST is already fetched by PrefetchImagesService, then call to setAdapter should be made.
    * If MY_SHOW_LIST is not already fetched, then PrefetchImagesService will broadcast intent. setAdapter will be called from broadcast receiver
    * */
    private void setAdapter() {
        if (mMyShowList == null) {
            return;
        }
        if (mMyShowList.size() > 0) {
            mLoadingTextView.setVisibility(View.GONE);
        }
        // Log.d(TAG, "setAdapter() mMyShowList.size=" + mMyShowList.size());
        mShowsAdapter.setListView(mListView);
        mShowsAdapter.setShowList(mMyShowList);
        mListView.setAdapter(mShowsAdapter);
        mShowsAdapter.notifyDataSetChanged();
    }

    private void getShowsFromDB() {
        TvListDataSource tvListDataSource = new TvListDataSource(getActivity().getBaseContext());
        String[] projection = {Utils.TITLE, Utils.YEAR, Utils.TRAKT_ID, Utils.SLUG, Utils.TVDB_ID,
                Utils.IMDB_ID, Utils.TMDB_ID, Utils.TVRAGE, Utils.NETWORK, Utils.AIRS_DAY, Utils.AIRS_TIME,
                Utils.UPDATED_AT, Utils.STATUS, Utils.OVERVIEW, Utils.GENRES, Utils.POSTER_THUMB_URI, Utils.BANNER_THUMB_URI,
                Utils.AWARDS, Utils.RUNTINME, Utils.IMDB_RATING, Utils.IMDB_VOTES, Utils.AIRS_TIMEZONE};
        Cursor cursor = null;
        try {
            String where = Utils.CONTENT_TYPE + "=? AND " + Utils.CONTENT_CATEGORY + "=?";
            String[] whereArgs = {Utils.SHOW, Utils.CATEGORY_FAVORITE};
            tvListDataSource.open();
            cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
                // Log.d(TAG, "getShowsFromDB() cursor == null ? " + (cursor == null));
                if (cursor != null) {
                    cursor.close();
                }
                tvListDataSource.close();
                return;
            }
            tvListDataSource.close();
            cursor.moveToFirst();
            String title, year, traktId, slug, tvdb, imdb, tmdb, tvrage;
            String network, airsDay, airsTime, updatedAt, status, imdbRating, runtime, imdbVotes, timeZone;
            String cardImageUrl, bannerImageUrl;
            Show show;
            IDs ids;
            do {
                title = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TITLE));
                year = cursor.getString(cursor.getColumnIndexOrThrow(Utils.YEAR));
                traktId = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TRAKT_ID));
                slug = cursor.getString(cursor.getColumnIndexOrThrow(Utils.SLUG));
                tvdb = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TVDB_ID));
                imdb = cursor.getString(cursor.getColumnIndexOrThrow(Utils.IMDB_ID));
                tmdb = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TMDB_ID));
                tvrage = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TVRAGE));
                network = cursor.getString(cursor.getColumnIndexOrThrow(Utils.NETWORK));
                airsDay = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AIRS_DAY));
                airsTime = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AIRS_TIME));
                updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(Utils.UPDATED_AT));
                status = cursor.getString(cursor.getColumnIndexOrThrow(Utils.STATUS));
                cardImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Utils.POSTER_THUMB_URI));
                bannerImageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Utils.BANNER_THUMB_URI));
                imdbRating = cursor.getString(cursor.getColumnIndexOrThrow(Utils.IMDB_RATING));
                imdbVotes = cursor.getString(cursor.getColumnIndexOrThrow(Utils.IMDB_VOTES));
                runtime = cursor.getString(cursor.getColumnIndexOrThrow(Utils.RUNTINME));
                timeZone = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AIRS_TIMEZONE));
                ids = new IDs(traktId, slug, tvdb, imdb, tmdb, tvrage);
                show = new Show(title, year, ids);
                show.setNetwork(network);
                show.setAirsDay(airsDay);
                show.setAirsTime(airsTime);
                show.setUpdatedAt(updatedAt);
                show.setStatus(status);
                show.setImdbRating(imdbRating);
                show.setRuntime(runtime);
                show.setImdbVotes(imdbVotes);
                show.setAirsTimeZone(timeZone);

                ImagesModel imagesModel = new ImagesModel();
                ImageCategory posterCategory = new ImageCategory();
                posterCategory.setThumbImages(cardImageUrl);
                imagesModel.setPosterImageCategory(posterCategory);
                ImageCategory bannerCategory = new ImageCategory();
                bannerCategory.setFullImages(bannerImageUrl);
                imagesModel.setBannerImageCategory(bannerCategory);
                show.setImageModel(imagesModel);

                if (mMyShowList == null) {
                    mMyShowList = new ArrayList<>();
                }
                mMyShowList.add(show);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            // Log.d(TAG, "getPopularShowsFromDB() EXCEPTION in query " + (e.getMessage()));
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void selectShow(View view, int position, int totalItems) {
        // Log.d(TAG, "selectShow()");
        String traktId = mMyShowList.get(position).getIDs().getTracktId();
        if (!isShowLoadComplete(traktId)) {
            Toast.makeText(getActivity(), "Please wait. Show is still being loaded", Toast.LENGTH_LONG).show();
            return;
        }
        if (totalItems < 2) {
            mOnShowSelectedListener.initializeDetail(traktId);
            mOnShowSelectedListener.displayDetailComplete(traktId);
            return;
        }
        if (!mIsAnimationInProgress) {
            mIsAnimationInProgress = true;
            mOnShowSelectedListener.initializeDetail(traktId);
            animateView(position, view);
        }
    }

    private void animateView(final int position, final View view) {
        mShowsAdapter.setIsAnimationInProgress(true, mListView.getFirstVisiblePosition(), mListView.getLastVisiblePosition(), position);
        mShowsAdapter.notifyDataSetChanged();
        // Log.d(TAG, "doAnimation() VIEW pos=" + position + " distance=-" + (view.getTop()) + "view height=" + view.getHeight());
        TranslateAnimation tanim = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0.0f,
                TranslateAnimation.ABSOLUTE, 0.0f,
                TranslateAnimation.ABSOLUTE, 0.0f,
                TranslateAnimation.ABSOLUTE, -(view.getTop()));
        tanim.setDuration(Utils.ANIMATION_DURATION);
        view.setAnimation(tanim);
        tanim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAddShowImageView.setVisibility(View.GONE);
                mMyShowListBackground.setVisibility(View.GONE);
                mOnShowSelectedListener.displayDetailInBackground(mMyShowList.get(position).getIDs().getTracktId());
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mShowList.setVisibility(View.GONE);
                mIsAnimationInProgress = false;
                mOnShowSelectedListener.displayDetailComplete(mMyShowList.get(position).getIDs().getTracktId());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }
}
