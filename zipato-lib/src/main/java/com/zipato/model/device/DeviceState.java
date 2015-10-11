/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.device;

import com.zipato.model.DynaObject;

import java.util.Date;

/**
 * Created by murielK on 4.6.2014..
 */
public class DeviceState extends DynaObject {
    public static String className = "DeviceState";
    private Date timestamp;
    private boolean trouble;
    private Date batteryTimestamp;
    private int batteryLevel;
    private boolean mainsPower;
    private boolean online;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isTrouble() {
        return trouble;
    }

    public void setTrouble(boolean trouble) {
        this.trouble = trouble;
    }

    public Date getBatteryTimestamp() {
        return batteryTimestamp;
    }

    public void setBatteryTimestamp(Date batteryTimestamp) {
        this.batteryTimestamp = batteryTimestamp;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public boolean isMainsPower() {
        return mainsPower;
    }

    public void setMainsPower(boolean mainsPower) {
        this.mainsPower = mainsPower;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
