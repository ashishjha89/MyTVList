package com.mytvlist.list;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mytvlist.R;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.model.Show;
import com.mytvlist.utils.ImageCaches;
import com.mytvlist.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class ShowsAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<Show> mShowList;

    private boolean mIsAnimationInProgress = false;

    private int mFirstVisiblePos, mLastVisiblePos, mSelectedItemPos;

    private int mAiredRegionTimeOffset;

    private int mLocalRegionTimeOffset;

    private ListView mListView;

    private static final String TAG = "ShowsAdapter";

    private OnShowItemSelectedListener mOnShowSelectedListener;

    private long mLastDownTime;

    private int mLastViewId;

    private Tracker mTracker;

    private Bitmap mPlaceHolderBitmap;

    private static final int CLICK_WAIT_INTERVAL = 170;

    public ShowsAdapter(Context context, OnShowItemSelectedListener onShowSelectedListener, Tracker tracker) {
        mContext = context;
        mOnShowSelectedListener = onShowSelectedListener;
        mTracker = tracker;
        mShowList = new ArrayList<>();
    }

    public void setShowList(ArrayList<Show> showList) {
        mShowList = showList;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.my_tv_list);
    }

    public void setListView(ListView listView) {
        mListView = listView;
    }

    public interface OnShowItemSelectedListener {
        void selectShow(View view, int position, int totalCount);
    }

    @Override
    public int getCount() {
        if (mShowList == null) {
            return 0;
        }
        return mShowList.size();
    }

    @Override
    public Object getItem(int i) {
        return mShowList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Log.d(TAG, "getView() position=" + position + "Title=" + mShowList.get(position).getTitle());
        View row;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.shows_list_item, null);
        } else {
            row = convertView;
        }
        ShowCardHolder showCardHolder = getShowCardHolder(row, position);
        if (mShowList.size() <= position) {
            return null;
        }
        showCardHolder.titleTv.setText(getTitle(mShowList.get(position)));
        showCardHolder.showTimingTV.setText(getShowTimings(mShowList.get(position)));
        String tertiaryText = getRuntimeDetail(mShowList.get(position));
        showCardHolder.showRuntimeTV.setText(tertiaryText);

        if (mIsAnimationInProgress) {
            doAnimation(row, position);
            return row;
        }

        loadBitmap(showCardHolder.poster, showCardHolder.banner, position);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) showCardHolder.mainView.getLayoutParams();
        params.rightMargin = 0;
        params.leftMargin = 0;
        showCardHolder.mainView.setLayoutParams(params);
        showCardHolder.mainView.setOnTouchListener(new SwipeDetector(showCardHolder, position));
        showCardHolder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mShowList.get(position).getTitle();
                Toast.makeText(mContext, title + " Deleted", Toast.LENGTH_SHORT).show();
                removeEntries(mShowList.get(position));
                mShowList.remove(position);
                notifyDataSetChanged();
            }
        });
        return row;
    }

    private void selectShow(View v, int position, int totalItems) {
        mOnShowSelectedListener.selectShow(v, position, totalItems);
    }

    public void loadBitmap(ImageView posterView, ImageView bannerView, int position) {
        final String imageKey = mShowList.get(position).getIDs().getTracktId();
        loadBitmap(imageKey, posterView, true, position);
        loadBitmap(imageKey, bannerView, false, position);
    }

    public void loadBitmap(String imageKey, ImageView imageView, boolean isPoster, int position) {
        if (cancelPotentialWork(imageKey, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, isPoster, imageKey, position);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imageKey);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = "";
        private boolean isPoster;
        private String imageKey;
        private int position;

        public BitmapWorkerTask(ImageView imageView, boolean isPoster, String imageKey, int position) {
            imageViewReference = new WeakReference<>(imageView);
            this.isPoster = isPoster;
            this.imageKey = imageKey;
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            if (isPoster) {
                Bitmap posterBitmap = ImageCaches.getPosterBitmapFromMemCache(imageKey);
                if (posterBitmap != null) {
                    return posterBitmap;
                } else {
                    posterBitmap = loadPosterThumbImage(position);
                    if (posterBitmap == null) {
                        return null;
                    }
                    // Add to LRU Cache
                    ImageCaches.addPosterBitmapToMemoryCache(imageKey, posterBitmap);
                    return posterBitmap;
                }
            } else {
                Bitmap bannerBitmap = ImageCaches.getBannerBitmapFromMemCache(imageKey);
                if (bannerBitmap != null) {
                    return bannerBitmap;
                } else {
                    bannerBitmap = loadBannerThumbImage(position);
                    if (bannerBitmap == null) {
                        return null;
                    }
                    // Add to LRU Cache
                    ImageCaches.addBannerBitmapToMemoryCache(imageKey, bannerBitmap);
                    return bannerBitmap;
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (isPoster && imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    int width = Utils.dpToPx(mContext, (int) mContext.getResources().getDimension(R.dimen.show_poster_thumb_width));
                    int height = Utils.dpToPx(mContext, (int) mContext.getResources().getDimension(R.dimen.show_poster_thumb_height));
                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        ;
                    }
                }
            } else if (!isPoster && imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    int height = (int) mContext.getResources().getDimension(R.dimen.show_card_height);
                    int width = (int) (height * 5.4);
                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || bitmapData.equals("") || !bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private String getTitle(Show show) {
        if (show == null) {
            return null;
        }
        return show.getTitle();
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
            result.append(" /  " + airsDay);
        }

        if (!airsTime.isEmpty()) {
            result.append("  " + airsTime);
        }

        return result.toString();
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
        if (t[0] == null || t[0].isEmpty() || t[0].equals("null") || t[1] == null || t[1].isEmpty() || t[1].equals("null")) {
            return false;
        }
        return true;
    }

    private String getRuntimeDetail(Show show) {
        if (show == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("Runtime: ");
        String runtime = show.getRuntime();
        result.append(runtime + " Minutes");
        return result.toString();
    }

    private Bitmap loadPosterThumbImage(int position) {
        // Get Bitmap from file
        Show show = mShowList.get(position);
        ImagesModel imageModel = show.getImageModel();
        if (imageModel == null) {
            return null;
        }
        if (imageModel.getPosterImageCategory() == null) {
            return null;
        }
        String thumbImageUrl = imageModel.getPosterImageCategory().getThumbImage();
        if (thumbImageUrl == null || thumbImageUrl.isEmpty()) {
            // Log.d(TAG, "ShowsAdapter loadPosterThumbImage()");
            return null;
        }
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(thumbImageUrl, bmOptions);
        return bitmap;
    }

    private Bitmap loadBannerThumbImage(int position) {
        // Get Bitmap from file
        Show show = mShowList.get(position);
        ImagesModel imageModel = show.getImageModel();
        if (imageModel == null) {
            return null;
        }
        if (imageModel.getBannerImageCategory() == null) {
            return null;
        }
        String thumbImageUrl = imageModel.getBannerImageCategory().getFullImage();
        if (thumbImageUrl == null || thumbImageUrl.isEmpty()) {
            // Log.d(TAG, "loadBannerThumbImage() return null position=" + position + " title=" + (mShowList.get(position).getTitle()) + " thumbImageUrl=" + thumbImageUrl);
            return null;
        }
        File imageFile = new File(thumbImageUrl);
        try {
            FileInputStream inputStream = new FileInputStream(imageFile);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }


    }

    public void setIsAnimationInProgress(boolean isProgress, int firstPos, int lastPos, int selectedItemPos) {
        mIsAnimationInProgress = isProgress;
        mFirstVisiblePos = firstPos;
        mLastVisiblePos = lastPos;
        mSelectedItemPos = selectedItemPos;
    }

    private void doAnimation(View view, int pos) {
        //int height = view.getHeight();
        int height = (int) mContext.getResources().getDimension(R.dimen.show_card_height);
        //int numberOfVisibleItems = mLastVisiblePos - mFirstVisiblePos + 1;
        if (pos == mSelectedItemPos || pos > mLastVisiblePos || pos < mFirstVisiblePos) {
            return;
        }
        view.setAlpha(1);

        if (pos > mSelectedItemPos) {
            // Animate downwards
            int distance = (mLastVisiblePos - mSelectedItemPos + 2) * height;
            TranslateAnimation tAnim = new TranslateAnimation(
                    TranslateAnimation.ABSOLUTE, 0.0f,
                    TranslateAnimation.ABSOLUTE, 0.0f,
                    TranslateAnimation.ABSOLUTE, 0.0f,
                    TranslateAnimation.ABSOLUTE, distance);
            tAnim.setDuration(Utils.ANIMATION_DURATION);
            view.setAnimation(tAnim);
        } else {
            // Animate upwards
            int distance = -((mSelectedItemPos - mFirstVisiblePos) * height);
            TranslateAnimation tAnim = new TranslateAnimation(
                    TranslateAnimation.ABSOLUTE, 0.0f,
                    TranslateAnimation.ABSOLUTE, 0.0f,
                    TranslateAnimation.ABSOLUTE, 0.0f,
                    TranslateAnimation.ABSOLUTE, distance);
            tAnim.setFillAfter(true);
            tAnim.setDuration(Utils.ANIMATION_DURATION);
            view.setAnimation(tAnim);
        }
    }

    class ShowCardHolder {
        FrameLayout deleteView;
        TextView titleTv;
        TextView showTimingTV;
        TextView showRuntimeTV;
        ImageView poster;
        ImageView banner;
        FrameLayout mainView;
        FrameLayout rowLayout;
        int position;
    }

    private ShowCardHolder getShowCardHolder(View row, int position) {
        ShowCardHolder showCardHolder = new ShowCardHolder();
        showCardHolder.titleTv = (TextView) row.findViewById(R.id.title_text);
        showCardHolder.showTimingTV = (TextView) row.findViewById(R.id.show_timings);
        showCardHolder.deleteView = (FrameLayout) row.findViewById(R.id.deleteShow);
        showCardHolder.showRuntimeTV = (TextView) row.findViewById(R.id.show_runtime);
        showCardHolder.poster = (ImageView) row.findViewById(R.id.item_photo);
        showCardHolder.banner = (ImageView) row.findViewById(R.id.card_background_banner);
        showCardHolder.mainView = (FrameLayout) row.findViewById(R.id.card_view);
        showCardHolder.rowLayout = (FrameLayout) row;
        showCardHolder.position = position;
        return showCardHolder;
    }

    class SwipeDetector implements View.OnTouchListener {

        private int MIN_DISTANCE = 500;
        private static final int MIN_LOCK_DISTANCE = 70; // disallow motion intercept
        private boolean motionInterceptDisallowed = false;
        private float downX, upX;
        private ShowCardHolder holder;
        private int position;
        private int MAX_DISTANCE;
        private int deleteWidth;

        public SwipeDetector(ShowCardHolder h, int pos) {
            holder = h;
            position = pos;
            DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
            MIN_DISTANCE = (int) (displayMetrics.widthPixels * 0.20);
            MAX_DISTANCE = (int) mContext.getResources().getDimension(R.dimen.delete_button_width);
            deleteWidth = (int) mContext.getResources().getDimension(R.dimen.delete_button_width);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    // Log.d(TAG, "ACTION_DOWN");
                    downX = event.getRawX();
                    mLastDownTime = System.currentTimeMillis();
                    mLastViewId = v.getId();
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    // Log.d(TAG, "ACTION_MOVE view Id="+(v.getId()) + " last view id="+mLastViewId);
                    upX = event.getRawX();
                    float deltaX = downX - upX;
                    if ((System.currentTimeMillis() - mLastDownTime < CLICK_WAIT_INTERVAL) && mLastViewId == v.getId()) {
                        holder.deleteView.setVisibility(View.GONE);
                        // Log.d(TAG, "ACTION_MOVE 1");
                        swipe(0);
                        //return true;
                        return false;
                    }
                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && mListView != null && !motionInterceptDisallowed) {
                        mListView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                        // Log.d(TAG, "ACTION_MOVE 2");
                    }
                    if (deltaX > 0) {
                        holder.deleteView.setVisibility(View.VISIBLE);
                        // Log.d(TAG, "ACTION_MOVE 3");
                        // New change [ashish.jha begin - this if
                        if (deltaX < MAX_DISTANCE) {
                            swipe(-(int) deltaX);
                        }
                    } else {
                        // if first swiped left and then swiped right
                        swipe(0);
                    }
                    return true;
                }

                case MotionEvent.ACTION_UP:

                    upX = event.getRawX();
                    float deltaX = upX - downX;
                    // Log.d(TAG, "ACTION_UP deleteWidth=" + deleteWidth + " delta time=" + (System.currentTimeMillis() - mLastDownTime) + " deltaX=" + deltaX);
                    if ((System.currentTimeMillis() - mLastDownTime < CLICK_WAIT_INTERVAL) && (mLastViewId == v.getId()) && (Math.abs(deltaX) < 10)) {
                        if (holder.deleteView.getVisibility() == View.VISIBLE) {
                            swipe(0);
                            holder.deleteView.setVisibility(View.GONE);

                        }
                        selectShow(holder.rowLayout, holder.position, mShowList.size());
                        // Log.d(TAG, "ACTION_UP IF 1");
                        return true;
                    }
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        // left or right
                        if (deltaX < 0) {
                            swipe(-deleteWidth);
                            // Log.d(TAG, "ACTION_UP IF 2");
                        }
                    } else {
                        // Log.d(TAG, "ACTION_UP IF 2 - ELSE");
                        swipe(0);
                    }

                    if (mListView != null) {
                        // Log.d(TAG, "ACTION_UP IF 3");
                        mListView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    // Log.d(TAG, "ACTION_CANCEL");
                    upX = event.getRawX();
                    float delta1X = upX - downX;
                    if ((System.currentTimeMillis() - mLastDownTime < CLICK_WAIT_INTERVAL) && (mLastViewId == v.getId()) && (Math.abs(delta1X) < 10)) {
                        if (holder.deleteView.getVisibility() == View.VISIBLE) {
                            swipe(0);
                            holder.deleteView.setVisibility(View.GONE);

                        }
                        //selectShow(holder.rowLayout, holder.position, mShowList.size());
                        // Log.d(TAG, "ACTION_CANCEL IF 1");
                        //return true;
                        return false;
                    }
                    if (Math.abs(delta1X) > MIN_DISTANCE) {
                        // left or right
                        //swipeRemove();
                        if (delta1X < 0) {
                            swipe(-deleteWidth);
                            // Log.d(TAG, "ACTION_CANCEL IF 2");
                        }
                        //return false;
                    } else {
                        swipe(0);
                    }

                    if (mListView != null) {
                        //  Log.d(TAG, "ACTION_CANCEL IF 3");
                        mListView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }
                    return false;
            }

            return true;
        }

        private void swipe(int distance) {
            holder.deleteView.setVisibility(View.VISIBLE);
            View animationView = holder.mainView;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        }

        private void swipeRemove() {
            removeEntries(mShowList.get(position));
            mShowList.remove(position);
            notifyDataSetChanged();
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
        tvListDataSource.deleteFromTable(Utils.TVLIST_SEASON_TABLE, where, whereArgs);
        tvListDataSource.deleteFromTable(Utils.TVLIST_EPISODE_TABLE, where, whereArgs);
        tvListDataSource.close();
        // Delete Directory
        File showDir = new File(mContext.getFilesDir(), show.getIDs().getTracktId());
        if (showDir.exists()) {
            deleteFiles(showDir);
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(Utils.MY_SHOWS_SCREEN)
                .setAction(Utils.SHOW_DELETE_EVENT)
                .build());
    }

    private void deleteFiles(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                // Log.d(TAG, "Delete File - " + child.getAbsolutePath());
                deleteFiles(child);
            }
        }
        fileOrDirectory.delete();
        // Log.d(TAG, "Delete Directory - " + fileOrDirectory.getAbsolutePath());
    }

}
