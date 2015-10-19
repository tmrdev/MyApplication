package org.timreynolds.myapplication.api;

import org.timreynolds.myapplication.model.ResponseData;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Tim Reynolds
 */
public class ApiManager {

    //https://www.googleapis.com/plus/v1/activities?maxResults=10&query=trending&orderBy=best&key=
    private static final String API_URL = "https://www.googleapis.com/plus/v1/";

    public interface GooglePlusInterface {
        @GET("/activities")
        // asynchronous execution with callback as last parameter
        void getData(@Query("query") String postType, @Query("key") String apiKey, @Query("maxResults") int limitValue, @Query("orderBy") String orderBy, Callback<ResponseData> callback);
    }

    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint(API_URL)
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build();

    private static final GooglePlusInterface TUMBLR_SERVICE = REST_ADAPTER.create(GooglePlusInterface.class);

    public static GooglePlusInterface getService() {
        return TUMBLR_SERVICE;
    }
}
