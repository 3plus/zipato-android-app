/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.box;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by murielK on 9/19/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Box implements Parcelable {

    public static final Parcelable.Creator<Box> CREATOR = new Parcelable.Creator<Box>() {
        public Box createFromParcel(Parcel source) {
            return new Box(source);
        }

        public Box[] newArray(int size) {
            return new Box[size];
        }
    };
    private String serial;
    private String name;
    private String timezone;
    private long gmtOffset;
    private boolean online;
    private String firmwareVersion;
    private String latestFirmwareVersion;
    private boolean needSync;
    private boolean firmwareUpgradeAvailable;
    private boolean firmwareUpgradeRequired;
    private Date saveDate;
    private Date syncDate;
    private String localIp;
    private String remoteIp;
    private BoxConfig config;

    public Box() {
    }

    private Box(Parcel in) {
        this.serial = in.readString();
        this.name = in.readString();
        this.timezone = in.readString();
        this.gmtOffset = in.readLong();
        this.online = in.readByte() != 0;
        this.firmwareVersion = in.readString();
        this.latestFirmwareVersion = in.readString();
        this.needSync = in.readByte() != 0;
        this.firmwareUpgradeAvailable = in.readByte() != 0;
        this.firmwareUpgradeRequired = in.readByte() != 0;
        long tmpSaveDate = in.readLong();
        this.saveDate = (tmpSaveDate == -1) ? null : new Date(tmpSaveDate);
        long tmpSyncDate = in.readLong();
        this.syncDate = (tmpSyncDate == -1) ? null : new Date(tmpSyncDate);
        this.localIp = in.readString();
        this.remoteIp = in.readString();
        this.config = in.readParcelable(BoxConfig.class.getClassLoader());
    }

    public BoxConfig getConfig() {
        return config;
    }

    public void setConfig(BoxConfig config) {
        this.config = config;
    }

    public boolean isFirmwareUpgradeAvailable() {
        return firmwareUpgradeAvailable;
    }

    public void setFirmwareUpgradeAvailable(boolean fua) {
        this.firmwareUpgradeAvailable = fua;
    }

    public boolean isFirmwareUpgradeRequired() {
        return firmwareUpgradeRequired;
    }

    public void setFirmwareUpgradeRequired(boolean fur) {
        this.firmwareUpgradeRequired = fur;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public long getGmtOffset() {
        return gmtOffset;
    }

    public void setGmtOffset(long gmtOffset) {
        this.gmtOffset = gmtOffset;
    }

    public String getLatestFirmwareVersion() {
        return latestFirmwareVersion;
    }

    public void setLatestFirmwareVersion(String lfv) {
        this.latestFirmwareVersion = lfv;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNeedSync() {
        return needSync;
    }

    public void setNeedSync(boolean needSync) {
        this.needSync = needSync;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public Date getSaveDate() {
        return saveDate;
    }

    public void setSaveDate(Date saveDate) {
        this.saveDate = saveDate;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Date getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.serial);
        dest.writeString(this.name);
        dest.writeString(this.timezone);
        dest.writeLong(this.gmtOffset);
        dest.writeByte(online ? (byte) 1 : (byte) 0);
        dest.writeString(this.firmwareVersion);
        dest.writeString(this.latestFirmwareVersion);
        dest.writeByte(needSync ? (byte) 1 : (byte) 0);
        dest.writeByte(firmwareUpgradeAvailable ? (byte) 1 : (byte) 0);
        dest.writeByte(firmwareUpgradeRequired ? (byte) 1 : (byte) 0);
        dest.writeLong((saveDate != null) ? saveDate.getTime() : -1);
        dest.writeLong((syncDate != null) ? syncDate.getTime() : -1);
        dest.writeString(this.localIp);
        dest.writeString(this.remoteIp);
        dest.writeParcelable(this.config, flags);
    }
}
