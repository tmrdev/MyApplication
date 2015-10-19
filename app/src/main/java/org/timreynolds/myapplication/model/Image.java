package org.timreynolds.myapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Image Model
 */
public class Image {

    @SerializedName("url")
    String url;

    public String getUrl() { return  url; }
}
