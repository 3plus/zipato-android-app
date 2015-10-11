/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dbudor on 07/06/2014.
 */
public class DynaObject {

    protected transient Map<String, Object> data;

    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }

    @JsonIgnore
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @JsonAnySetter
    public void put(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }
}
