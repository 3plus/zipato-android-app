/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zipato.model.UUIDObject;

import java.util.Date;
import java.util.UUID;

/**
 * Created by murielK on 8/27/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoneState implements UUIDObject {

    private UUID uuid;
    private boolean ready = true;
    private boolean armed;
    private boolean tripped;
    private boolean bypassed;
    private int tripCount;
    private boolean sensorState;
    private boolean sensorOffline;
    private Date exitUntil;
    private Date entryUntil;
    private Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public boolean isArmed() {
        return armed;
    }

    public void setArmed(boolean armed) {
        this.armed = armed;
    }

    public boolean isTripped() {
        return tripped;
    }

    public void setTripped(boolean tripped) {
        this.tripped = tripped;
    }

    public boolean isBypassed() {
        return bypassed;
    }

    public void setBypassed(boolean bypassed) {
        this.bypassed = bypassed;
    }

    public int getTripCount() {
        return tripCount;
    }

    public void setTripCount(int tripCount) {
        this.tripCount = tripCount;
    }

    public boolean isSensorState() {
        return sensorState;
    }

    public void setSensorState(boolean sensorState) {
        this.sensorState = sensorState;
    }

    public boolean isSensorOffline() {
        return sensorOffline;
    }

    public void setSensorOffline(boolean sensorOffline) {
        this.sensorOffline = sensorOffline;
    }

    public Date getExitUntil() {
        return exitUntil;
    }

    public void setExitUntil(Date exitUntil) {
        this.exitUntil = exitUntil;
    }

    public Date getEntryUntil() {
        return entryUntil;
    }

    public void setEntryUntil(Date entryUntil) {
        this.entryUntil = entryUntil;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
