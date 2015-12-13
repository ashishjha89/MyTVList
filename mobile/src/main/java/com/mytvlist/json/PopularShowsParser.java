package com.mytvlist.json;

import com.mytvlist.model.PopularShow;
import com.mytvlist.model.Show;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish.jha on 7/3/2015.
 */

public class PopularShowsParser {

    public ArrayList<Show> getPopularShowList(JSONArray popularShowsJsonArray) {
        ArrayList<Show> popularShowList = new ArrayList<>();
        JSONObject popularShowJson;
        PopularShow popularShow;
        Show showObj;
        try {
            int noOfEntries = popularShowsJsonArray.length();
            for (int i = 0; i < noOfEntries; i++) {
                popularShowJson = popularShowsJsonArray.getJSONObject(i);
                showObj = new ShowParser().getShow(popularShowJson);

                if (showObj == null) {
                    return null;
                }
                popularShow = new PopularShow(showObj.getTitle(), showObj.getYear(), showObj.getIDs());
                popularShow.setImageModel(showObj.getImageModel());

                popularShowList.add(popularShow);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return popularShowList;
    }
}
