/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by murielK on 9/11/2015.
 */
public class ObjectItemsClick {

    public static final int UI_TYPE = 1;
    public static final int ROOM_TYPES = 2;
    public static final int ROOMS = 3;
    public static final int D_MANAGER = 4;
    public static final int SCENES = 5;
    public static final int SCENES_TYPE = 6;
    public static final int FAVORITE_BUTTON = 7;
    public int position;
    public int fromTo;

    public ObjectItemsClick(@FromTo int fromTo, int position) {
        this.fromTo = fromTo;
        this.position = position;
    }

    public ObjectItemsClick(int fromTo) {
        this.fromTo = fromTo;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UI_TYPE, ROOM_TYPES, ROOMS, D_MANAGER, SCENES,
            SCENES_TYPE, FAVORITE_BUTTON})
    public @interface FromTo {
    }
}
