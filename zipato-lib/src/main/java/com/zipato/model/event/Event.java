/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by murielK on 8/12/2015.
 */
public class Event {

    public static final int EVENT_TYPE_SECURITY = 0;
    public static final int EVENT_TYPE_ITEM_CLICK = 1;
    public static final int EVENT_TYPE_LAUNCHER = 2;
    public static final int EVENT_TYPE_REFRESH_REQUEST = 3;
    public static final int EVENT_TYPE_REPO_SYNCED = 4;
    public static final int EVENT_TYPE_REBUILD_REQUEST = 5;
    public static final int EVENT_TYPE_SCENE_CONTROLLER = 6;
    public static final int EVENT_TYPE_ON_BOX_CHANGE = 7;
    public static final int EVENT_TYPE_CONNECTIVITY_EVENT = 8;
    public static final int EVENT_TYPE_TYPE_MENU = 9;
    public static final int EVENT_TYPE_LIST_VIEW_REFRESH = 10;
    public static final int EVENT_TYPE_AUTO_UPDATER_SERVICE = 11;
    public static final int EVENT_TYPE_DIS_REFRESH_TITLE = 12;
    public static final int EVENT_TYPE_KITKAT_ICON = 13;
    public static final int EVENT_TYPE_BONJOUR_EVENT = 14;
    public static final int EVENT_TYPE_ENBALE_SWIPE_VIEW_PAGER = 15;
    public static final int EVENT_TYPE_HIDE_ADD_NEW_ON_SCENES = 16;

    public int eventType;
    public Object eventObject;

    public Event(Object eventObject, @EventType int eventType) {
        this.eventObject = eventObject;
        this.eventType = eventType;
    }

    @Retention(RetentionPolicy.CLASS)
    @IntDef({EVENT_TYPE_SECURITY, EVENT_TYPE_ITEM_CLICK,
            EVENT_TYPE_LAUNCHER, EVENT_TYPE_REFRESH_REQUEST,
            EVENT_TYPE_ON_BOX_CHANGE, EVENT_TYPE_KITKAT_ICON,
            EVENT_TYPE_CONNECTIVITY_EVENT, EVENT_TYPE_TYPE_MENU,
            EVENT_TYPE_REPO_SYNCED, EVENT_TYPE_REBUILD_REQUEST,
            EVENT_TYPE_SCENE_CONTROLLER, EVENT_TYPE_LIST_VIEW_REFRESH,
            EVENT_TYPE_AUTO_UPDATER_SERVICE, EVENT_TYPE_DIS_REFRESH_TITLE,
            EVENT_TYPE_BONJOUR_EVENT, EVENT_TYPE_ENBALE_SWIPE_VIEW_PAGER,
            EVENT_TYPE_HIDE_ADD_NEW_ON_SCENES})
    public @interface EventType {
    }

}
