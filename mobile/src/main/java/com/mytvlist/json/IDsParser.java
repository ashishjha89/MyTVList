package com.mytvlist.json;

import com.mytvlist.model.IDs;
import com.mytvlist.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ashish.jha on 7/3/2015.
 */
public class IDsParser {

    public IDs getIDs(JSONObject idJson) {
        IDs ids = new IDs();
        try {
            if (idJson.has(Utils.TRAKT_ID)) {
                String trakt = idJson.getString(Utils.TRAKT_ID);
                ids.setTracktId(trakt);
            }
            if (idJson.has(Utils.SLUG)) {
                String slug = idJson.getString(Utils.SLUG);
                ids.setSlug(slug);
            }
            if (idJson.has(Utils.TVDB_ID)) {
                String tvdb = idJson.getString(Utils.TVDB_ID);
                ids.setTvdb(tvdb);
            }
            if (idJson.has(Utils.IMDB_ID)) {
                String imdb = idJson.getString(Utils.IMDB_ID);
                ids.setImdb(imdb);
            }
            if (idJson.has(Utils.TMDB_ID)) {
                String tmdb = idJson.getString(Utils.TMDB_ID);
                ids.setTmDb(tmdb);
            }
            if (idJson.has(Utils.TVRAGE)) {
                String tvRage = idJson.getString(Utils.TVRAGE);
                ids.setTvrage(tvRage);
            }
            return ids;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
