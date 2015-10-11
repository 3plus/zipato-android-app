/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by murielK on 9/15/2015.
 */
public class ObjectLauncher {

    public static final int LAUNCH_ACTIVITY = 1;
    public static final int LAUNCH_SCENES = 2;
    public static final int LAUNCH_FRAGMENT = 3;
    public static final int LAUNCH_LOG_OUT = 4;

    public Class<?> clzz;
    public int launchType;
    public Object object;

    public ObjectLauncher(@LaunchType int launchType, Class<?> clzz, Object object) {
        this.clzz = clzz;
        this.launchType = launchType;
        this.object = object;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LAUNCH_ACTIVITY, LAUNCH_SCENES, LAUNCH_FRAGMENT, LAUNCH_LOG_OUT})
    public @interface LaunchType {
    }
}
