package com.mytvlist.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mytvlist.MyTvListAnalyticsApplication;
import com.mytvlist.R;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.model.Episode;
import com.mytvlist.model.ImageCategory;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.model.Show;
import com.mytvlist.utils.ImageCaches;
import com.mytvlist.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ashish on 15/8/15.
 */
public class EpisodeDetailActivity extends Activity {

    private String mShowTraktId, mSeasonTraktId, mCurrentTraktId;

    private ImageView mDetailBackground;

    private TextView mTitleTv, mShowTimingsTV, mShowRuntimeTV, mOverviewTV, mEpisodeTitle, mEpisodeFirstAiredDay, mEpisodeHeader;

    private ImageView mPoster, mBanner, mEpisodeImage;

    private FrameLayout mPrevIndicator, mNextIndicator;

    private Show mShow;

    private Episode mCurrentEpisode;

    private ArrayList<String> mEpisodeTraktIdList;

    private FrameLayout mCardView;

    private Context mContext;

    private String mCurrentSeasonNumber, mCurrentEpisodeNumber;

    private ProgressDialog mProgressDialog;

    private EpisodeLoaderWorkerTask mEpisodeLoaderWorkerTask;

    private EpisodeImageLoaderWorkerTask mEpisodeImageLoaderWorkerTask;

    private int mCurrentIndex = -1;

    private Tracker mTracker;

    private final static String TAG = "EpisodeDetailActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        mContext = this;

        // Obtain the shared Tracker instance.
        MyTvListAnalyticsApplication application = (MyTvListAnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        setContentView(R.layout.episode_detail);
        mCardView = (FrameLayout) findViewById(R.id.card_view);
        mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mShowTraktId = getIntent().getStringExtra(Utils.SHOW_TRAKT_ID);
        mSeasonTraktId = getIntent().getStringExtra(Utils.SEASON_TRAKT_ID);
        mCurrentTraktId = getIntent().getStringExtra(Utils.TRAKT_ID);
        String showTitle = getIntent().getStringExtra(Utils.TITLE);
        String channelAndTime = getIntent().getStringExtra(Utils.AIRS_TIME);
        String runtime = getIntent().getStringExtra(Utils.RUNTINME);
        // Log.d(TAG, "onCreate() mShowTraktId=" + mShowTraktId + " mSeasonTraktId=" + mSeasonTraktId + " mCurrentTraktId=" + mCurrentTraktId);
        initViews();
        mTitleTv.setText(showTitle);
        mShowTimingsTV.setText(channelAndTime);
        mShowRuntimeTV.setText(runtime);
        initEpisodeTraktIdList();
        if (mEpisodeTraktIdList.size() < 2) {
            mPrevIndicator.setVisibility(View.GONE);
            mNextIndicator.setVisibility(View.GONE);
        }
        refreshIndicatorVisibility();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // overridePendingTransition(R.anim.slide_in, R.anim.slide_back);
        overridePendingTransition(R.anim.nothing, R.anim.slide_back);
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, Utils.EPISODE_DETAIL_SCREEN);
        mTracker.setScreenName(Utils.EPISODE_DETAIL_SCREEN);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void initViews() {
        mDetailBackground = (ImageView) findViewById(R.id.episode_detail_bg);
        mTitleTv = (TextView) findViewById(R.id.title_text);
        mShowTimingsTV = (TextView) findViewById(R.id.show_timings);
        mShowRuntimeTV = (TextView) findViewById(R.id.show_runtime);
        mOverviewTV = (TextView) findViewById(R.id.episode_overview);
        mEpisodeTitle = (TextView) findViewById(R.id.episodeTitle);
        mEpisodeHeader = (TextView) findViewById(R.id.episodeHeader);
        mEpisodeFirstAiredDay = (TextView) findViewById(R.id.episodeFirstAirDay);
        mPoster = (ImageView) findViewById(R.id.item_photo);
        mBanner = (ImageView) findViewById(R.id.card_background_banner);
        mEpisodeImage = (ImageView) findViewById(R.id.episodeImage);
        mPrevIndicator = (FrameLayout) findViewById(R.id.prev_ep_indicator);
        loadShow();
        mPrevIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousEpisode();
            }
        });
        mNextIndicator = (FrameLayout) findViewById(R.id.next_ep_indicator);
        mNextIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextEpisode();
            }
        });
        mEpisodeTraktIdList = new ArrayList<>();
        Bitmap cachedPosterBitmap = ImageCaches.getPosterBitmapFromMemCache(mShowTraktId);
        if (cachedPosterBitmap == null) {
            loadPosterBitmap(mPoster);
        } else {
            mPoster.setImageBitmap(cachedPosterBitmap);
            loadDetailBackground(cachedPosterBitmap);
        }
        Bitmap cachedBannerBitmap = ImageCaches.getBannerBitmapFromMemCache(mShowTraktId);
        if (cachedBannerBitmap == null) {
            loadBannerBitmap(mBanner);
        } else {
            mBanner.setImageBitmap(cachedBannerBitmap);
        }
        ImageView youtubeIcon = (ImageView) findViewById(R.id.show_trailer);
        youtubeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                String queryString2 = mTitleTv.getText() + " season" + mCurrentSeasonNumber + " episode" + mCurrentEpisodeNumber + " promo";
                intent.putExtra("query", queryString2);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(Utils.EPISODE_DETAIL_SCREEN)
                        .setAction(Utils.LAUNCH_YOUTUBE_EVENT)
                        .build());
            }
        });
        // LinearLayout episodeContent = (LinearLayout) findViewById(R.id.episode_content_layout);
        // episodeContent.setOnTouchListener(new LinearLayoutTouchListener(this));
    }

    private void setPreviousEpisode() {
        // Log.d(TAG, "setPreviousEpisode()");
        if (mCurrentIndex == 0) {
            return;
        }
        if (mCurrentIndex > 0) {
            mCurrentIndex--;
        }
        refreshIndicatorVisibility();
        mCurrentTraktId = mEpisodeTraktIdList.get(mCurrentIndex);
        loadEpisodeInBackground(mCurrentTraktId);
    }

    private void setNextEpisode() {
        // Log.d(TAG, "setNextEpisode()");
        if (mCurrentIndex == mEpisodeTraktIdList.size() - 1) {
            return;
        }
        if (mCurrentIndex < mEpisodeTraktIdList.size()) {
            mCurrentIndex++;
        }
        refreshIndicatorVisibility();
        mCurrentTraktId = mEpisodeTraktIdList.get(mCurrentIndex);
        loadEpisodeInBackground(mCurrentTraktId);
    }

    private void startProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Loading...", "", true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void loadShow() {
        mShow = null;
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        String[] projection = {Utils.FANART_THUMB_URI, Utils.POSTER_THUMB_URI, Utils.BANNER_THUMB_URI};
        String where = Utils.TRAKT_ID + "=?";
        String[] whereArgs = {mShowTraktId};
        Cursor cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();
        String fanArtUrl = cursor.getString(cursor.getColumnIndexOrThrow(Utils.FANART_THUMB_URI));
        String posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(Utils.POSTER_THUMB_URI));
        String bannerUrl = cursor.getString(cursor.getColumnIndexOrThrow(Utils.BANNER_THUMB_URI));
        mShow = new Show();
        ImagesModel imagesModel = new ImagesModel();
        ImageCategory fanart = new ImageCategory();
        fanart.setThumbImages(fanArtUrl);
        imagesModel.setFanartImageCategory(fanart);
        ImageCategory poster = new ImageCategory();
        poster.setThumbImages(posterUrl);
        imagesModel.setPosterImageCategory(poster);
        ImageCategory banner = new ImageCategory();
        banner.setThumbImages(bannerUrl);
        imagesModel.setFanartImageCategory(banner);
        mShow.setImageModel(imagesModel);
    }

    private void initEpisodeTraktIdList() {
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        String[] projection = {Utils.TRAKT_ID, Utils.OVERVIEW, Utils.POSTER, Utils.TITLE, Utils.FIRST_AIRED, Utils.NUMBER, Utils.SEASON};
        String where = Utils.SHOW_TRAKT_ID + "=? and " + Utils.SEASON_TRAKT_ID + "=?";
        String[] whereArgs = {mShowTraktId, mSeasonTraktId};
        Cursor cursor = null;
        try {
            cursor = tvListDataSource.getContents(Utils.TVLIST_EPISODE_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
                // Log.d(TAG, "initEpisodeTraktIdList() cursor empty");
                return;
            }
            // Log.d(TAG, "initEpisodeTraktIdList() cursor size " + (cursor.getCount()));
            String epTraktId, overview, poster, title, airedTime, episodeNumber, seasonNumber;
            cursor.moveToFirst();
            do {
                epTraktId = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TRAKT_ID));
                overview = cursor.getString(cursor.getColumnIndexOrThrow(Utils.OVERVIEW));
                poster = cursor.getString(cursor.getColumnIndexOrThrow(Utils.POSTER));
                title = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TITLE));
                airedTime = cursor.getString(cursor.getColumnIndexOrThrow(Utils.FIRST_AIRED));
                episodeNumber = cursor.getString(cursor.getColumnIndexOrThrow(Utils.NUMBER));
                seasonNumber = cursor.getString(cursor.getColumnIndexOrThrow(Utils.SEASON));
                // Log.d(TAG, "initEpisodeTraktIdList() epTraktId=" + epTraktId + " mCurrentTraktId=" + mCurrentTraktId);
                if (epTraktId != null) {
                    mEpisodeTraktIdList.add(epTraktId);
                    if (epTraktId.equals(mCurrentTraktId)) {
                        mCurrentIndex = mEpisodeTraktIdList.size() - 1;
                        mEpisodeTitle.setText(title);
                        mCurrentSeasonNumber = seasonNumber;
                        mCurrentEpisodeNumber = episodeNumber;
                        mEpisodeHeader.setText("Season " + mCurrentSeasonNumber + " - Episode " + mCurrentEpisodeNumber);
                        setEpisodeTitle(title);
                        setEpisodeOverview(overview);
                        setEpisodeImage(poster);
                        setEpisodeTime(airedTime);
                    }
                }
            } while (cursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            tvListDataSource.close();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void refreshIndicatorVisibility() {
        if (mCurrentIndex == 0) {
            mPrevIndicator.setVisibility(View.GONE);
        } else {
            mPrevIndicator.setVisibility(View.VISIBLE);
        }
        if (mCurrentIndex >= mEpisodeTraktIdList.size() - 1) {
            mNextIndicator.setVisibility(View.GONE);
        } else {
            mNextIndicator.setVisibility(View.VISIBLE);
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
                int height = (int) getResources().getDimension(R.dimen.show_card_height);
                int width = (int) (height * 5.4);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    class EpisodeBitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        private String imageUrl;

        public EpisodeBitmapWorkerTask(ImageView imageView, String src) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference(imageView);
            imageUrl = src;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Void... params) {
            return loadEpisodeThumbImage(imageUrl);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    class EpisodeLoaderWorkerTask extends AsyncTask<String, Void, Void> {

        EpisodeLoaderWorkerTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startProgressDialog();
        }

        @Override
        protected Void doInBackground(String... params) {
            // Log.d(TAG, "EpisodeLoaderWorkerTask doInBackground() mCurrentEpisodeNumber=" + mCurrentEpisodeNumber);
            String[] projection = {Utils.OVERVIEW, Utils.POSTER, Utils.TITLE, Utils.FIRST_AIRED, Utils.SEASON, Utils.NUMBER};
            String where = Utils.SHOW_TRAKT_ID + "=? and " + Utils.SEASON_TRAKT_ID + "=? and " + Utils.TRAKT_ID + "=?";
            String[] whereArgs = {mShowTraktId, mSeasonTraktId, params[0]};
            TvListDataSource tvListDataSource = new TvListDataSource(mContext);
            tvListDataSource.open();
            Cursor cursor = tvListDataSource.getContents(Utils.TVLIST_EPISODE_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
                // Log.d(TAG, "EpisodeLoaderWorkerTask() cursor empty");
                return null;
            }
            cursor.moveToFirst();
            String overview = cursor.getString(cursor.getColumnIndexOrThrow(Utils.OVERVIEW));
            String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(Utils.POSTER));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(Utils.TITLE));
            String firstAired = cursor.getString(cursor.getColumnIndexOrThrow(Utils.FIRST_AIRED));
            String seasonNumber = cursor.getString(cursor.getColumnIndexOrThrow(Utils.SEASON));
            String episodeNumber = cursor.getString(cursor.getColumnIndexOrThrow(Utils.NUMBER));
            cursor.close();
            tvListDataSource.close();
            Episode episode = new Episode();
            episode.setOverview(overview);
            episode.setTitle(title);
            episode.setFirstAiredTime(firstAired);
            episode.setSeasonNumber(seasonNumber);
            episode.setEpisodeNumber(episodeNumber);
            mCurrentSeasonNumber = seasonNumber;
            mCurrentEpisodeNumber = episodeNumber;
            episode.setThumbImageUrl(imageUrl);
            mCurrentEpisode = episode;
            return null;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Void v) {
            // Log.d(TAG, "EpisodeLoaderWorkerTask onPostExecute() mCurrentEpisodeNumber=" + mCurrentEpisodeNumber);
            setEpisodeTitle(mCurrentEpisode.getTitle());
            setEpisodeOverview(mCurrentEpisode.getOverview());
            setEpisodeTime(mCurrentEpisode.getFirstAiredTime());
            mEpisodeHeader.setText("Season " + mCurrentEpisode.getSeasonNumber() + " - Episode " + mCurrentEpisode.getEpisodeNumber());
            //new EpisodeImageLoaderWorkerTask(mCurrentEpisodeNumber).execute(mCurrentEpisode.getThumbImageUrl());
            if (mEpisodeImageLoaderWorkerTask != null) {
                mEpisodeImageLoaderWorkerTask.cancel(true);
            }
            mEpisodeImageLoaderWorkerTask = new EpisodeImageLoaderWorkerTask(mCurrentEpisodeNumber);
            mEpisodeImageLoaderWorkerTask.execute(mCurrentEpisode.getThumbImageUrl());
            dismissProgressDialog();
        }
    }


    class EpisodeImageLoaderWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private String currentEpisodeNumber;

        EpisodeImageLoaderWorkerTask(String epNo) {
            currentEpisodeNumber = epNo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // Log.d(TAG, "EpisodeImageLoaderWorkerTask() doInBackground() currentEpisodeNumber=" + currentEpisodeNumber + " mCurrentEpisodeNumber=" + mCurrentEpisodeNumber);
            String imageUrl = params[0];
            if (Utils.isNetworkAvailable(mContext)) {
                // Log.d(TAG, "EpisodeImageLoaderWorkerTask() imageUrl=" + imageUrl);
                Bitmap bitmap = Utils.getBitmapFromURL(imageUrl);
                if (bitmap != null) {
                    return bitmap;
                }
            }
            Bitmap bitmap = loadDefaultEpisodeBackground();
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Log.d(TAG, "EpisodeImageLoaderWorkerTask() onPostExecute() currentEpisodeNumber=" + currentEpisodeNumber + " mCurrentEpisodeNumber=" + mCurrentEpisodeNumber);
            if (bitmap != null && currentEpisodeNumber.equals(mCurrentEpisodeNumber)) {
                // Log.d(TAG, "EpisodeImageLoaderWorkerTask() onPostExecute() APPLY IMAGES");
                mEpisodeImage.setImageBitmap(bitmap);
            }
        }
    }


    private Bitmap loadPosterThumbImage() {
        // Get Bitmap from file
        if (mShow.getImageModel() == null || mShow.getImageModel().getPosterImageCategory() == null) {
            return null;
        }
        String thumbImageUrl = mShow.getImageModel().getPosterImageCategory().getThumbImage();
        if (thumbImageUrl == null || thumbImageUrl.isEmpty()) {
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(thumbImageUrl, bmOptions);
        int width = (int) getResources().getDimension(R.dimen.show_poster_thumb_width);
        int height = (int) getResources().getDimension(R.dimen.show_poster_thumb_height);
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        return bitmap;
    }

    private Bitmap loadBannerThumbImage() {
        // Get Bitmap from file
        if (mShow.getImageModel() == null || mShow.getImageModel().getBannerImageCategory() == null) {
            return null;
        }
        String imageUrl = mShow.getImageModel().getBannerImageCategory().getFullImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imageUrl, bmOptions);
        int width = (int) getResources().getDimension(R.dimen.show_poster_thumb_width);
        int height = (int) getResources().getDimension(R.dimen.show_poster_thumb_height);
        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        return bitmap;
    }

    private Bitmap loadEpisodeThumbImage(String imageUrl) {
        Bitmap bitmap;
        // Get Bitmap from file
        if (imageUrl == null || imageUrl.isEmpty() || !Utils.isNetworkAvailable(this)) {
            bitmap = loadDefaultEpisodeBackground();
        } else {
            bitmap = Utils.getBitmapFromURL(imageUrl);
            if (bitmap == null) {
                bitmap = loadDefaultEpisodeBackground();
            }
        }
        return bitmap;
    }

    private Bitmap loadDefaultEpisodeBackground() {
        if (mShow == null) {
            return null;
        }
        if (mShow.getImageModel() == null || mShow.getImageModel().getFanartImageCategory() == null) {
            return null;
        }
        String url = mShow.getImageModel().getFanartImageCategory().getThumbImage();
        if (url == null || url.isEmpty()) {
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(url, bmOptions);
        return bitmap;
    }

    private void loadDetailBackground(Bitmap bm) {
        // Log.d(TAG, "loadDetailBackground()");
        if (bm == null) {
            return;
        }
        Drawable drawable = new BitmapDrawable(getResources(), bm);
        mDetailBackground.setBackground(drawable);
    }

    private void loadEpisodeInBackground(String epTraktId) {
        // Log.d(TAG, "loadEpisodeInBackground() epTraktId=" + epTraktId + " mCurrentIndex=" + mCurrentIndex);
        if (mEpisodeLoaderWorkerTask != null) {
            mEpisodeLoaderWorkerTask.cancel(true);
            dismissProgressDialog();
        }
        mEpisodeLoaderWorkerTask = new EpisodeLoaderWorkerTask();
        mEpisodeLoaderWorkerTask.execute(epTraktId);
    }

    private void setEpisodeTitle(String title) {
        // Log.d(TAG, "setEpisodeTitle() title=" + title);
        mEpisodeTitle.setText(title);
    }

    private void setEpisodeOverview(String overview) {
        mOverviewTV.setText(overview);
    }

    private void setEpisodeImage(String imageUrl) {
        new EpisodeBitmapWorkerTask(mEpisodeImage, imageUrl).execute();
    }

    private void setEpisodeTime(String airTime) {
        // Log.d(TAG, "setEpisodeTime() airTime=" + airTime);
        if (airTime == null || airTime.length() < 10) {
            return;
        }
        airTime = airTime.substring(0, 10);
        String[] time = airTime.split("-");
        if (time.length != 3) {
            mEpisodeFirstAiredDay.setText(airTime);
        } else {
            String year = time[0];
            String month = getMonth(time[1]);
            mEpisodeFirstAiredDay.setText("(" + time[2] + " " + month + " " + year + ")");
        }

    }

    private String getMonth(String mon) {
        switch (mon) {
            case "01":
                return Utils.JANUARY;
            case "02":
                return Utils.FEBRUARY;
            case "03":
                return Utils.MARCH;
            case "04":
                return Utils.APRIL;
            case "05":
                return Utils.MAY;
            case "06":
                return Utils.JUNE;
            case "07":
                return Utils.JULY;
            case "08":
                return Utils.AUGUST;
            case "09":
                return Utils.SEPTEMBER;
            case "10":
                return Utils.OCTOBER;
            case "11":
                return Utils.NOVEMBER;
            case "12":
                return Utils.DECEMBER;
            default:
                return mon;
        }
    }


    class LinearLayoutTouchListener implements View.OnTouchListener {

        private Activity activity;

        private int MIN_DISTANCE;

        private float downX, upX;

        public LinearLayoutTouchListener(Activity mainActivity) {
            activity = mainActivity;
            MIN_DISTANCE = (int) (activity.getResources().getDisplayMetrics().widthPixels * 0.35);
        }

        public void onRightToLeftSwipe() {
            if (mPrevIndicator.getVisibility() == View.VISIBLE) {
                setPreviousEpisode();
            }
        }

        public void onLeftToRightSwipe() {
            if (mNextIndicator.getVisibility() == View.VISIBLE) {
                setNextEpisode();
            }
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    upX = event.getX();

                    float deltaX = downX - upX;

                    // swipe horizontal?
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        // left or right
                        if (deltaX < 0) {
                            this.onLeftToRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onRightToLeftSwipe();
                            return true;
                        }
                    }

                    return false; // no swipe horizontally and no swipe vertically
                }// case MotionEvent.ACTION_UP:
            }
            return false;
        }

    }
}
