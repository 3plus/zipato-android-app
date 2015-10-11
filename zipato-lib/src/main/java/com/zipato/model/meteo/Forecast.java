/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.meteo;

import android.util.SparseIntArray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zipato.model.BaseObject;
import com.zipato.model.attribute.Attribute;

import java.util.Date;

/**
 * Created by murielK on 11/25/2014.
 */
public class Forecast extends BaseObject {
    Attribute[] attributes;
    @JsonIgnore
    SparseIntArray map;
    @JsonIgnore
    Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SparseIntArray getMap() {
        if (map == null)
            map = new SparseIntArray();
        return map;
    }

    public void setMap(SparseIntArray map) {
        this.map = map;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }
}
