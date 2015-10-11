/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.event;

import com.zipato.model.event.ObjectItemsClick.FromTo;

/**
 * Created by murielK on 9/11/2015.
 */
public class ObjectListRefresh {

    public int position;
    public boolean reset;
    public int fromTo;

    public ObjectListRefresh(@FromTo int fromTo, int position, boolean reset) {
        this.fromTo = fromTo;
        this.position = position;
        this.reset = reset;
    }

    public ObjectListRefresh(@FromTo int fromTo) {
        this.fromTo = fromTo;
    }

}
