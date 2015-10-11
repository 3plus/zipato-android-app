/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.os.Bundle;
import android.util.Log;

import com.zipato.appv2.R;
import com.zipato.model.client.RestObject;
import com.zipato.model.device.Device;
import com.zipato.model.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by murielK on 9/22/2014.
 */
public class JIPCamFragment extends BaseRemoveJoinDFragment {


    private static final String FRAGMENT_TAG = JIPCamFragment.class.getSimpleName();
    private static final HashMap<String, Boolean> objectBody = new HashMap<String, Boolean>();

    static {
        objectBody.put("onvif", true);
        objectBody.put("upnp", true);
        objectBody.put("http", true);
        objectBody.put("scan", false);
    }

    private final List<Device> deviceList = new ArrayList<>();

    @Override
    protected Object getObjectBody() {
        return objectBody;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
        infiniteCount = true;

    }

    @Override
    protected void restart() {
        setPostCreateText();
        startCounter(COUNTER_TIME);
        start();
    }

    @Override
    protected void setPostCreateText() {
        try {
            text1.setText(languageManager.translate("init_ip_discovery"));
            text2.setText("");
            textTitleID = "stating_discovery";
            butID = "";
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
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
            if ((input != null) && !"TIMEOUT".equalsIgnoreCase(input)) {
                text2.setText(languageManager.translate(input));

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
    protected void setTitleSearching() {
        //Empty
    }

    @Override
    protected boolean isStarted(String status, String type) {
        return ("STARTED".equalsIgnoreCase(status) && "REDISCOVERY".equalsIgnoreCase(type)) || ("STARTED".equalsIgnoreCase(status) && "DISCOVERY".equalsIgnoreCase(type));

    }

    @Override
    protected void setOnStartedText() {
        try {
            textTitleID = "searching_device";
            butID = "";
            text1.setText(languageManager.translate("searching_device"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
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
        deviceChecker(devices);
        if (deviceList.isEmpty())
            return;
        infiniteCount = true;
        try {
            textTitleID = "device_found";
            text1.setText(languageManager.translate("synchronizing"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }

    }

    private void deviceChecker(List<Device> devices) {
        if ((devices == null))
            return;

        deviceChecker(devices.toArray(new Device[devices.size()]));
    }

    private void deviceChecker(Device[] devices) {
        if ((devices == null))
            return;

        for (Device device : devices) {
            if ((device.getData() == null) && (device.getTemplateId() == null))
                continue;
            String temp = "";
            try {
                temp = device.getTemplateId();
                String[] tempArray = temp.split("CAMERA");
                temp = tempArray[0];
            } catch (Exception e) {

            }
            if ("CAMERA".equalsIgnoreCase(temp) || ((device.getData() != null) && device.getData().containsKey("descriptor"))) {
                if (!deviceList.contains(device))
                    deviceList.add(device);
            }
        }
    }

    @Override
    protected boolean isDeviceFound(String status, String type, Device[] devices) {
        if (devices == null)
            return false;

        //deviceList.clear();
        deviceChecker(devices);
        return !deviceList.isEmpty();
    }


    @Override
    protected void setOnFoundText() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            textTitleID = "device_found";
            butID = "";
            text1.setText(languageManager.translate("device_found"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            for (int i = 0; i < deviceList.size(); i++) {
                stringBuilder.append(deviceList.get(i).getName());
                if (i < (deviceList.size() - 1))
                    stringBuilder.append(", ");
            }
            text2.setText(stringBuilder.toString());

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
            StringBuilder stringBuilder = new StringBuilder(languageManager.translate("device_joined_text1"));
            stringBuilder.append(" ");
            for (int i = 0; i < deviceList.size(); i++) {
                stringBuilder.append(deviceList.get(i).getName());
                if (i < (deviceList.size() - 1))
                    stringBuilder.append(", ");
            }
            stringBuilder.append(" ");
            if (deviceList.size() > 1) {
                stringBuilder.append(languageManager.translate("devices"));
            } else {
                stringBuilder.append(languageManager.translate("device"));
            }
            text2.setText(stringBuilder.toString());
            stopCounter(R.drawable.circle_sticker, 0, false);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected void postFound() {
        if (deviceList.isEmpty()) {
            baseFragmentHandler.post(new Runnable() {
                @Override
                public void run() {
                    fail("device_not_found");
                }
            });
            return;
        }
        try {
            Log.d(getFragmentTag(), "Starting synchronization...");
            RestObject resp = restTemplate.getForObject("v2/box/synchronize?ifNeeded=false&wait=true&timeout=30", RestObject.class);
            Log.d(getFragmentTag(), "Synchronization done and isSuccess? " + resp.isSuccess());
            if (resp.isSuccess()) {
                baseFragmentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onSyncDone();
                    }
                });

            } else {
                baseFragmentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        fail("synch_fail");
                    }
                });

            }

        } catch (Exception e) {

            baseFragmentHandler.post(new Runnable() {
                @Override
                public void run() {
                    fail("synch_fail");
                }
            });
        }


    }

    @Override
    protected void postFail() {


    }

    @Override
    protected void setTextOnCounterStopped() {

    }

    @Override
    String stringTitle() {
        return textTitleID;
    }

    @Override
    int stepID() {
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


}
