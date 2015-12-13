package com.mytvlist.json;

import com.mytvlist.model.Cast;
import com.mytvlist.model.Person;
import com.mytvlist.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ashish on 20/9/15.
 */
public class CastParser {

    public ArrayList<Cast> getCastListParser(JSONObject showCastJson) {
        JSONArray castJsonArray;
        JSONObject castJson;
        Cast cast;
        ArrayList<Cast> castList = new ArrayList();
        try {
            if (showCastJson.has(Utils.CAST)) {
                castJsonArray = showCastJson.getJSONArray(Utils.CAST);
                for (int i = 0; i < castJsonArray.length(); i++) {
                    castJson = castJsonArray.getJSONObject(i);
                    cast = getCharacter(castJson);
                    castList.add(cast);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return castList;
        }

        return castList;
    }

    public Cast getCharacter(JSONObject characterJson) {
        Cast cast = new Cast();
        Person person;
        JSONObject personJson;
        String charecterName;
        try {
            if (characterJson.has(Utils.CHARACTER)) {
                charecterName = characterJson.getString(Utils.CHARACTER);
                cast.setCharecterName(charecterName);
            }
            if (characterJson.has(Utils.PERSON)) {
                personJson = characterJson.getJSONObject(Utils.PERSON);
                person = new PersonParser().getPersonModel(personJson);
                cast.setPerson(person);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return cast;
        }
        return cast;
    }
}
