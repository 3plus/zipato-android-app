/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zipato.model.UUIDObject;
import com.zipato.model.attribute.Attribute;

import java.util.Date;
import java.util.UUID;

/**
 * Created by murielK on 8/26/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Partition implements UUIDObject {


    private boolean showIcon;
    private boolean success;
    private Date timestamp;
    private boolean tripped;
    private Date boxTimestamp;
    private int event;
    private Zone[] zones;
    private Attribute[] attributes;
    private PartitionState state;
    private String icon;
    private String userIcon;
    private String name;
    private UUID uuid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public PartitionState getState() {
        return state;
    }

    public void setState(PartitionState state) {
        this.state = state;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
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

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public Zone[] getZones() {
        return zones;
    }

    public void setZones(Zone[] zones) {
        this.zones = zones;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }
}
