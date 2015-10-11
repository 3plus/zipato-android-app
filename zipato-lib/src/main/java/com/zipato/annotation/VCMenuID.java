/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.zipato.appv2.activities.ShowVCMenu.SHOW_ID_ARCHIVES;
import static com.zipato.appv2.activities.ShowVCMenu.SHOW_ID_CHANGE_ICON;
import static com.zipato.appv2.activities.ShowVCMenu.SHOW_ID_CONFIG;
import static com.zipato.appv2.activities.ShowVCMenu.SHOW_ID_EVENT;
import static com.zipato.appv2.activities.ShowVCMenu.SHOW_ID_EVENT_SECURITY;
import static com.zipato.appv2.activities.ShowVCMenu.SHOW_ID_ZONES;

/**
 * Created by murielK on 9/29/2015.
 */
@Retention(RetentionPolicy.CLASS)
@IntDef({SHOW_ID_ZONES, SHOW_ID_EVENT_SECURITY, SHOW_ID_EVENT, SHOW_ID_CONFIG, SHOW_ID_CHANGE_ICON, SHOW_ID_ARCHIVES})
public @interface VCMenuID {
}
