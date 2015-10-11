/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.brand;

import android.os.Parcel;
import android.os.Parcelable;

import com.zipato.model.DynaObject;

/**
 * Created by murielK on 8/19/2014.
 */
public class DiscoveryData extends DynaObject implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
