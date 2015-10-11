/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.typereport;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Created by dbudor on 03/06/2014.
 */
public class TypeReportKey implements Parcelable {

    public static final Parcelable.Creator<TypeReportKey> CREATOR = new Parcelable.Creator<TypeReportKey>() {
        public TypeReportKey createFromParcel(Parcel source) {
            return new TypeReportKey(source);
        }

        public TypeReportKey[] newArray(int size) {
            return new TypeReportKey[size];
        }
    };
    private UUID uuid;
    private EntityType type;

    public TypeReportKey(TypeReportItem item) {
        type = item.getEntityType();
        uuid = item.getUuid();
    }

    @JsonCreator
    public TypeReportKey(@JsonProperty("uuid") UUID uuid, @JsonProperty("type") EntityType type) {
        this.uuid = uuid;
        this.type = type;
    }

    private TypeReportKey(Parcel in) {
        this.uuid = (UUID) in.readSerializable();
        int tmpType = in.readInt();
        this.type = (tmpType == -1) ? null : EntityType.values()[tmpType];
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeReportKey)) return false;

        TypeReportKey that = (TypeReportKey) o;

        if (type != that.type) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.uuid);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }
}
