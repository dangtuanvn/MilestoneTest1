package com.hasbrain.milestonetest.model;

import java.util.List;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/5/16.
 */
public class FacebookPhotoResponse {
    private List<FacebookImage> data;
    private String before;
    private String after;

    public List<FacebookImage> getData() {
        return data;
    }

    public void setData(List<FacebookImage> data) {
        this.data = data;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }
}
