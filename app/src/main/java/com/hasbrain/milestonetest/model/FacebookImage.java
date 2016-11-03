package com.hasbrain.milestonetest.model;

import java.io.Serializable;

/**
 * Created by Jupiter (vu.cao.duy@gmail.com) on 1/5/16.
 */
public class FacebookImage implements Serializable{
    private String id;
    private String name;
    private String imageUrl;
    private String thumbnailUrl;
    private String createdTime;
    private String fromUserName;
    private boolean bookmark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public void setBookmark(boolean bookmark){
        this.bookmark = bookmark;
    }

    public boolean isBookmark(){
        return bookmark;
    }
}
