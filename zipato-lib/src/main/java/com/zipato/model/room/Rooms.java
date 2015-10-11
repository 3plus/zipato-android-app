/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.room;

import com.zipato.model.BaseObject;
import com.zipato.model.IDObject;

/**
 * Created by murielK on 9/4/2014.
 */
public class Rooms extends BaseObject implements IDObject {

    private int id;
    private String stringUri;

    public String getStringUri() {
        return stringUri;
    }

    public void setStringUri(String stringUri) {
        this.stringUri = stringUri;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
