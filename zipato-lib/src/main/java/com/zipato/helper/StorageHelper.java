/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.os.Environment;

/**
 * Created by murielK on 9/25/2014.
 */
public class StorageHelper {

    // Storage states
    private boolean externalStorageAvailable, externalStorageWritable;


    private void checkStorage() {
        // Get the external storage state
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
                // Storage is available and writable
                externalStorageAvailable = externalStorageWritable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                // Storage is only readable
                externalStorageAvailable = true;
                externalStorageWritable = false;
                break;
            default:
                // Storage is neither readable nor writable
                externalStorageAvailable = externalStorageWritable = false;
                break;
        }
    }


    public boolean isExternalStorageAvailable() {
        checkStorage();
        return externalStorageAvailable;
    }


    public boolean isExternalStorageWriteable() {
        checkStorage();
        return externalStorageWritable;
    }


    public boolean isESAvailableWritable() {
        checkStorage();
        return externalStorageAvailable && externalStorageWritable;
    }
}