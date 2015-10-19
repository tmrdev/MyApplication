package org.timreynolds.myapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Actor model
 */
public class Actor {

    @SerializedName("image")
    Image image;

    public Image getImage() {
        return image;
    }
}
