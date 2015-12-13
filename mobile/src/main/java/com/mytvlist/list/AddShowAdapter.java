package com.mytvlist.list;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mytvlist.R;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.listener.ChooseItemListener;
import com.mytvlist.model.Show;
import com.mytvlist.utils.ImageCaches;
import com.mytvlist.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ashish.jha on 7/9/2015.
 */
/*
*  Adapter responsible for showing mix of 'popular shows' and 'trending shows'
 * To display 20 items to select from. Must have a net connection to show these items.
 * 10 items will be from Trending List. Remaining 10 items will be popular items but not part of Trending items.
 * To achieve this, first query for 10 trending items. Thereafter query 20 items for 'popular category'.
 * Add 'popular items' only till we have total items as 20
* */
public class AddShowAdapter extends BaseAdapter {

    private ArrayList<Show> mInterestingShowList;

    private ArrayList<Boolean> mIsAddedShow;

    private ChooseItemListener mListener;

    private Context mContext;

    private Bitmap mPlaceHolderBitmap;

    public AddShowAdapter(Context context, ChooseItemListener listener) {
        mContext = context;
        mListener = listener;
        mIsAddedShow = new ArrayList<>();
    }

    public void setInterestingList(ArrayList<Show> interestingShowList) {
        mInterestingShowList = interestingShowList;
        ArrayList<Boolean> isAddedShow = new ArrayList<>();
        int existingShowCount = mIsAddedShow.size();
        for (int i = 0; i < existingShowCount; i++) {
            isAddedShow.add(mIsAddedShow.get(i));
        }
        for (int i = existingShowCount; i < mInterestingShowList.size(); i++) {
            isAddedShow.add(false);
        }
        mIsAddedShow = isAddedShow;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.my_tv_list);
    }

    @Override
    public int getCount() {
        return mInterestingShowList.size();
    }

    @Override
    public Object getItem(int i) {
        return mInterestingShowList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return Long.valueOf(mInterestingShowList.get(i).getIDs().getTracktId());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView name;
        ImageView photo;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.add_item, null);
        }
        name = (TextView) convertView.findViewById(R.id.add_item_text);
        photo = (ImageView) convertView.findViewById(R.id.add_show_photo);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d("ashish", "onClick() position=" + position + " mIsAddedShow.get(position)=" + (mIsAddedShow.get(position)));
                // Disable the view
                if (mIsAddedShow.get(position)) {
                    // Delete the item
                    setAddedShowMode(v, false);
                    mIsAddedShow.add(position, false);
                    mListener.onListItemSelected(false, position, true);
                } else {
                    if (getShowCount() >= Utils.MAX_SHOW_LIMIT) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                mContext);
                        alertDialogBuilder.setTitle("Maximum Show Count Reached");
                        alertDialogBuilder
                                .setMessage("Maximum Show Count of " + Utils.MAX_SHOW_LIMIT + " already added")
                                .setCancelable(true);
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        // Add the item
                        setAddedShowMode(v, true);
                        mIsAddedShow.add(position, true);
                        mListener.onListItemSelected(false, position, false);
                    }
                }

            }
        });
        String title = mInterestingShowList.get(position).getTitle();
        String imageUrl = null;
        if (mInterestingShowList.get(position).getImageModel() != null && mInterestingShowList.get(position).getImageModel().getPosterImageCategory() != null) {
            imageUrl = mInterestingShowList.get(position).getImageModel().getPosterImageCategory().getThumbImage();
        }
        name.setText(title);
        String traktId = mInterestingShowList.get(position).getIDs().getTracktId();
        if (photo != null && imageUrl != null && traktId != null) {
            loadBitmap(imageUrl, photo, traktId);
        }
        setAddedShowMode(convertView, mIsAddedShow.get(position));
        return convertView;
    }

    private int getShowCount() {
        TvListDataSource tvListDataSource = new TvListDataSource(mContext);
        String[] projection = {Utils.TRAKT_ID};
        Cursor cursor = null;
        try {
            String where = Utils.CONTENT_TYPE + "=? AND " + Utils.CONTENT_CATEGORY + "=?";
            String[] whereArgs = {Utils.SHOW, Utils.CATEGORY_FAVORITE};
            tvListDataSource.open();
            cursor = tvListDataSource.getContents(Utils.TVLIST_SHOW_TABLE, projection, where, whereArgs);
            if (cursor == null || cursor.getCount() == 0) {
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
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }

    private void setAddedShowMode(View v, boolean isAdded) {
        if (isAdded) {
            // put grey state and make checkbox visible
            ImageView grayedPhotoEffect = (ImageView) v.findViewById(R.id.disable_photo);
            grayedPhotoEffect.setVisibility(View.VISIBLE);
            ImageView grayedViewEffect = (ImageView) v.findViewById(R.id.disable_view);
            grayedViewEffect.setVisibility(View.VISIBLE);
            ImageView checkImage = (ImageView) v.findViewById(R.id.show_checkbox);
            checkImage.setVisibility(View.VISIBLE);
            TextView n = (TextView) v.findViewById(R.id.add_item_text);
            n.setTextColor(mContext.getResources().getColor(R.color.disabled_text_color));
        } else {
            // hide grey state and make checkbox gone
            ImageView grayedPhotoEffect = (ImageView) v.findViewById(R.id.disable_photo);
            grayedPhotoEffect.setVisibility(View.GONE);
            ImageView grayedViewEffect = (ImageView) v.findViewById(R.id.disable_view);
            grayedViewEffect.setVisibility(View.INVISIBLE);
            ImageView checkImage = (ImageView) v.findViewById(R.id.show_checkbox);
            checkImage.setVisibility(View.GONE);
            TextView n = (TextView) v.findViewById(R.id.add_item_text);
            n.setTextColor(mContext.getResources().getColor(R.color.off_white));
        }
    }

    public void loadBitmap(String imageUrl, ImageView imageView, String traktId) {
        if (cancelPotentialWork(imageUrl, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, traktId);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imageUrl);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = "";
        private String traktId;

        public BitmapWorkerTask(ImageView imageView, String tId) {
            imageViewReference = new WeakReference<>(imageView);
            traktId = tId;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            Bitmap posterBitmap = ImageCaches.getNewShowBitmapFromMemCache(traktId);
            if (posterBitmap != null) {
                return posterBitmap;
            } else {
                Bitmap bm = downloadBitmap(data);
                ImageCaches.addNewShowBitmapToMemoryCache(traktId, bm);
                return bm;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
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

    private Bitmap downloadBitmap(String imageUrl) {
        Bitmap bitmap;
        if (imageUrl == null || imageUrl.isEmpty() || !Utils.isNetworkAvailable(mContext)) {
            return null;
        } else {
            bitmap = Utils.getBitmapFromURL(imageUrl);
        }
        return bitmap;
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
}
