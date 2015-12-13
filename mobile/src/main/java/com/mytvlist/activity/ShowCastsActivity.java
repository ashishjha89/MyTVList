package com.mytvlist.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mytvlist.MyTvListAnalyticsApplication;
import com.mytvlist.R;
import com.mytvlist.json.CastParser;
import com.mytvlist.list.ShowCastsAdapter;
import com.mytvlist.loaders.ContentLaoder;
import com.mytvlist.model.Cast;
import com.mytvlist.utils.ImageCaches;
import com.mytvlist.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish on 20/9/15.
 */
public class ShowCastsActivity extends Activity {

    private ProgressDialog mProgressDialog;

    private Context mContext;

    private String mTraktId;

    private String mThumbImageUrl;

    private ShowCastsAdapter mAdapter;

    private ListView mShowCastListView;

    private ImageView mCastListBackground;

    private Tracker mTracker;

    private static final String TAG = "ShowCastsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        mContext = this;
        mTraktId = getIntent().getStringExtra(Utils.TRAKT_ID);
        mThumbImageUrl = getIntent().getStringExtra(Utils.THUMB);
        setContentView(R.layout.show_casts_layout);
        mShowCastListView = (ListView) findViewById(R.id.show_casts_list);
        mCastListBackground = (ImageView) findViewById(R.id.cast_list_bg);
        new ShowCastFetcherTask().execute();

        // Obtain the shared Tracker instance.
        MyTvListAnalyticsApplication application = (MyTvListAnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, Utils.CAST_LIST_SCREEN);
        mTracker.setScreenName(Utils.CAST_LIST_SCREEN);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    class ShowCastFetcherTask extends AsyncTask<Void, Void, ArrayList<Cast>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(mContext, "Loading ...", "", true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<Cast> doInBackground(Void... voids) {
            Bitmap cachedPosterBitmap = ImageCaches.getPosterBitmapFromMemCache(mTraktId);
            if (cachedPosterBitmap == null) {
                loadCastBackgroundImage();
            } else {
                setCastListBackground(cachedPosterBitmap);
            }
            String requestUri = "https://api-v2launch.trakt.tv/shows/" + mTraktId + "/people?extended=images";
            String response = ContentLaoder.getContent(requestUri);
            JSONObject castsJson;
            try {
                castsJson = new JSONObject(response);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            ArrayList<Cast> castArrayList = new CastParser().getCastListParser(castsJson);
            return castArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Cast> casts) {
            super.onPostExecute(casts);
            if (casts == null) {
                finish();
            }
            mAdapter = new ShowCastsAdapter(mContext);
            mAdapter.setCastList(casts);
            mShowCastListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    private void loadCastBackgroundImage() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(mThumbImageUrl, bmOptions);
        if (bitmap != null) {
            setCastListBackground(bitmap);
        }
    }

    private void setCastListBackground(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        mCastListBackground.setBackground(drawable);
    }
}
