package com.mytvlist.json;

import com.mytvlist.model.ImageCategory;
import com.mytvlist.model.ImagesModel;
import com.mytvlist.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ashish.jha on 7/10/2015.
 */
public class ImageParser {

    public ImagesModel getImageModel(JSONObject imageJson) {
        ImagesModel imageModel = new ImagesModel();
        try {
            if (imageJson.has(Utils.FANART)) {
                JSONObject fanartJson = imageJson.getJSONObject(Utils.FANART);
                ImageCategory fanartImage = new ImageCategoryParser().getImageCategory(fanartJson);
                imageModel.setFanartImageCategory(fanartImage);
            }
            if (imageJson.has(Utils.POSTER)) {
                JSONObject posterJson = imageJson.getJSONObject(Utils.POSTER);
                ImageCategory posterImage = new ImageCategoryParser().getImageCategory(posterJson);
                imageModel.setPosterImageCategory(posterImage);
            }
            if (imageJson.has(Utils.LOGO)) {
                JSONObject logoJson = imageJson.getJSONObject(Utils.LOGO);
                ImageCategory logoImage = new ImageCategoryParser().getImageCategory(logoJson);
                imageModel.setLogoImageCategory(logoImage);
            }
            if (imageJson.has(Utils.CLEARART)) {
                JSONObject clearartJson = imageJson.getJSONObject(Utils.CLEARART);
                ImageCategory clearartImage = new ImageCategoryParser().getImageCategory(clearartJson);
                imageModel.setClearartImageCategory(clearartImage);
            }
            if (imageJson.has(Utils.BANNER)) {
                JSONObject bannerJson = imageJson.getJSONObject(Utils.BANNER);
                ImageCategory bannerImage = new ImageCategoryParser().getImageCategory(bannerJson);
                imageModel.setBannerImageCategory(bannerImage);
            }
            if (imageJson.has(Utils.THUMB)) {
                JSONObject thumbJson = imageJson.getJSONObject(Utils.THUMB);
                ImageCategory thumbImage = new ImageCategoryParser().getImageCategory(thumbJson);
                imageModel.setThumbImageCategory(thumbImage);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return imageModel;
    }

    class ImageCategoryParser {
        public ImageCategory getImageCategory(JSONObject json) {
            ImageCategory imageCategory = new ImageCategory();
            try {
                if (json.has(Utils.FULL)) {
                    String fullUrl = json.getString(Utils.FULL);
                    imageCategory.setFullImages(fullUrl);
                }
                if (json.has(Utils.MEDIUM)) {
                    String mediumUrl = json.getString(Utils.MEDIUM);
                    imageCategory.setMediumImages(mediumUrl);
                }
                if (json.has(Utils.THUMB)) {
                    String thumbUrl = json.getString(Utils.THUMB);
                    imageCategory.setThumbImages(thumbUrl);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return imageCategory;
        }
    }
}
