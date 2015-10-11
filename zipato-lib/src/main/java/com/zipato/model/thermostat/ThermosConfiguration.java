/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.thermostat;

import com.zipato.model.BaseObject;

import java.util.HashMap;

/**
 * Created by murielK on 7/24/2014.
 */
public class ThermosConfiguration extends BaseObject {
    private Preset[] presets;
    private HashMap<String, String> autoPresetTimes = new HashMap<String, String>();
    private boolean hidden;
    private EnumOperation operationMode;
    private String room;
    private String status;
    private double hysteresis;
    private double separation;
    private double cooldown;
    private double offset;
    private String className;

    public Preset[] getPresets() {
        return presets;
    }

    public void setPresets(Preset[] presets) {
        this.presets = presets;
    }

    public HashMap<String, String> getAutoPresetTimes() {
        return autoPresetTimes;
    }

    public void setAutoPresetTimes(HashMap<String, String> autoPresetTimes) {
        this.autoPresetTimes = autoPresetTimes;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public EnumOperation getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(EnumOperation operationMode) {
        this.operationMode = operationMode;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getHysteresis() {
        return hysteresis;
    }

    public void setHysteresis(double hysteresis) {
        this.hysteresis = hysteresis;
    }

    public double getSeparation() {
        return separation;
    }

    public void setSeparation(double separation) {
        this.separation = separation;
    }

    public double getCooldown() {
        return cooldown;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }


}


