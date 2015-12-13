package com.mytvlist.json;

import com.mytvlist.model.IDs;
import com.mytvlist.model.Person;
import com.mytvlist.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ashish on 20/9/15.
 */
public class PersonParser {

    public Person getPersonModel(JSONObject personJson) {
        Person person = new Person();
        JSONObject imagesJson, headshotJson, idJson;
        IDs ids;
        try {
            if (personJson.has(Utils.NAME)) {
                person.setPersonName(personJson.getString(Utils.NAME));
            }
            if (personJson.has(Utils.IMAGES)) {
                imagesJson = personJson.getJSONObject(Utils.IMAGES);
                if (imagesJson.has(Utils.HEADSHOT)) {
                    headshotJson = imagesJson.getJSONObject(Utils.HEADSHOT);
                    if (headshotJson.has(Utils.THUMB)) {
                        person.setThumbImageUri(headshotJson.getString(Utils.THUMB));
                    }
                }
            }

            if (personJson.has(Utils.IDS)) {
                idJson = personJson.getJSONObject(Utils.IDS);
                ids = new IDsParser().getIDs(idJson);
                person.setIDs(ids);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return person;
    }
}
