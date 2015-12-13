package com.mytvlist.list;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mytvlist.R;
import com.mytvlist.model.Cast;
import com.mytvlist.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ashish on 20/9/15.
 */
public class ShowCastsAdapter extends BaseAdapter {

    private Context mContext;

    private ArrayList<Cast> mCastList;

    private Bitmap mPlaceHolderBitmap;

    private static final String TAG = "ShowCastsAdapter";

    public ShowCastsAdapter(Context context) {
        mContext = context;
    }

    public void setCastList(ArrayList<Cast> castList) {
        mCastList = castList;
        mPlaceHolderBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person_icon);
    }

    @Override
    public int getCount() {
        return mCastList.size();
    }

    @Override
    public Object getItem(int i) {
        return mCastList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Log.d(TAG, "getView() position=" + position);
        TextView castNameTV, personNameTV;
        ImageView photoView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cast_item, null);
        }
        castNameTV = (TextView) convertView.findViewById(R.id.cast_name_text);
        personNameTV = (TextView) convertView.findViewById(R.id.person_name_text);
        photoView = (ImageView) convertView.findViewById(R.id.cast_photo);
        String personName = mCastList.get(position).getPerson().getPersonName();
        String castName = mCastList.get(position).getCharecterName();
        final String imageUrl = mCastList.get(position).getPerson().getThumbImageUri();
        // Log.d(TAG, "getView() position=" + position +" personName="+personName+" castName="+castName+" imageUrl="+imageUrl);
        castNameTV.setText(castName);
        personNameTV.setText(personName);
        if (photoView != null && imageUrl != null) {
            loadBitmap(imageUrl, photoView);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imdbId = mCastList.get(position).getPerson().getIDs().getImdb();
                if (imdbId == null || imdbId.isEmpty() || imdbId.equalsIgnoreCase("null")) {
                    return;
                } else {
                    String url = "http://www.imdb.com/name/" + imdbId;
                    // Log.d(TAG, "url="+url);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("imdb"));
                    i.setData(Uri.parse(url));
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.addCategory(Intent.CATEGORY_BROWSABLE);
                    mContext.startActivity(i);
                }
            }
        });
        return convertView;
    }

    public void loadBitmap(String imageUrl, ImageView imageView) {
        if (cancelPotentialWork(imageUrl, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imageUrl);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String imageUrl = "";

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bm = downloadBitmap(imageUrl);
            return bm;
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
            final String bitmapData = bitmapWorkerTask.imageUrl;
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
