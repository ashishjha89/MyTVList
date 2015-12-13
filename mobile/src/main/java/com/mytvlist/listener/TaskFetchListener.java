package com.mytvlist.listener;

import com.mytvlist.utils.Utils;

/**
 * Created by ashish.jha on 7/9/2015.
 */
public interface TaskFetchListener {

    void setResult(Object result, Utils.CONTENT_TYPE_ENUM cType);
}
