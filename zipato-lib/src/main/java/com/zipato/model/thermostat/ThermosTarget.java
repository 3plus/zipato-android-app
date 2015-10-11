/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.thermostat;

import com.zipato.model.DynaObject;

/**
 * Created by murielK on 7/24/2014.
 */
public class ThermosTarget extends DynaObject {
    String target;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}