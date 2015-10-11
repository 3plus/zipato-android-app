/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.scene;

import com.zipato.model.DynaObject;

import java.util.UUID;

/**
 * Created by murielK on 9/25/2014.
 */
public class SceneSetting extends DynaObject {

    private UUID clusterEndpoint;
    private UUID endpoint;
    private UUID attributeUuid;
    private UUID device;
    private String clusterClass;
    private String attributeState;
    private String value;

    public UUID getClusterEndpoint() {
        return clusterEndpoint;
    }

    public void setClusterEndpoint(UUID clusterEndpoint) {
        this.clusterEndpoint = clusterEndpoint;
    }

    public UUID getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(UUID endpoint) {
        this.endpoint = endpoint;
    }

    public UUID getAttributeUuid() {
        return attributeUuid;
    }

    public void setAttributeUuid(UUID attributeUuid) {
        this.attributeUuid = attributeUuid;
    }

    public UUID getDevice() {
        return device;
    }

    public void setDevice(UUID device) {
        this.device = device;
    }

    public String getClusterClass() {
        return clusterClass;
    }

    public void setClusterClass(String clusterClass) {
        this.clusterClass = clusterClass;
    }

    public String getAttributeState() {
        return attributeState;
    }

    public void setAttributeState(String attributeState) {
        this.attributeState = attributeState;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
