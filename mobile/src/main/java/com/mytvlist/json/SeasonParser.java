package com.mytvlist.json;

import com.mytvlist.model.Episode;
import com.mytvlist.model.IDs;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.model.Season;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish on 9/8/15.
 */
public class SeasonParser {

    public ArrayList<Season> getSeasonShowList(JSONArray seasonJsonArray) {
        ArrayList<Season> mSeasonList = new ArrayList<>();
        JSONObject seasonJson;
        JSONObject idsJson;
        JSONObject imageJson;
        JSONArray episodeJSONArray;
        Season season;
        String number;
        IDs iDs;
        String rating;
        String votes;
        String episodeCount;
        String airedEpisodesCount;
        String overview;
        ArrayList<Episode> episodeList;
        ImagesModel imageModel;
        int noOfSeasons = seasonJsonArray.length();
        try {
            for (int i = 0; i < noOfSeasons; i++) {
                seasonJson = seasonJsonArray.getJSONObject(i);
                season = new Season();
                if (seasonJson.has(Utils.NUMBER)) {
                    number = seasonJson.getString(Utils.NUMBER);
                    season.setSeasonNumber(number);
                }
                if (seasonJson.has(Utils.IDS)) {
                    idsJson = seasonJson.getJSONObject(Utils.IDS);
                    iDs = new IDsParser().getIDs(idsJson);
                    season.setIDs(iDs);
                }
                if (seasonJson.has(Utils.RATING)) {
                    rating = seasonJson.getString(Utils.RATING);
                    season.setRating(rating);
                }
                if (seasonJson.has(Utils.VOTES)) {
                    votes = seasonJson.getString(Utils.VOTES);
                    season.setVoteCount(votes);
                }
                if (seasonJson.has(Utils.EPISODE_COUNT)) {
                    episodeCount = seasonJson.getString(Utils.EPISODE_COUNT);
                    season.setEpisodeCount(episodeCount);
                }
                if (seasonJson.has(Utils.AIRED_EPISODES)) {
                    airedEpisodesCount = seasonJson.getString(Utils.AIRED_EPISODES);
                    season.setAiredEpisodeCount(airedEpisodesCount);
                }
                if (seasonJson.has(Utils.OVERVIEW)) {
                    overview = seasonJson.getString(Utils.OVERVIEW);
                    season.setOverview(overview);
                }
                if (seasonJson.has(Utils.IMAGES)) {
                    imageJson = seasonJson.getJSONObject(Utils.IMAGES);
                    imageModel = new ImageParser().getImageModel(imageJson);
                    season.setImageModel(imageModel);
                }
                if (seasonJson.has(Utils.EPISODES)) {
                    episodeJSONArray = seasonJson.getJSONArray(Utils.EPISODES);
                    episodeList = new EpisodeParser().getEpisodeList(episodeJSONArray);
                    season.setEpisodeList(episodeList);
                }
                mSeasonList.add(season);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return mSeasonList;
    }
}
