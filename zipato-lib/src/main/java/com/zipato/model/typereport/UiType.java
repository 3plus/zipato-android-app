/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.typereport;

import com.zipato.model.DynaObject;

import java.util.Comparator;

/**
 * Created by murielK on 2.6.2014..
 */
public class UiType extends DynaObject {

    private String link;
    private String name;
    public static final Comparator<UiType> NAME_COMPARATOR = new Comparator<UiType>() {
        @Override
        public int compare(UiType i1, UiType i2) {

            return i1.getName().compareTo(i2.getName());
        }
    };
    private String endpointType;
    private String relativeUrl;

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        UiType uiType = (UiType) o;
        if (!name.equals(uiType.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }
}
