/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.zipato.appv2.R;
import com.zipato.model.client.RestObject;
import com.zipato.model.device.Device;
import com.zipato.model.event.Event;

import java.util.List;

/**
 * Created by murielK on 8/18/2014.
 */
public class JDeviceFragment extends BaseRemoveJoinDFragment {

    private static final String FRAGMENT_TAG = JDeviceFragment.class.getSimpleName();
    private boolean synchronizing;
    private String deviceName;


    @Override
    protected void setTitleSearching() {
        //Empty
    }


    @Override
    protected Object getObjectBody() {
        if ((objectParcel != null) && (objectParcel.getDiscoveryData() != null))
            return objectParcel.getDiscoveryData().getData();
        return null;
    }

    @Override
    protected void restart() {
        setPostCreateText();
        startCounter(COUNTER_TIME);
        start();
    }

    @Override
    protected void setPostCreateText() {
        infiniteCount = true;
        try {
            textTitleID = "stating_discovery";
            butID = "";
            text1.setText(languageManager.translate("stating_discovery"));
            text2.setText("");
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected void setTextIsRunning() {
        try {
            textTitleID = "discovery_fail";
            butID = "";
            text1.setText(languageManager.translate("discovery_already_running"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            stopCounter(R.drawable.circle_refresh, 0, true);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }

    }

    @Override
    protected void fail(String input) {
        try {
            textTitleID = "discovery_fail";
            butID = "";
            text1.setText(languageManager.translate("tap_to_try_again"));
            if ((input != null)) {
                if ("TIMEOUT".equalsIgnoreCase(input)) {
                    text2.setText(languageManager.translate("discovery_timeout"));
                }
            } else {
                text2.setText(languageManager.translate("discovery_comunication_error"));
            }
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            stopCounter(R.drawable.circle_refresh, 0, true);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected boolean isStarted(String status, String type) {
        return ("STARTED".equalsIgnoreCase(status) && "INCLUSION".equalsIgnoreCase(type)) || ("STARTED".equalsIgnoreCase(status) && "DISCOVERY".equalsIgnoreCase(type)) || ("STARTED".equalsIgnoreCase(status) && "REDISCOVERY".equalsIgnoreCase(type)) || ("SUCCESSFULL".equalsIgnoreCase(status) && "INCLUSION".equalsIgnoreCase(type));
    }

    @Override
    protected void setOnStartedText() {
        final String inclusionText = languageManager.translate("trigger_inclusion");
        try {
            if (objectParcel.getName() == null) {
                final String temp = inclusionText.replace("{device}", languageManager.translate("device"));
                text1.setText(temp);
            } else {
                final String temp = inclusionText.replace("{device}", objectParcel.getName());
                text1.setText(temp);
                // text1.setText(languageManager.translate("please_triggger") + "\n" + objectParcel.getName() + " " + languageManager.translate("set_inclusion"));
            }
            text2.setText(languageManager.translate("join_device_text1"));
            textTitleID = "searching_device";
            butID = "";
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            infiniteCount = false;
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected boolean isSuccess(String status, String type) {
        return ("RESYNCHRONIZED".equalsIgnoreCase(status) && "DISCOVERY".equalsIgnoreCase(type)) || ("SUCCESSFUL".equalsIgnoreCase(status) && "DISCOVERY".equalsIgnoreCase(type)) || ("SUCCESSFUL".equalsIgnoreCase(status) && "REDISCOVERY".equalsIgnoreCase(type));
    }

    @Override
    protected void setOnSuccessText(List<Device> devices) {
        synchronizing = true;
        infiniteCount = true;
        if (!devices.isEmpty())
            this.deviceName = devices.get(0).getName();
        try {
            textTitleID = "device_found";
            text1.setText(languageManager.translate("synchronizing"));
            text2.setText("");
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }

    }

    private void onSyncDone() {
        try {
            textTitleID = "device_joined";
            text1.setText(languageManager.translate("congratulation"));
            butID = "done";
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            final String tempText = languageManager.translate("device_joined_text2");
            final String tempText2;
            if (deviceName != null) {
                tempText2 = tempText.replace("{deviceName}", deviceName);
            } else {
                tempText2 = tempText.replace("{deviceName}", "");
            }
            text2.setText(tempText2);
            stopCounter(R.drawable.circle_sticker, 0, false);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected void postFound() {
        RestObject resp = null;
        try {
            Log.d(getFragmentTag(), "Starting synchronization...");
            resp = restTemplate.getForObject("v2/box/synchronize?ifNeeded=false&wait=true&timeout=30", RestObject.class);
            Log.d(getFragmentTag(), "Synchronization done and isSuccess? " + resp.isSuccess());

        } catch (Exception e) {

            //Empty

        } finally {
            final RestObject finalResp = resp;
            baseFragmentHandler.post(new Runnable() {
                @Override
                public void run() {
                    if ((finalResp != null) && finalResp.isSuccess()) {
                        onSyncDone();
                    } else {
                        fail(null);
                        text2.setText(languageManager.translate("synch_fail"));
                    }
                }
            });
        }


    }

    @Override
    protected void postFail() {

    }


    @Override
    protected void setTextOnCounterStopped() {
        if (synchronizing)
            return;
        try {
            textTitleID = "discovery_fail";
            butID = "";
            text1.setText(" ");
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            stopCounter(R.drawable.circle_refresh, 0, true);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }

    }

    @Override
    String stringTitle() {
        return textTitleID;
    }

    @Override
    int stepID() {
        if ((objectParcel != null) && objectParcel.isZwave())
            return 3;
        return 2;
    }

    @Override
    String fragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    String stringButTitle() {
        return butID;
    }

    @Override
    protected boolean isDeviceFound(String status, String type, Device[] devices) {
        return "DEVICE_FOUND".equalsIgnoreCase(status) || "NODE_FOUND".equalsIgnoreCase(status);
    }

    @Override
    protected void setOnFoundText() {
        try {
            textTitleID = "device_found";
            butID = "";
            text1.setText(languageManager.translate("joining_device"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        frameLayout.removeAllViews();
        View view;
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_joining, null);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_joining_land, null);
        }
        frameLayout.addView(view);
        injectView(frameLayout);
    }

}
