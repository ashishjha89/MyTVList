package com.mytvlist.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.mytvlist.R;
import com.mytvlist.contents.ShowsFragment;
import com.mytvlist.contents.ShowDetail;


/**
 * Created by ashish.jha on 7/3/2015.
 */
public class ShowsActivity extends Activity implements ShowsFragment.OnShowSelectedListener {

    private View mShowListView;

    private View mShowDetailView;

    private ShowsFragment mShowsFragment;

    private ShowDetail mShowDetailFragment;

    private FragmentManager mFragmentManager;

    private boolean mIsDetailMode = false;

    public static String SELECTED_TRAKT_ID = "";

    private final String TAG = "MyTVShowsActivity";

    public static final String FROM_ADD_SHOW = "from_add_show";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
       // overridePendingTransition(R.anim.slide_back, R.anim.nothing);
        setContentView(R.layout.my_shows);
        mShowListView = findViewById(R.id.myTVList);
        mShowDetailView = findViewById(R.id.showDetail);
        mFragmentManager = getFragmentManager();
        mShowsFragment = new ShowsFragment();
        mShowDetailFragment = new ShowDetail();
        initFragments();
    }

    private void initFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.myTVList, mShowsFragment);
        transaction.add(R.id.showDetail, mShowDetailFragment);
        transaction.detach(mShowDetailFragment);
        transaction.commit();
    }

    @Override
    public void initializeDetail(String traktId) {
        // DetailFragment should be attached. Don't detach ShowFragment yet so as to display both the fragments at the same time
        mIsDetailMode = true;
        mShowDetailView.setVisibility(View.VISIBLE);
        SELECTED_TRAKT_ID = traktId;
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.attach(mShowDetailFragment);
        transaction.commit();
        mShowDetailFragment.initDetailLayout(traktId);
    }

    @Override
    public void displayDetailInBackground(String traktId) {
        mShowDetailView.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayDetailComplete(String traktId) {
        // Detach ShowsFragment to show only DetailFragment
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.detach(mShowsFragment);
        transaction.commit();
    }

    @Override
    public void removeDetailPage() {
        // Detach DetailFragment. Possibly this Activity is launching another activity (AddSearchShowActivity),
        // and we don't want attached DetailFragment when we come back to this Activity.
        // We want DetailFragment to be attached only when we want Detail page to be visible
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.detach(mShowDetailFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        // Log.d(TAG, "onBackPressed() mIsDetailMode=" + mIsDetailMode);
        if (!mIsDetailMode) {
            // ShowFragment was visible - finish the Activity
            finish();
        } else {
            // Detail mode - Detail DetailFragment and Attach ShowFragment
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.detach(mShowDetailFragment);
            transaction.attach(mShowsFragment);
            transaction.commit();
            mIsDetailMode = false;
            mShowListView.setVisibility(View.VISIBLE);
            mShowDetailView.setVisibility(View.GONE);
        }
    }
}
