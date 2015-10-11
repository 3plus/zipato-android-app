/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zipato.appv2.ZipatoApplication;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectConnectivity;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 1/14/2015.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Inject
    InternetConnectionHelper internetConnectionHelper;
    @Inject
    EventBus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        ((ZipatoApplication) context.getApplicationContext()).inject(this);
        final ObjectConnectivity objectConnectivity = new ObjectConnectivity(internetConnectionHelper.isOnline(), null);
        eventBus.post(new Event(objectConnectivity, Event.EVENT_TYPE_CONNECTIVITY_EVENT));
    }
}
