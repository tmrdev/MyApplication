package org.timreynolds.myapplication.utility;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

/*
 * Tim Reynolds
 *
 * PicassoCache - cache and configuration settings for image library
 */
public class PicassoCache {

    /**
     * Static Picasso Instance
     */
    private static Picasso picassoInstance = null;

    /**
     * PicassoCache Constructor
     *
     * @param context application Context
     */
    private PicassoCache(Context context) {

        Downloader downloader   = new OkHttpDownloader(context, Integer.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(downloader);
        // NOTE: enables Red for Network, Blue for Disk, Green for Memory
        builder.indicatorsEnabled(false);
        // Adding Error Listener
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Logger.i("picasso-image-error", " -> uri " + uri + " :: exc -> " + exception);
                exception.printStackTrace();
            }
        });
        picassoInstance = builder.build();
    }

    /**
     * Get Singleton Picasso Instance
     *
     * @param context application Context
     * @return Picasso instance
     */
    public static Picasso getPicassoInstance (Context context) {

        if (picassoInstance == null) {

            new PicassoCache(context);
            return picassoInstance;
        }

        return picassoInstance;
    }

}