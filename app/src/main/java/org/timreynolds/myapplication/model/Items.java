package org.timreynolds.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Items Model
 */
public class Items {

    @SerializedName("title")
    String title;

    @SerializedName("actor")
    Actor actor;

    @SerializedName("url")
    String url;

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public Actor getActor() {
        return actor;
    }
}