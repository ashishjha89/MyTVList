package com.mytvlist.json;

import com.mytvlist.model.Show;
import com.mytvlist.model.TrendingShow;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish.jha on 7/3/2015.
 */

public class TrendingShowsParser {

    public ArrayList<Show> getTrendingShowList(JSONArray trendingShowsJsonArray) {
        ArrayList<Show> trendingShowList = new ArrayList<>();
        JSONObject trendingShowJson;
        TrendingShow trendingShow;
        JSONObject showJson;
        Show showObj;
        String watchers;
        try {
            int noOfEntries = trendingShowsJsonArray.length();
            for (int i = 0; i < noOfEntries; i++) {
                trendingShowJson = trendingShowsJsonArray.getJSONObject(i);
                watchers = trendingShowJson.getString(Utils.WATCHERS);
                showJson = trendingShowJson.getJSONObject(Utils.SHOW);
                showObj = new ShowParser().getShow(showJson);
                if(showObj == null){
                    return null;
                }
                trendingShow = new TrendingShow(watchers, showObj.getTitle(), showObj.getYear(), showObj.getIDs());
                trendingShow.setImageModel(showObj.getImageModel());
                trendingShowList.add(trendingShow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return trendingShowList;
    }



}
