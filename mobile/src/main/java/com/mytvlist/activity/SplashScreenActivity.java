package com.mytvlist.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

import com.mytvlist.R;
import com.mytvlist.database.TvListDataSource;
import com.mytvlist.service.PrefetchService;
import com.mytvlist.utils.Utils;

import java.io.File;

/**
 * Created by ashish.jha on 24/07/2015.
 */
/*
* Display Splash Screen
* Start Service to load banners for AddSearchShowActivity". The service will fetch banners from http. Save them to file. Prepares model to have their im memory copy.
* Start Service to show "INTERESTING_CONTENTS" in AddSearchShowActivity
* Start Service to load "Poster" images for "MyShowActivity" (also used in "ShowDetailActivity")
* */
public class SplashScreenActivity extends Activity {

    private boolean mIsCleanApp = true;

    private static final String TAG = "SplashScreenActivity";

    private Intent mPrefetchIntent;

    private ProgressDialog mLoadingProgressDialog;

    private BroadcastReceiver mInterestingShowAdditionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.d(TAG, "onReceive()");
            if (intent.getAction() != null && intent.getAction().equals(Utils.LOAD_INTERESTING_SHOWS)) {
                if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing()) {
                    mLoadingProgressDialog.dismiss();
                    launchAddShowActivity();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsCleanLaunch();
        mPrefetchIntent = new Intent(this, PrefetchService.class);
        // Log.d(TAG, "onCreate() mIsCleanApp=" + mIsCleanApp);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (mIsCleanApp) {
            clearDB();
            File localStorageDir = getFilesDir();
            deleteFiles(localStorageDir);
            setContentView(R.layout.splash_screen_layout);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Utils.LOAD_INTERESTING_SHOWS);
            LocalBroadcastManager.getInstance(this).registerReceiver(mInterestingShowAdditionReceiver, filter);

            mLoadingProgressDialog = ProgressDialog.show(this, "Loading Shows ...", "", true);
            mLoadingProgressDialog.setCancelable(true);
            mLoadingProgressDialog.show();

            initPrefetchInterestingShows();
        } else {
            launchMyShowActivity();
            initPrefetchInterestingShows();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mInterestingShowAdditionReceiver);
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

    private void checkIsCleanLaunch() {
        SharedPreferences prefs = getSharedPreferences(Utils.MY_TV_LIST_PREFS, MODE_PRIVATE);
        mIsCleanApp = prefs.getBoolean(Utils.IS_CLEAN_LAUNCH, true);
    }

    private void launchAddShowActivity() {
        Intent intent = new Intent(this, AddShowActivity.class);
        intent.putExtra(Utils.IS_CALLED_FROM_SPLASH, true);
        startActivity(intent);
        finish();
    }

    private void launchMyShowActivity() {
        //Intent intent = new Intent(this, MyShowActivity.class);
        Intent intent = new Intent(this, ShowsActivity.class);
        intent.putExtra(Utils.IS_CALLED_FROM_SPLASH, true);
        startActivity(intent);
    }

    private void initPrefetchInterestingShows() {
        // Log.d(TAG, "initPrefetchInterestingShows()");
        mPrefetchIntent.setAction(Utils.LOAD_INTERESTING_SHOWS);
        startService(mPrefetchIntent);
    }

    private void clearDB() {
        TvListDataSource tvListDataSource = new TvListDataSource(this);
        tvListDataSource.open();
        tvListDataSource.deleteFromTable(Utils.TVLIST_SHOW_TABLE, null, null);
        tvListDataSource.deleteFromTable(Utils.TVLIST_SEASON_TABLE, null, null);
        tvListDataSource.close();
    }

    private void deleteFiles(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteFiles(child);
            }
        }
        fileOrDirectory.delete();
    }
}
