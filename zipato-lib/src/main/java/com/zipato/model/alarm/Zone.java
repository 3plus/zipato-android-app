/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.BaseObject;
import com.zipato.model.device.Device;

/**
 * Created by murielK on 8/26/2014.
 */
public class Zone extends BaseObject {

    private ZoneState zoneState = new ZoneState();
    private Device device;
    private ZoneConfig config;

    public ZoneConfig getConfig() {
        return config;
    }

    public void setConfig(ZoneConfig config) {
        this.config = config;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public ZoneState getZoneState() {
        return zoneState;
    }

    public void setZoneState(ZoneState zoneState) {
        this.zoneState = zoneState;
    }
}
