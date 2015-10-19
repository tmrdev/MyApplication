package org.timreynolds.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit.client.Response;

/**
 * ResponseData - main json node off of Search Results
 */
public class ResponseData {
    @SerializedName("title")
    String title;

    @SerializedName("items")
    public List<Items> items;

    public String getTitle() {
        return title;
    }

    public List<Items> getItems() {
        return items;
    }

}
