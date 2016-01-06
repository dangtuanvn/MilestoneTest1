package com.hasbrain.milestonetest.model.converter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import com.hasbrain.milestonetest.model.FacebookImage;
import com.hasbrain.milestonetest.model.FacebookPhotoResponse;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/5/16.
 */
public class FacebookPhotoResponseDeserializer extends EasyDeserializer<FacebookPhotoResponse> {

    @Override
    public FacebookPhotoResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        FacebookPhotoResponse facebookPhotoResponse = null;
        if (json != null && json.isJsonObject()) {
            facebookPhotoResponse = new FacebookPhotoResponse();
            JsonObject jsonObject = json.getAsJsonObject();
            Type listFacebookImageType = new TypeToken<List<FacebookImage>>(){}.getType();
            facebookPhotoResponse.setData(
                    context.<List<FacebookImage>>deserialize(jsonObject.getAsJsonArray("data"), listFacebookImageType));
            JsonObject pagingJsonObject = jsonObject.getAsJsonObject("paging");
            JsonElement cursorJsonElement = pagingJsonObject.get("cursors");
            if (cursorJsonElement != null && cursorJsonElement.isJsonObject()) {
                JsonObject cursorsJsonObject = cursorJsonElement.getAsJsonObject();
                facebookPhotoResponse.setBefore(getStringValue(cursorsJsonObject.get("before"), null));
                facebookPhotoResponse.setAfter(getStringValue(cursorsJsonObject.get("after"), null));
            }
        }
        return facebookPhotoResponse;
    }
}
