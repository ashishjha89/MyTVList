package com.mytvlist.json;

import com.mytvlist.model.IDs;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.model.Show;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class ShowParser {

    public Show getShow(JSONObject showJson) {
        Show show;
        try {
            String title = showJson.getString(Utils.TITLE);
            String year = showJson.getString(Utils.YEAR);
            JSONObject idJson = showJson.getJSONObject(Utils.IDS);
            IDs ids = new IDsParser().getIDs(idJson);
            if (ids == null) {
                return null;
            }
            show = new Show(title, year, ids);
            if (showJson.has(Utils.IMAGES)) {
                JSONObject imageJson = showJson.getJSONObject(Utils.IMAGES);
                ImagesModel imageModel = new ImageParser().getImageModel(imageJson);
                show.setImageModel(imageModel);
            }
            if (showJson.has(Utils.OVERVIEW)) {
                show.setOverview(showJson.getString(Utils.OVERVIEW));
            }
            if (showJson.has(Utils.FIRST_AIRED)) {
                show.setFirstAired(showJson.getString(Utils.FIRST_AIRED));
            }
            if (showJson.has(Utils.AIRS)) {
                JSONObject airsJson = showJson.getJSONObject(Utils.AIRS);
                show.setAirsDay(airsJson.getString(Utils.AIRS_DAY));
                show.setAirsTime(airsJson.getString(Utils.AIRS_TIME));
                show.setAirsTimeZone(airsJson.getString(Utils.AIRS_TIMEZONE));
            }
            if (showJson.has(Utils.RUNTINME)) {
                show.setRuntime(showJson.getString(Utils.RUNTINME));
            }
            if (showJson.has(Utils.NETWORK)) {
                show.setNetwork(showJson.getString(Utils.NETWORK));
            }
            if (showJson.has(Utils.COUNTRY)) {
                show.setCountry(showJson.getString(Utils.COUNTRY));
            }
            if (showJson.has(Utils.UPDATED_AT)) {
                show.setUpdatedAt(showJson.getString(Utils.UPDATED_AT));
            }
            if (showJson.has(Utils.TRAILER)) {
                show.setTrailer(showJson.getString(Utils.TRAILER));
            }
            if (showJson.has(Utils.HOMEPAGE)) {
                show.setHomepage(showJson.getString(Utils.HOMEPAGE));
            }
            if (showJson.has(Utils.STATUS)) {
                show.setStatus(showJson.getString(Utils.STATUS));
            }
            if (showJson.has(Utils.GENRES)) {
                JSONArray jsonGenreArray = showJson.getJSONArray(Utils.GENRES);
                int len = jsonGenreArray.length();
                String[] genreStringArray = new String[len];
                for (int i = 0; i < len; i++) {
                    genreStringArray[i] = jsonGenreArray.getString(i);
                }
                show.setGenresArray(genreStringArray);
            }
            if (showJson.has(Utils.STATUS)) {
                show.setStatus(showJson.getString(Utils.STATUS));
            }
            if (showJson.has(Utils.AIRED_EPISODES)) {
                show.setAiredEpisodes(showJson.getString(Utils.AIRED_EPISODES));
            }
            if (showJson.has(Utils.TRAKT_RATING)) {
                show.setTraktRating(showJson.getString(Utils.TRAKT_RATING));
            }
            return show;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
