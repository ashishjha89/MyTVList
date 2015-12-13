package com.mytvlist.contents;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mytvlist.MyTvListAnalyticsApplication;
import com.mytvlist.R;
import com.mytvlist.activity.ShowCastsActivity;
import com.mytvlist.activity.ShowsActivity;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.list.SeasonListAdapter;
import com.mytvlist.model.IDs;
import com.mytvlist.model.ImageCategory;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.model.Show;
import com.mytvlist.utils.ImageCaches;
import com.mytvlist.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by ashish.jha on 7/22/2015.
 */
public class ShowDetail extends Fragment {

    private final String TAG = "ShowDetail";

    private String mTraktId;

    private Show mShow;

    private String mTopImageFilePath;

    private ImageView mDetailBackground;

    private TextView mTitleTv, mShowTimingsTV, mShowRuntimeTV, mOverviewTV, mGenresTV, mIMDBRatingTV, mAwardsTV, mTraktRank;

    private ImageView mPoster, mBanner;

    private int mAiredRegionTimeOffset;

    private int mLocalRegionTimeOffset;

    private boolean mIsViewInitialized;

    private List<String> mSeasonListNumbers;

    private List<String> mSeasonTraktList;

    private HashMap<String, List<String>> mEpisodeMap;

    private HashMap<String, List<String>> mEpisodeTraktMap;

    private SeasonListAdapter mSeasonListAdapter;

    private ExpandableListView mSeasonExpListView;

    private ImageView mExpandCollapseIndicator;

    private FrameLayout mCardView;

    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        // Obtain the shared Tracker instance.
        MyTvListAnalyticsApplication application = (MyTvListAnalyticsApplication) (getActivity().getApplication());
        mTracker = application.getDefaultTracker();

        mTraktId = getActivity().getIntent().getStringExtra(Utils.TRAKT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.show_detail, container, false);
        mCardView = (FrameLayout) v.findViewById(R.id.card_view);
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        mDetailBackground = (ImageView) v.findViewById(R.id.show_detail_bg);
        mTitleTv = (TextView) v.findViewById(R.id.title_text);
        mShowTimingsTV = (TextView) v.findViewById(R.id.show_timings);
        mShowRuntimeTV = (TextView) v.findViewById(R.id.show_runtime);
        mOverviewTV = (TextView) v.findViewById(R.id.show_overview);
        mGenresTV = (TextView) v.findViewById(R.id.genres);
        mIMDBRatingTV = (TextView) v.findViewById(R.id.show_imdb_rating);
        mAwardsTV = (TextView) v.findViewById(R.id.show_awards);
        mTraktRank = (TextView) v.findViewById(R.id.show_trakt_ratings);
        mPoster = (ImageView) v.findViewById(R.id.item_photo);
        mBanner = (ImageView) v.findViewById(R.id.card_background_banner);
        mSeasonExpListView = (ExpandableListView) v.findViewById(R.id.seasonExpandableListView);
        mExpandCollapseIndicator = (ImageView) v.findViewById(R.id.season_expand_collapse_indicator);
        LinearLayout imdbIcon = (LinearLayout) v.findViewById(R.id.imdb_rating_layout);
        imdbIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String url = "http://www.imdb.com/?i=" + mShow.getIDs().getImdb();
                String url = "http://www.imdb.com/title/" + mShow.getIDs().getImdb();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("imdb"));
                i.setData(Uri.parse(url));
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(i);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.SHOW_DETAIL_SCREEN)
                        .setAction(Utils.LAUNCH_IMDB_EVENT)
                        .build());

            }
        });
        LinearLayout traktIcon = (LinearLayout) v.findViewById(R.id.trakt_rating_layout);
        traktIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://trakt.tv/shows/" + mTraktId;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.SHOW_DETAIL_SCREEN)
                        .setAction(Utils.LAUNCH_TRAKT_EVENT)
                        .build());
            }
        });
        ImageView youtubeIcon = (ImageView) v.findViewById(R.id.show_trailer);
        youtubeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra("query", mShow.getTitle() + " Official Trailer");
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.SHOW_DETAIL_SCREEN)
                        .setAction(Utils.LAUNCH_YOUTUBE_EVENT)
                        .build());
            }
        });
        RelativeLayout viewCastLayout = (RelativeLayout) v.findViewById(R.id.view_casts_layout);
        viewCastLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.isNetworkAvailable(getActivity())) {
                    Intent intent = new Intent(getActivity(), ShowCastsActivity.class);
                    intent.putExtra(Utils.TRAKT_ID, mTraktId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), " Network not available", Toast.LENGTH_LONG).show();
                }
            }
        });
        ImageView exploreInImdb = (ImageView) v.findViewById(R.id.explore_imdb);
        exploreInImdb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://www.imdb.com/title/" + mShow.getIDs().getImdb();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("imdb"));
                i.setData(Uri.parse(url));
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(i);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.SHOW_DETAIL_SCREEN)
                        .setAction(Utils.LAUNCH_IMDB_EVENT)
                        .build());
            }
        });
        ImageView exploreInTrakt = (ImageView) v.findViewById(R.id.explore_trakt);
        exploreInTrakt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://trakt.tv/shows/" + mTraktId;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.SHOW_DETAIL_SCREEN)
                        .setAction(Utils.LAUNCH_TRAKT_EVENT)
                        .build());
            }
        });
        mTraktId = ShowsActivity.SELECTED_TRAKT_ID;
        mIsViewInitialized = true;
        initDetailLayout(mTraktId);
        RelativeLayout expandCollapseIndicator = (RelativeLayout) v.findViewById(R.id.season_expand_collapse_layout);
        expandCollapseIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d(TAG, "Season Click - Season Count = " + mSeasonListNumbers.size() + " Episode Entries=" + mEpisodeMap.size());
                if (mSeasonExpListView.getVisibility() == View.GONE) {
                    mSeasonExpListView.setVisibility(View.VISIBLE);
                    final Animation myRotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_anim_expand);
                    myRotation.setFillAfter(true);
                    mExpandCollapseIndicator.startAnimation(myRotation);
                } else {
                    mSeasonExpListView.setVisibility(View.GONE);
                    final Animation myRotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_anim_collapse);
                    myRotation.setFillAfter(true);
                    mExpandCollapseIndicator.startAnimation(myRotation);
                }
            }
        });
        return v;
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, Utils.SHOW_DETAIL_SCREEN);
        mTracker.setScreenName(Utils.SHOW_DETAIL_SCREEN);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void initDetailLayout(String traktId) {
        if (!mIsViewInitialized) {
            return;
        }
        mTraktId = traktId;
        mShow = getShowsFromDB(mTraktId);
        if (mShow == null) {
            getActivity().finish();
            return;
        }
        initViews();
        prepareSeasonsListData();
        mSeasonListAdapter = new SeasonListAdapter(getActivity(), mTraktId, mSeasonListNumbers, mSeasonTraktList, mEpisodeMap, mEpisodeTraktMap,
                mTitleTv.getText().toString(), mShowTimingsTV.getText().toString(), mShowRuntimeTV.getText().toString());
        // setting list adapter
        mSeasonExpListView.setAdapter(mSeasonListAdapter);
    }

    private void initViews() {
        mTitleTv.setText(mShow.getTitle());
        mShowTimingsTV.setText(getShowTimings(mShow));
        mShowRuntimeTV.setText(getRuntimeDetail(mShow));
        mOverviewTV.setText(mShow.getOverview());
        mGenresTV.setText(mShow.getGenres());
        mIMDBRatingTV.setText(mShow.getIMDBRating());
        // TODO - Remove entry for awards when N/A
        mAwardsTV.setText(mShow.getAwards());
        String traktRating = mShow.getTraktRating();
        if (traktRating != null && !traktRating.isEmpty() && !traktRating.equalsIgnoreCase("null")) {
            // Log.d(TAG, "initViews() traktRating=" + traktRating);
            if (traktRating.length() > 3) {
                traktRating = traktRating.substring(0, 3);
            }
            mTraktRank.setText(traktRating);
        }
        Bitmap cachedPosterBitmap = ImageCaches.getPosterBitmapFromMemCache(mTraktId);
        if (cachedPosterBitmap == null) {
            loadPosterBitmap(mPoster);
        } else {
            mPoster.setImageBitmap(cachedPosterBitmap);
            loadDetailBackground(cachedPosterBitmap);
        }
        Bitmap cachedBannerBitmap = ImageCaches.getBannerBitmapFromMemCache(mTraktId);
        if (cachedBannerBitmap == null) {
            loadBannerBitmap(mBanner);
        } else {
            mBanner.setImageBitmap(cachedBannerBitmap);
        }
    }

    private String getShowTimings(Show show) {
        if (show == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        String network = show.getNetwork();
        if (!network.isEmpty()) {
            result.append(network);
        }
        String airsDay = show.getAirsDay();

        String airsTime = show.getAirsTime();

        if (!isValidDay(airsDay) || !isValidAirsTime(airsTime)) {
            return show.getStatus().toUpperCase();
        }

        if (mAiredRegionTimeOffset == 0) {
            TimeZone airedTz = TimeZone.getTimeZone(show.getAirsTimeZone());
            // Raw offset from UTC
            mAiredRegionTimeOffset = airedTz.getRawOffset();
        }
        if (mLocalRegionTimeOffset == 0) {
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            mLocalRegionTimeOffset = tz.getRawOffset();
        }
        //int seconds = (int) (mUTCTimeOffset / 1000) % 60;
        int airedOffsetMinutes = ((mAiredRegionTimeOffset / (1000 * 60)) % 60);
        int airedOffsetHours = ((mAiredRegionTimeOffset / (1000 * 60 * 60)) % 24);

        int localOffsetMinutes = ((mLocalRegionTimeOffset / (1000 * 60)) % 60);
        int localOffsetHours = ((mLocalRegionTimeOffset / (1000 * 60 * 60)) % 24);

        int totalOffsetMinutes = localOffsetMinutes - airedOffsetMinutes;
        int totalOffsetHours = localOffsetHours - airedOffsetHours;
        if (totalOffsetMinutes > 60) {
            totalOffsetMinutes = totalOffsetMinutes - 60;
            totalOffsetHours++;
        } else if (totalOffsetMinutes < -60) {
            totalOffsetMinutes = totalOffsetMinutes + 60;
            totalOffsetHours--;
        }
        if (totalOffsetHours > 24) {
            totalOffsetHours = totalOffsetHours - 24;
            airsDay = getNextDay(airsDay);
        } else if (totalOffsetHours < -24) {
            totalOffsetHours = totalOffsetHours + 24;
            airsDay = getPreviousDay(airsDay);
        }

        String[] showTime = airsTime.split(":");
        int hr = Integer.parseInt(showTime[0]);
        int min = Integer.parseInt(showTime[1]);
        if (totalOffsetMinutes + min < 0) {
            min = min + 60;
            hr = hr - 1;
            if (hr < 0) {
                airsDay = getPreviousDay(airsDay);
                hr = hr + 24;
            }
        } else if (totalOffsetMinutes + min > 60) {
            min = min - 60;
            hr = hr + 1;
            if (hr > 24) {
                airsDay = getNextDay(airsDay);
                hr = hr - 24;
            }
        }
        min = min + airedOffsetMinutes;

        if (hr + totalOffsetHours < 0) {
            hr = hr + 24;
            airsDay = getPreviousDay(airsDay);
        } else if (hr + totalOffsetHours > 24) {
            hr = hr - 24;
            airsDay = getNextDay(airsDay);
        }
        hr = hr + totalOffsetHours;
        String ampm;
        if (hr > 12) {
            hr = hr - 12;
            ampm = "PM";
        } else {
            ampm = "AM";
        }
        String h = "" + hr;
        String m = "" + min;
        if (hr < 10) {
            h = "0" + hr;
        }
        if (min == 0) {
            m = min + "0";
        }
        airsTime = "" + h + ":" + m + " " + ampm;

        if (!airsDay.isEmpty()) {
            result.append(" /  ");
            result.append(airsDay);
        }

        if (!airsTime.isEmpty()) {
            result.append("  ");
            result.append(airsTime);
        }

        return result.toString();
    }

    private boolean isValidDay(String day) {
        switch (day) {
            case "Monday":
                return true;
            case "Tuesday":
                return true;
            case "Wednesday":
                return true;
            case "Thursday":
                return true;
            case "Friday":
                return true;
            case "Saturday":
                return true;
            case "Sunday":
                return true;
            default:
                return false;
        }
    }

    private boolean isValidAirsTime(String time) {
        if (!time.contains(":")) {
            return false;
        }
        String[] t = time.split(":");
        return !(t[0] == null || t[0].isEmpty() || t[0].equals("null") || t[1] == null || t[1].isEmpty() || t[1].equals("null"));
    }

    private String getPreviousDay(String day) {
        switch (day) {
            case "Monday":
                return "Sunday";
            case "Tuesday":
                return "Monday";
            case "Wednesday":
                return "Tuesday";
            case "Thursday":
                return "Wednesday";
            case "Friday":
                return "Thursday";
            case "Saturday":
                return "Friday";
            case "Sunday":
                return "Saturday";
            default:
                return day;
        }
    }

    private String getNextDay(String day) {
        switch (day) {
            case "Monday":
                return "Tuesday";
            case "Tuesday":
                return "Wednesday";
            case "Wednesday":
                return "Thursday";
            case "Thursday":
                return "Friday";
            case "Friday":
                return "Saturday";
            case "Saturday":
                return "Sunday";
            case "Sunday":
                return "Monday";
            default:
                return day;
        }
    }

    private String getRuntimeDetail(Show show) {
        if (show == null) {
            return "";
        }
        // Log.d(TAG, "getRuntimeDetail() START");
        StringBuilder result = new StringBuilder();
        result.append("Runtime: ");
        String runtime = show.getRuntime();
        result.append(runtime);
        result.append(" Minutes");
        // Log.d(TAG, "getRuntimeDetail() END result=" + result);
        return result.toString();
    }


    private Show getShowsFromDB(String traktId) {
        // Log.d(TAG, "getShowsFromDB()");
        TvListDataSource tvListDataSource = new TvListDataSource(getActivity().getBaseContext());
        String[] projection = {Utils.TITLE, Utils.YEAR, Utils.TRAKT_ID, Utils.SLUG, Utils.TVDB_ID,
                Utils.IMDB_ID, Utils.TMDB_ID, Utils.TVRAGE, Utils.NETWORK, Utils.AIRS_DAY, Utils.AIRS_TIME,
                Utils.UPDATED_AT, Utils.STATUS, Utils.OVERVIEW, Utils.GENRES, Utils.POSTER_THUMB_URI,
                Utils.AWARDS, Utils.RUNTINME, Utils.IMDB_RATING, Utils.TRAKT_RATING, Utils.AIRS_TIMEZONE};
        Cursor cursor = null;
        try {
            tvListDataSource.open();
            String where = Utils.TRAKT_ID + "=?";
            String[] whereArgs = {traktId};
            cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                tvListDataSource.close();
                return null;
            }
            tvListDataSource.close();
            cursor.moveToFirst();
            String title = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TITLE));
            String year = cursor.getString(cursor.getColumnIndexOrThrow(Utils.YEAR));
            String network = cursor.getString(cursor.getColumnIndexOrThrow(Utils.NETWORK));
            String airsDay = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AIRS_DAY));
            String airsTime = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AIRS_TIME));
            String updatedAt = cursor.getString(cursor.getColumnIndexOrThrow(Utils.UPDATED_AT));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(Utils.STATUS));
            String overview = cursor.getString(cursor.getColumnIndexOrThrow(Utils.OVERVIEW));
            String genres = cursor.getString(cursor.getColumnIndexOrThrow(Utils.GENRES));
            String awards = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AWARDS));
            String runtime = cursor.getString(cursor.getColumnIndexOrThrow(Utils.RUNTINME));
            String imdbRating = cursor.getString(cursor.getColumnIndexOrThrow(Utils.IMDB_RATING));
            String imdbId = cursor.getString(cursor.getColumnIndexOrThrow(Utils.IMDB_ID));
            String traktRating = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TRAKT_RATING));
            String timeZone = cursor.getString(cursor.getColumnIndexOrThrow(Utils.AIRS_TIMEZONE));
            mTopImageFilePath = cursor.getString(cursor.getColumnIndexOrThrow(Utils.POSTER_THUMB_URI));
            Show show = new Show();
            show.setTitle(title);
            show.setYear(year);
            show.setNetwork(network);
            show.setAirsDay(airsDay);
            show.setAirsTime(airsTime);
            show.setUpdatedAt(updatedAt);
            show.setStatus(status);
            show.setOverview(overview);
            show.setGenres(genres);
            show.setAwards(awards);
            show.setRuntime(runtime);
            show.setImdbRating(imdbRating);
            IDs ids = new IDs();
            ids.setImdb(imdbId);
            ids.setTracktId(traktId);
            show.setIDs(ids);
            show.setTraktRating(traktRating);
            show.setAirsTimeZone(timeZone);

            ImagesModel imagesModel = new ImagesModel();
            ImageCategory posterCategory = new ImageCategory();
            posterCategory.setThumbImages(mTopImageFilePath);
            imagesModel.setPosterImageCategory(posterCategory);
            show.setImageModel(imagesModel);

            return show;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void loadPosterBitmap(ImageView imageView) {
        PosterBitmapWorkerTask task = new PosterBitmapWorkerTask(imageView);
        task.execute();
    }

    private void loadBannerBitmap(ImageView imageView) {
        BannerBitmapWorkerTask task = new BannerBitmapWorkerTask(imageView);
        task.execute();
    }


    class PosterBitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public PosterBitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            return loadPosterThumbImage();
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                    loadDetailBackground(bitmap);
                }
            }
        }
    }

    class BannerBitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public BannerBitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            return loadBannerThumbImage();
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                int height = (int) getActivity().getResources().getDimension(R.dimen.show_card_height);
                int width = (int) (height * 5.4);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    private Bitmap loadPosterThumbImage() {
        if (mShow == null || mShow.getImageModel() == null || mShow.getImageModel().getPosterImageCategory() == null) {
            return null;
        }
        // Get Bitmap from file
        String thumbImageUrl = mShow.getImageModel().getPosterImageCategory().getThumbImage();
        if (thumbImageUrl == null || thumbImageUrl.isEmpty()) {
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(thumbImageUrl, bmOptions);
        int width = (int) getActivity().getResources().getDimension(R.dimen.show_poster_thumb_width);
        int height = (int) getActivity().getResources().getDimension(R.dimen.show_poster_thumb_height);
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, /*imageView.getWidth()*/width, /*imageView.getHeight()*/height, true);
        }
        return bitmap;
    }

    private Bitmap loadBannerThumbImage() {
        if (mShow == null || mShow.getImageModel() == null || mShow.getImageModel().getBannerImageCategory() == null) {
            return null;
        }
        // Get Bitmap from file
        String imageUrl = mShow.getImageModel().getBannerImageCategory().getFullImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imageUrl, bmOptions);
        int width = (int) getActivity().getResources().getDimension(R.dimen.show_poster_thumb_width);
        int height = (int) getActivity().getResources().getDimension(R.dimen.show_poster_thumb_height);
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, /*imageView.getWidth()*/width, /*imageView.getHeight()*/height, true);
        }
        return bitmap;
    }

    private void loadDetailBackground(Bitmap bm) {
        Drawable drawable = new BitmapDrawable(getResources(), bm);
        mDetailBackground.setBackground(drawable);
    }

    private void prepareSeasonsListData() {
        mSeasonListNumbers = new ArrayList<>();
        mSeasonTraktList = new ArrayList<>();
        mEpisodeMap = new HashMap<>();
        mEpisodeTraktMap = new HashMap<>();
        loadSeasonHeaders();
    }

    private void loadSeasonHeaders() {
        String seasonNumber, seasonTraktId, episodeNumber, episodeTitle, episodeTrakt;
        ArrayList<String> episodeList;
        ArrayList<String> episodeTraktList;
        TvListDataSource tvListDataSource = new TvListDataSource(getActivity().getBaseContext());
        tvListDataSource.open();
        String[] episodeProjection = {Utils.NUMBER, Utils.TITLE, Utils.TRAKT_ID};
        String episodeWhere = Utils.SEASON_TRAKT_ID + "=?";
        String[] episodeArgs = new String[1];
        Cursor episodeCursor;
        String[] seasonProjection = {Utils.TRAKT_ID, Utils.NUMBER};
        String seasonWhere = Utils.SHOW_TRAKT_ID + "=?";
        String[] seasonWhereArgs = {mTraktId};
        Cursor seasonCursor = tvListDataSource.getContents(Utils.TVLIST_SEASON_TABLE, seasonProjection, seasonWhere, seasonWhereArgs);
        if (seasonCursor == null || seasonCursor.getCount() == 0) {
            // Log.d(TAG, "Season cursor = null");
            if (seasonCursor != null) {
                seasonCursor.close();
            }
            tvListDataSource.close();
            return;
        } else {
            // Log.d(TAG, "Season Count = " + seasonCursor.getCount());
        }
        seasonCursor.moveToFirst();
        do {
            seasonNumber = seasonCursor.getString(seasonCursor.getColumnIndexOrThrow(Utils.NUMBER));
            seasonTraktId = seasonCursor.getString(seasonCursor.getColumnIndexOrThrow(Utils.TRAKT_ID));
            episodeArgs[0] = seasonTraktId;
            episodeCursor = tvListDataSource.getContents(Utils.TVLIST_EPISODE_TABLE, episodeProjection, episodeWhere, episodeArgs);
            if (episodeCursor == null || episodeCursor.getCount() == 0) {
                // Log.d(TAG, "EPISODE cursor = null");
                if (episodeCursor != null) {
                    episodeCursor.close();
                }
                tvListDataSource.close();
                return;
            }

            episodeCursor.moveToFirst();
            episodeList = new ArrayList<>();
            episodeTraktList = new ArrayList<>();
            do {
                episodeNumber = episodeCursor.getString(episodeCursor.getColumnIndexOrThrow(Utils.NUMBER));
                episodeTitle = episodeCursor.getString(episodeCursor.getColumnIndexOrThrow(Utils.TITLE));
                episodeTrakt = episodeCursor.getString(episodeCursor.getColumnIndexOrThrow(Utils.TRAKT_ID));
                episodeList.add("Ep " + episodeNumber + " - " + episodeTitle);
                episodeTraktList.add(episodeTrakt);
            } while (episodeCursor.moveToNext());
            episodeCursor.close();
            if (episodeList.size() > 0) {
                if (!seasonNumber.equals("0")) {
                    mSeasonListNumbers.add(seasonNumber);
                    mSeasonTraktList.add(seasonTraktId);
                }
            }
            mEpisodeMap.put(seasonNumber, episodeList);
            mEpisodeTraktMap.put(seasonNumber, episodeTraktList);
        } while (seasonCursor.moveToNext());
        seasonCursor.close();
        tvListDataSource.close();
    }
}
