/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zipato.model.UUIDObject;

import java.util.UUID;

/**
 * Created by murielK on 6.6.2014..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceStateEvent implements UUIDObject {

    private UUID uuid;
    private DeviceState state;

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }
}
