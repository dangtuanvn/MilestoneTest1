package com.hasbrain.milestonetest.model.converter;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * Easy serializer that includes multiple methods to get primitive value easier.
 * Created by Jupiter (vu.cao.duy@gmail.com) on 10/29/14.
 */
public abstract class EasyDeserializer<T> implements JsonDeserializer<T> {

    /**
     * Get bool value of a json element.
     * @param jsonElement
     * @param defaultValue
     * @return
     */
    protected boolean getBooleanValue(JsonElement jsonElement, boolean defaultValue) {
        boolean returnedValue = defaultValue;
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            returnedValue = jsonElement.getAsBoolean();
        }
        return returnedValue;
    }

    /**
     * Get int value of a json element.
     * @param jsonElement
     * @param defaultValue
     * @return
     */
    protected int getIntValue(JsonElement jsonElement, int defaultValue) {
        int returnedValue = defaultValue;
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            returnedValue = jsonElement.getAsInt();
        }
        return returnedValue;
    }

    /**
     * Get long value from json element.
     * @param jsonElement
     * @param defaultValue
     * @return
     */
    public long getLongValue(JsonElement jsonElement, long defaultValue) {
        long returnedValue = defaultValue;
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            returnedValue = jsonElement.getAsLong();
        }
        return returnedValue;
    }

    /**
     * Get String value from json element.
     * @param jsonElement
     * @param defaultValue
     * @return
     */
    protected String getStringValue(JsonElement jsonElement, String defaultValue) {
        String returnedValue = defaultValue;
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            returnedValue = jsonElement.getAsString();
        }
        return returnedValue;
    }

    /**
     * Get double value from json element.
     * @param jsonElement
     * @param defaultValue
     * @return
     */
    protected double getDoubleValue(JsonElement jsonElement, double defaultValue) {
        double returnedValue = defaultValue;
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            returnedValue = jsonElement.getAsDouble();
        }
        return returnedValue;
    }
}
