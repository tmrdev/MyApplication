package org.timreynolds.myapplication;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * MyApplication - create app instance for global access
 */
public class MyApplication extends Application {

    private static MyApplication sInstance;

    /**
     * onCreate
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    /**
     * isConnected - check if there is a network connection
     *
     * @return boolean
     */
    public static boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * MyApplication getInstance
     *
     * @return sInstance
     */
    public synchronized static MyApplication getInstance() {
        return sInstance;
    }


}
