/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Looper;

/**
 * Created by murielK on 8/19/2015.
 */
public final class Utils {

    private Utils() {
    }


    public static String getHexForKitKat(String in) {

        return String.valueOf(Character.toChars(Integer.parseInt(in, 16)));
    }

    public static String capitalizer(final String input) {
        if ((input == null) || (input.isEmpty()))
            return "";

        if (input.length() == 1)
            return input.toUpperCase();

        return (input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase());
    }

    public static void checkNotMain() { //copied from Picasso utils :)
        if (isMain()) {
            throw new IllegalStateException("Method call should not happen from the main thread.");
        }
    }

    public static void checkMain() { //copied from Picasso utils :)
        if (!isMain()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }
    }

    public static boolean isMain() { //copied from Picasso utils :)
        return Looper.getMainLooper().getThread() == Thread.currentThread(); // yes equal should work as the currentThread should point to the mainUiThread
    }

    public static boolean isPreJellyBean() {
        return (VERSION.SDK_INT < VERSION_CODES.JELLY_BEAN);
    }
}
