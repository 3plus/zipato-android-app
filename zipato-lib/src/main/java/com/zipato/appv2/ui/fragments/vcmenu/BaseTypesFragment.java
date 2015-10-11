/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.vcmenu;

import android.os.Bundle;
import android.util.Log;

import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.helper.CheckLastUpdateHelper;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectConnectivity;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.model.typereport.TypeReportRepository;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 7/29/2014.
 */
public abstract class BaseTypesFragment extends BaseFragment {

    public static final String DEGREE = "\u00B0";
    public static final int STATE_ON_OFF = 11;
    public static final int WARM_WHITE = 203;
    public static final int COLOR_RGB = 213;
    public static final int COLD_WHITE = 204;
    public static final int COLOR_RED = 199;
    public static final int COLOR_GREEN = 200;
    public static final int COLOR_BLUE = 201;
    public static final int TEMPERATURE = 208;
    public static final int INTENSITY = 8;
    public static final int POSITION = 8;
    public static final int ACTUAL = 4;
    public static final int DISABLE = 159;
    public static final int STATE_ = 191;
    public static final int TARGET = 3;
    public static final int VALUE = 185;
    public static final int VOLUME = 256;
    //private static final int  HOLD_UNTIL= ;
    public static final int PRESET = 186;
    public static final int MODE = 187;
    public static final int MODE_ALARM = 197;
    public static final int HOLD_UNTIL = 188;
    //Weather
    public static final int TEMP_MAX = 150;
    public static final int TEMP_MIN = 151;
    public static final int HUMIDITY = 146;
    public static final int PRECIPITATION_PPM = 149;
    public static final int WIND_DIRECTION = 154;
    public static final int TEMPERATURE_WEATHER = 145;
    public static final int SUNRISE = 220;
    public static final int SUNSET = 221;
    // private static final int  WEATHER_DESC =146;
    public static final int WIND_SPEED = 157;
    public static final int OBS_TIME = 158;
    public static final int WEATHER_DESC = 153;
    public static final int WEATHER_UV = 99999999;
    public static final int WEATHER_ICON = 254;
    public static final int BRIGHTNESS = 198;

    @Inject
    protected AttributeRepository attributeRepository;
    @Inject
    protected ExecutorService executor;
    @Inject
    protected EventBus eventBus;
    @Inject
    protected TypeReportRepository typeReportRepository;

    protected long previousUpdate;

    protected TypeReportKey key;

    private CheckLastUpdateHelper lastUpdateHelper;

    protected abstract boolean registerTimeout();

    protected TypeReportItem getItem() {
        if (key != null)
            return typeReportRepository.get(key);
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            try {
                key = savedInstanceState.getParcelable("Key");
            } catch (Exception e) {

            }
        } else if (getArguments() != null) {
            try {
                key = getArguments().getParcelable(PARCELABLE_KEY);
            } catch (Exception e) {
                //
            }
        }

        eventBus.register(this);
        lastUpdateHelper = new CheckLastUpdateHelper();

    }

    @Override
    public void onResume() {
        if (!eventBus.isRegistered(this))
            eventBus.register(this);
        super.onResume();
        if (registerTimeout()) // yeah some subclass wont need it like config, event ...
            lastUpdateHelper.startStatusChecker(new CheckLastUpdateHelper.OnNoUpdatedListner() {
                @Override
                public void notUpdate() {
                    Log.e("BaseControllerFragment", lastUpdateHelper.wasUpdated());
                    final String message = languageManager.translate("connection_time_out_message").replace("{productName}", getResources().getString(R.string.reg_box));
                    eventBus.post(new Event(new ObjectConnectivity(internetConnectionHelper.isOnline(), message), Event.EVENT_TYPE_CONNECTIVITY_EVENT));
                }
            });

    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
        if (registerTimeout())
            lastUpdateHelper.stopStatusChecker();
    }

    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_CONNECTIVITY_EVENT:
                handlerConnectivityEvent((ObjectConnectivity) event.eventObject);
                break;
            case Event.EVENT_TYPE_AUTO_UPDATER_SERVICE:
                lastUpdateHelper.setLastUpdate(System.currentTimeMillis());
                break;
        }
    }

    protected void handlerConnectivityEvent(ObjectConnectivity event) {
        // to be override in case
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            TypeReportKey key = getItem().getKey();
            outState.putParcelable("Key", key);
        } catch (Exception e) {
        }
    }


}
