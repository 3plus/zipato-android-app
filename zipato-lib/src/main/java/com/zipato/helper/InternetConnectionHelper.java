/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

/**
 * Created by murielK on 10.6.2014..
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectionHelper {

    private final ConnectivityManager connectivity;

    public InternetConnectionHelper(Context context) {
        connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public boolean isOnline() {

        if (connectivity != null) {

            final NetworkInfo[] infos = connectivity.getAllNetworkInfo();
            if (infos != null) {
                for (NetworkInfo networkInfo : infos) {
                    if (networkInfo.getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }


    public boolean isConnectedWifi() {
        final NetworkInfo info = connectivity.getActiveNetworkInfo();
        return ((info != null) && info.isConnected() && (info.getType() == ConnectivityManager.TYPE_WIFI));
    }

    public boolean isConnectedMobile() {
        final NetworkInfo info = connectivity.getActiveNetworkInfo();
        return ((info != null) && info.isConnected() && (info.getType() == ConnectivityManager.TYPE_MOBILE));
    }
}
