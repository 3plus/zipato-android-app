/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import com.zipato.model.DynaObject;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

/**
 * Created by murielK on 10/9/2014.
 */
public class AttrLogValue extends DynaObject {

    private UUID uuid;
    private String nameEntity;
    private String attrName;
    private Date t;
    public static final Comparator<AttrLogValue> DATE_COMPARATOR = new Comparator<AttrLogValue>() {

        @Override
        public int compare(AttrLogValue i1, AttrLogValue i2) {

            return (i2.getT().getTime() < i1.getT().getTime()) ? -1 :
                    ((i2.getT().getTime() > i1.getT().getTime()) ? 1 : 0);
        }
    };
    private Object v;

    public static Comparator<AttrLogValue> getDateComparator() {
        return DATE_COMPARATOR;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getNameEntity() {
        return nameEntity;
    }

    public void setNameEntity(String nameEntity) {
        this.nameEntity = nameEntity;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public Date getT() {
        return t;
    }

    public void setT(Date t) {
        this.t = t;
    }

    public Object getV() {
        return v;
    }

    public void setV(Object v) {
        this.v = v;
    }
}
