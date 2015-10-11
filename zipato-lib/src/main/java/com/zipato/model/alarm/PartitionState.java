/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.DynaObject;

import java.util.Date;
import java.util.UUID;

/**
 * Created by murielK on 8/26/2014.
 */
public class PartitionState extends DynaObject {

    private Date timestamp;
    private ArmMode armMode;
    private boolean tripped;
    private Date boxTimestamp;

    //private  ZoneType tripType;
    private UUID trippedBy;
    private UUID trippedByEndpoint;
    private boolean tripSensorState;
    private boolean entryStarted;
    private Date entryUntil;
    private Date entryFrom;
    private UUID entryZone;
    private boolean exitStarted;
    private Date exitUntil;
    private Date exitFrom;
    private Date sirenStartTime;
    private int actualMobilityTime;
    private int mobilityCount;
    private int mobilityCountDay;
    private boolean ringing;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public ArmMode getArmMode() {
        return armMode;
    }

    public void setArmMode(ArmMode armMode) {
        this.armMode = armMode;
    }

    public boolean isTripped() {
        return tripped;
    }

    public void setTripped(boolean tripped) {
        this.tripped = tripped;
    }

    public Date getBoxTimestamp() {
        return boxTimestamp;
    }

    public void setBoxTimestamp(Date boxTimestamp) {
        this.boxTimestamp = boxTimestamp;
    }

    public UUID getTrippedBy() {
        return trippedBy;
    }

    public void setTrippedBy(UUID trippedBy) {
        this.trippedBy = trippedBy;
    }

    public UUID getTrippedByEndpoint() {
        return trippedByEndpoint;
    }

    public void setTrippedByEndpoint(UUID trippedByEndpoint) {
        this.trippedByEndpoint = trippedByEndpoint;
    }

    public boolean isTripSensorState() {
        return tripSensorState;
    }

    public void setTripSensorState(boolean tripSensorState) {
        this.tripSensorState = tripSensorState;
    }

    public boolean isEntryStarted() {
        return entryStarted;
    }

    public void setEntryStarted(boolean entryStarted) {
        this.entryStarted = entryStarted;
    }

    public Date getEntryUntil() {
        return entryUntil;
    }

    public void setEntryUntil(Date entryUntil) {
        this.entryUntil = entryUntil;
    }

    public Date getEntryFrom() {
        return entryFrom;
    }

    public void setEntryFrom(Date entryFrom) {
        this.entryFrom = entryFrom;
    }

    public UUID getEntryZone() {
        return entryZone;
    }

    public void setEntryZone(UUID entryZone) {
        this.entryZone = entryZone;
    }

    public boolean isExitStarted() {
        return exitStarted;
    }

    public void setExitStarted(boolean exitStarted) {
        this.exitStarted = exitStarted;
    }

    public Date getExitUntil() {
        return exitUntil;
    }

    public void setExitUntil(Date exitUntil) {
        this.exitUntil = exitUntil;
    }

    public Date getExitFrom() {
        return exitFrom;
    }

    public void setExitFrom(Date exitFrom) {
        this.exitFrom = exitFrom;
    }

    public Date getSirenStartTime() {
        return sirenStartTime;
    }

    public void setSirenStartTime(Date sirenStartTime) {
        this.sirenStartTime = sirenStartTime;
    }

    public int getActualMobilityTime() {
        return actualMobilityTime;
    }

    public void setActualMobilityTime(int actualMobilityTime) {
        this.actualMobilityTime = actualMobilityTime;
    }

    public int getMobilityCount() {
        return mobilityCount;
    }

    public void setMobilityCount(int mobilityCount) {
        this.mobilityCount = mobilityCount;
    }

    public int getMobilityCountDay() {
        return mobilityCountDay;
    }

    public void setMobilityCountDay(int mobilityCountDay) {
        this.mobilityCountDay = mobilityCountDay;
    }

    public boolean isRinging() {
        return ringing;
    }

    public void setRinging(boolean ringing) {
        this.ringing = ringing;
    }
}
