/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.camera;

import com.zipato.model.BaseObject;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.device.Device;
import com.zipato.model.endpoint.ClusterEndpoint;

/**
 * Created by murielK on 9/8/2014.
 */
public class Camera extends BaseObject {

    private String adminUrl;
    private String snapshot;
    private String hiQualityStream;
    private String lowQualityStream;
    private String mjpegUrl;
    private String ipAddress;
    private SVFileRest lastFile;
    private Device device;
    private ClusterEndpoint clusterEndpoints;
    private Attribute attributes;
    private boolean showIcon;
    private String templateId;


    public String getAdminUrl() {
        return adminUrl;
    }

    public void setAdminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getHiQualityStream() {
        return hiQualityStream;
    }

    public void setHiQualityStream(String hiQualityStream) {
        this.hiQualityStream = hiQualityStream;
    }

    public String getLowQualityStream() {
        return lowQualityStream;
    }

    public void setLowQualityStream(String lowQualityStream) {
        this.lowQualityStream = lowQualityStream;
    }

    public String getMjpegUrl() {
        return mjpegUrl;
    }

    public void setMjpegUrl(String mjpegUrl) {
        this.mjpegUrl = mjpegUrl;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public SVFileRest getLastFile() {
        return lastFile;
    }

    public void setLastFile(SVFileRest lastFile) {
        this.lastFile = lastFile;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public ClusterEndpoint getClusterEndpoints() {
        return clusterEndpoints;
    }

    public void setClusterEndpoints(ClusterEndpoint clusterEndpoints) {
        this.clusterEndpoints = clusterEndpoints;
    }

    public Attribute getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute attributes) {
        this.attributes = attributes;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

}
