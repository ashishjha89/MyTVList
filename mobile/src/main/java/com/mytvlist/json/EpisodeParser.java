package com.mytvlist.json;

import com.mytvlist.model.Episode;
import com.mytvlist.model.IDs;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish on 9/8/15.
 */
public class EpisodeParser {

    public ArrayList<Episode> getEpisodeList(JSONArray episodeJsonArray) {
        ArrayList<Episode> episodeList = new ArrayList<>();
        JSONObject episodeJson;
        Episode episode;
        int noOfEpisode = episodeJsonArray.length();
        try {
            for (int i = 0; i < noOfEpisode; i++) {
                episodeJson = episodeJsonArray.getJSONObject(i);
                episode = getEpisode(episodeJson);
                episodeList.add(episode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return episodeList;
    }

    public Episode getEpisode(JSONObject episodeJson) {
        JSONObject idsJson;
        JSONObject imageJson;
        JSONObject screenshotJson;
        String seasonNumber;
        String episodeNumber;
        String title;
        IDs iDs;
        String rating;
        String votes;
        String firstAired;
        String lastUpdated;
        String overview;
        String thumbImageUrl;
        Episode episode = new Episode();
        try {
            if (episodeJson.has(Utils.SEASON)) {
                seasonNumber = episodeJson.getString(Utils.SEASON);
                episode.setSeasonNumber(seasonNumber);
            }
            if (episodeJson.has(Utils.NUMBER)) {
                episodeNumber = episodeJson.getString(Utils.NUMBER);
                episode.setEpisodeNumber(episodeNumber);
            }
            if (episodeJson.has(Utils.TITLE)) {
                title = episodeJson.getString(Utils.TITLE);
                episode.setTitle(title);
            }
            if (episodeJson.has(Utils.IDS)) {
                idsJson = episodeJson.getJSONObject(Utils.IDS);
                iDs = new IDsParser().getIDs(idsJson);
                episode.setIDs(iDs);
            }
            if (episodeJson.has(Utils.OVERVIEW)) {
                overview = episodeJson.getString(Utils.OVERVIEW);
                episode.setOverview(overview);
            }
            if (episodeJson.has(Utils.RATING)) {
                rating = episodeJson.getString(Utils.RATING);
                episode.setRating(rating);
            }
            if (episodeJson.has(Utils.VOTES)) {
                votes = episodeJson.getString(Utils.VOTES);
                episode.setVoteCount(votes);
            }
            if (episodeJson.has(Utils.FIRST_AIRED)) {
                firstAired = episodeJson.getString(Utils.FIRST_AIRED);
                episode.setFirstAiredTime(firstAired);
            }
            if (episodeJson.has(Utils.UPDATED_AT)) {
                lastUpdated = episodeJson.getString(Utils.UPDATED_AT);
                episode.setLastUpdatedTime(lastUpdated);
            }
            if (episodeJson.has(Utils.IMAGES)) {
                imageJson = episodeJson.getJSONObject(Utils.IMAGES);
                screenshotJson = imageJson.getJSONObject(Utils.SCREENSHOT);
                if (screenshotJson.has(Utils.THUMB)) {
                    thumbImageUrl = screenshotJson.getString(Utils.THUMB);
                    episode.setThumbImageUrl(thumbImageUrl);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return episode;
    }


}
