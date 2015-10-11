/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import com.zipato.model.DynaObject;

/**
 * Created by murielK on 10/9/2014.
 */
public class AttributesLog extends DynaObject {

    private String prev;
    private String next;
    private AttrLogValue[] values;

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public AttrLogValue[] getValues() {
        return values;
    }

    public void setValues(AttrLogValue[] values) {
        this.values = values;
    }
}
