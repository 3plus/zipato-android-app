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
public class ObjectMenu {

    public static final int MENU_MAIN = 0;
    public static final int MENU_BOX_INFO = 1;
    public static final int MENU_SETTINGS = 2;

    public int menuType;
    public Object object;

    public ObjectMenu(@MenuType int menuType, Object object) {
        this.menuType = menuType;
        this.object = object;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MENU_MAIN, MENU_BOX_INFO, MENU_SETTINGS})
    public @interface MenuType {
    }
}
