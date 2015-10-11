/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;

/**
 * Created by murielK on 9/15/2015.
 */
public class ObjectConnectivity {

    public boolean isOnline;
    public Object object;

    public ObjectConnectivity(boolean isOnline, Object object) {
        this.isOnline = isOnline;
        this.object = object;
    }
}
