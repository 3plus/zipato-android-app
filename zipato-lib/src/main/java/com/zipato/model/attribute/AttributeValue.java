/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import java.util.Date;

/**
 * Created by dbudor on 02/06/2014.
 */
public class AttributeValue {
    public static String className = "AttributeValue";
    Object value;
    Date timestamp;
    Object pendingValue;
    Date pendingTimestamp;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Object getPendingValue() {
        return pendingValue;
    }

    public void setPendingValue(Object pendingValue) {
        this.pendingValue = pendingValue;
    }

    public Date getPendingTimestamp() {
        return pendingTimestamp;
    }

    public void setPendingTimestamp(Date pendingTimestamp) {
        this.pendingTimestamp = pendingTimestamp;
    }
}
