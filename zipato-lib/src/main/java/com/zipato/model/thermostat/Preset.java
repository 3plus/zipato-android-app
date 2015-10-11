/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.thermostat;

import com.zipato.model.DynaObject;

import java.util.HashMap;

/**
 * Created by murielK on 7/24/2014.
 */
public class Preset extends DynaObject {

    private String name;
    private HashMap<EnumOperation, ThermosTarget> map = new HashMap<EnumOperation, ThermosTarget>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<EnumOperation, ThermosTarget> getMap() {
        return map;
    }

    public void setMap(HashMap<EnumOperation, ThermosTarget> map) {
        this.map = map;
    }
}