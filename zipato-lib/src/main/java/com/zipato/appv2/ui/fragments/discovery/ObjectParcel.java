/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.os.Parcel;
import android.os.Parcelable;

import com.zipato.model.brand.DiscoveryData;

import java.util.UUID;

/**
 * Created by murielK on 8/19/2014.
 */
public class ObjectParcel implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ObjectParcel> CREATOR = new Parcelable.Creator<ObjectParcel>() {
        @Override
        public ObjectParcel createFromParcel(Parcel in) {
            return new ObjectParcel(in);
        }

        @Override
        public ObjectParcel[] newArray(int size) {
            return new ObjectParcel[size];
        }
    };
    private UUID uuid;
    private String name;
    private DiscoveryData discoveryData;
    private boolean isZwave;

    public ObjectParcel(UUID uuid, DiscoveryData discoveryData, String name) {
        this.uuid = uuid;
        this.discoveryData = discoveryData;
        this.name = name;
    }

    protected ObjectParcel(Parcel in) {
        uuid = (UUID) in.readValue(UUID.class.getClassLoader());
        discoveryData = (DiscoveryData) in.readValue(DiscoveryData.class.getClassLoader());
        name = in.readString();
        isZwave = in.readByte() != 0x00;

    }

    public boolean isZwave() {
        return isZwave;
    }

    public void setZwave(boolean isZwave) {
        this.isZwave = isZwave;
    }

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

    public DiscoveryData getDiscoveryData() {
        return discoveryData;
    }

    public void setDiscoveryData(DiscoveryData discoveryData) {
        this.discoveryData = discoveryData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(uuid);
        dest.writeValue(discoveryData);
        dest.writeValue(name);
        dest.writeByte((byte) (isZwave ? 0x01 : 0x00));
    }
}