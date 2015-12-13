package com.mytvlist.tasks;

import android.os.AsyncTask;

import com.mytvlist.listener.TaskFetchListener;
import com.mytvlist.loaders.ContentLaoder;
import com.mytvlist.utils.Utils;

/**
 * Created by ashish.jha on 7/11/2015.
 */
public class ShowFetcherTask extends AsyncTask<String, Void, String> {

    private TaskFetchListener mTaskFetchListener;

    private Utils.CONTENT_TYPE_ENUM mShowMode;

    public ShowFetcherTask(TaskFetchListener taskFetchListener, Utils.CONTENT_TYPE_ENUM showMode) {
        mTaskFetchListener = taskFetchListener;
        mShowMode = showMode;
    }

    @Override
    protected String doInBackground(String... traktUrls) {
        return ContentLaoder.getContent(traktUrls[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mTaskFetchListener.setResult(result, mShowMode);
    }
}
