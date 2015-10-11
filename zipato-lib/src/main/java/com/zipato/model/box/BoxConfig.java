/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.box;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by murielK on 5/20/2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoxConfig implements Parcelable {

    public static final Parcelable.Creator<BoxConfig> CREATOR = new Parcelable.Creator<BoxConfig>() {
        public BoxConfig createFromParcel(Parcel source) {
            return new BoxConfig(source);
        }

        public BoxConfig[] newArray(int size) {
            return new BoxConfig[size];
        }
    };
    /**
     * "className": null,
     * "mtu": "",
     * "staticGateway": "172.16.1.1",
     * "simUsername": "",
     * "clusterBoxSerial": "CL010025180D018A4F",
     * "timeZone": 3600,
     * "staticIp": "172.16.1.77",
     * "keepOnline": false,
     * "temperatureScale": "F",
     * "staticDns1": "8.8.8.8",
     * "staticDns2": "8.8.4.4",
     * "proxy": "http://172.16.128.36:3128",
     * "name": "ured",
     * "simPassword": "",
     * "simAPN": "",
     * "ledBrightness": 3,
     * "simPIN": "",
     * "staticNetmask": "255.255.255.0",
     * "timeZoneId": 32
     */


    private String staticGateway;
    private String simUsername;
    private String clusterBoxSerial;
    private int timeZone;
    private String staticIp;
    private boolean keepOnline;
    private String temperatureScale;
    private String staticDns1;
    private String staticDns2;
    private String proxy;
    private String name;
    private String simPassword;
    private String simAPN;
    private String ledBrightness;
    private String simPIN;
    private String staticNetmask;
    private String timeZoneId;

    public BoxConfig() {
    }

    private BoxConfig(Parcel in) {
        this.staticGateway = in.readString();
        this.simUsername = in.readString();
        this.clusterBoxSerial = in.readString();
        this.timeZone = in.readInt();
        this.staticIp = in.readString();
        this.keepOnline = in.readByte() != 0;
        this.temperatureScale = in.readString();
        this.staticDns1 = in.readString();
        this.staticDns2 = in.readString();
        this.proxy = in.readString();
        this.name = in.readString();
        this.simPassword = in.readString();
        this.simAPN = in.readString();
        this.ledBrightness = in.readString();
        this.simPIN = in.readString();
        this.staticNetmask = in.readString();
        this.timeZoneId = in.readString();
    }

    public String getClusterBoxSerial() {
        return clusterBoxSerial;
    }

    public void setClusterBoxSerial(String clusterBoxSerial) {
        this.clusterBoxSerial = clusterBoxSerial;
    }

    public boolean isKeepOnline() {
        return keepOnline;
    }

    public void setKeepOnline(boolean keepOnline) {
        this.keepOnline = keepOnline;
    }

    public String getLedBrightness() {
        return ledBrightness;
    }

    public void setLedBrightness(String ledBrightness) {
        this.ledBrightness = ledBrightness;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getSimAPN() {
        return simAPN;
    }

    public void setSimAPN(String simAPN) {
        this.simAPN = simAPN;
    }

    public String getSimPassword() {
        return simPassword;
    }

    public void setSimPassword(String simPassword) {
        this.simPassword = simPassword;
    }

    public String getSimPIN() {
        return simPIN;
    }

    public void setSimPIN(String simPIN) {
        this.simPIN = simPIN;
    }

    public String getSimUsername() {
        return simUsername;
    }

    public void setSimUsername(String simUsername) {
        this.simUsername = simUsername;
    }

    public String getStaticDns1() {
        return staticDns1;
    }

    public void setStaticDns1(String staticDns1) {
        this.staticDns1 = staticDns1;
    }

    public String getStaticDns2() {
        return staticDns2;
    }

    public void setStaticDns2(String staticDns2) {
        this.staticDns2 = staticDns2;
    }

    public String getStaticGateway() {
        return staticGateway;
    }

    public void setStaticGateway(String staticGateway) {
        this.staticGateway = staticGateway;
    }

    public String getStaticIp() {
        return staticIp;
    }

    public void setStaticIp(String staticIp) {
        this.staticIp = staticIp;
    }

    public String getStaticNetmask() {
        return staticNetmask;
    }

    public void setStaticNetmask(String staticNetmask) {
        this.staticNetmask = staticNetmask;
    }

    public String getTemperatureScale() {
        return temperatureScale;
    }

    public void setTemperatureScale(String temperatureScale) {
        this.temperatureScale = temperatureScale;
    }

    public int getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(int timeZone) {
        this.timeZone = timeZone;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.staticGateway);
        dest.writeString(this.simUsername);
        dest.writeString(this.clusterBoxSerial);
        dest.writeInt(this.timeZone);
        dest.writeString(this.staticIp);
        dest.writeByte(keepOnline ? (byte) 1 : (byte) 0);
        dest.writeString(this.temperatureScale);
        dest.writeString(this.staticDns1);
        dest.writeString(this.staticDns2);
        dest.writeString(this.proxy);
        dest.writeString(this.name);
        dest.writeString(this.simPassword);
        dest.writeString(this.simAPN);
        dest.writeString(this.ledBrightness);
        dest.writeString(this.simPIN);
        dest.writeString(this.staticNetmask);
        dest.writeString(this.timeZoneId);
    }
}
