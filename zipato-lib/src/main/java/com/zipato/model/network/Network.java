/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.network;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.zipato.model.BaseEntityType;
import com.zipato.model.device.Device;

/**
 * Created by murielK on 5.6.2014..
 */
public class Network extends BaseEntityType {

    @JsonManagedReference
    private Device[] devices;

    @Override
    @JsonIgnore
    public Device[] getChildren() {
        return devices;
    }

    public Device[] getDevices() {
        return devices;
    }

    public void setDevices(Device[] devices) {
        this.devices = devices;
    }
}
