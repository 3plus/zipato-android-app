/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.services;

import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;

import javax.inject.Inject;

/**
 * Created by murielK on 8/28/2015.
 */
public class ZipaInstanceIDListenerService extends InstanceIDListenerService {

    @Inject
    PreferenceHelper preferenceHelper;

    @Override
    public void onTokenRefresh() {
        Log.d("ZipaInstanceIDListenerService", "onTokenRefresh");
        preferenceHelper.putStringPref(Preference.PROPERTY_REG_ID, "");
    }
}
