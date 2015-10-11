/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;

/**
 * Created by murielK on 10/6/2015.
 */
public class ObjectBonjour {

    public static final int ADD = 0;
    public static final int REMOVE = 1;

    public int eventType;
    public Object info;

    public ObjectBonjour(int eventType, Object info) {
        this.eventType = eventType;
        this.info = info;
    }
}
