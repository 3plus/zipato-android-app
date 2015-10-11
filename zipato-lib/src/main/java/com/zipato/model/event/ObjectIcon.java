/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by murielK on 9/29/2015.
 */
public class ObjectIcon {

    public static final int TYPE_ICON = 0;
    public static final int TYPE_COLOR = 1;

    public Object value;
    public int kkEventType;

    public ObjectIcon(@KKEventType int kkTypeEvent, Object value) {
        this.value = value;
        kkEventType = kkTypeEvent;
    }

    @Retention(RetentionPolicy.CLASS)
    @IntDef({TYPE_COLOR, TYPE_ICON})
    public @interface KKEventType {
    }
}
