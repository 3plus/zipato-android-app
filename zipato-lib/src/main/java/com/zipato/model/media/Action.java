/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.media;

import com.zipato.model.DynaObject;

/**
 * Created by murielK on 3/6/2015.
 */
public class Action extends DynaObject {

    private Object[] fields;
    private Actions name;

    public Object[] getFields() {
        return fields;
    }

    public void setFields(Object[] fields) {
        this.fields = fields;
    }

    public Actions getName() {
        return name;
    }

    public void setName(Actions name) {
        this.name = name;
    }
}
