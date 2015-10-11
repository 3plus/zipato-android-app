/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

/**
 * Created by murielK on 8/11/2015.
 */
public final class TagFactoryUtils {

    private static final String TAG_PREFIX = "Zipato_";

    private TagFactoryUtils() {
    }

    public static String getTag(final Object obj) {
        final Class cls = obj.getClass();
        return getTag(cls.getSimpleName());

    }

    public static String getTag(final Class cls) {
        return getTag(cls.getSimpleName());

    }

    public static String getTag(final String suffix) {
        return TAG_PREFIX + suffix;
    }
}