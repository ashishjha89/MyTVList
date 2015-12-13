package com.mytvlist.list;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mytvlist.R;
import com.mytvlist.listener.ChooseItemListener;
import com.mytvlist.model.Show;
import com.mytvlist.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ashish.jha on 7/9/2015.
 */
/*
*  Adapter responsible for showing mix of 'popular shows' and 'trending shows'
 * To display 20 items to select from. Must have a net connection to show these items.
 * 10 items will be from Trending List. Reamaining 10 items will be popular items but not part of Trending items.
 * To achieve this, first query for 10 trending items. Thereafter query 20 items for 'popular category'.
 * Add 'popular items' only till we have total items as 20
* */
public class SearchContentAdapter extends BaseAdapter {

    private ArrayList<Show> mSearchShowList;

    private ChooseItemListener mListener;

    private Context mContext;

    public SearchContentAdapter(Context context, ChooseItemListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void setSearchList(ArrayList<Show> interestingShowList) {
        mSearchShowList = interestingShowList;
    }

    @Override
    public int getCount() {
        return mSearchShowList.size();
    }

    @Override
    public Object getItem(int i) {
        return mSearchShowList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return Long.valueOf(mSearchShowList.get(i).getIDs().getTracktId());
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
                mListener.onListItemSelected(true, position, false);
            }
        });
        String title = mSearchShowList.get(position).getTitle();
        String year = "Year: " + mSearchShowList.get(position).getYear();
        String imageUrl = null;
        if (mSearchShowList.get(position).getImageModel() != null && mSearchShowList.get(position).getImageModel().getPosterImageCategory() != null) {
            imageUrl = mSearchShowList.get(position).getImageModel().getPosterImageCategory().getThumbImage();
        }
        name.setText(title +" \n" + year);
        if (photo != null && imageUrl != null) {
            loadBitmap(imageUrl, photo);
        }
        return convertView;
    }

    public void loadBitmap(String imageUrl, ImageView imageView) {
        if (cancelPotentialWork(imageUrl, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imageUrl);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = "";

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            return downloadBitmap(data);
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
