/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.DynaObject;

import java.util.UUID;

/**
 * Created by murielK on 3/12/2015.
 */
public class ZoneConfig extends DynaObject {

    private String type;
    private UUID endpoint;
    private String armMode;
    private UUID attributeUuid;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(UUID endpoint) {
        this.endpoint = endpoint;
    }

    public String getArmMode() {
        return armMode;
    }

    public void setArmMode(String armMode) {
        this.armMode = armMode;
    }

    public UUID getAttributeUuid() {
        return attributeUuid;
    }

    public void setAttributeUuid(UUID attributeUuid) {
        this.attributeUuid = attributeUuid;
    }
}
