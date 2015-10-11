/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.zipato.appv2.R;
import com.zipato.model.device.Device;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectLauncher;

import java.util.HashMap;
import java.util.List;

/**
 * Created by murielK on 8/21/2014.
 */
public class RDeviceFragment extends BaseRemoveJoinDFragment {

    private static final String FRAGMENT_TAG = RDeviceFragment.class.getSimpleName();
    private static final HashMap<String, String> commandMap = new HashMap<String, String>();

    static {
        commandMap.put("command", "DELETE_DEVICE_ZW");
    }

    @Override
    protected void setTextIsRunning() {
        try {
            textTitleID = "restart_fail";
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
            textTitleID = "restart_fail";
            butID = "";
            text1.setText(languageManager.translate("removal_fail"));
            if ((input != null) && "TIMEOUT".equals(input)) {
                text2.setText(languageManager.translate("discovery_timeout"));
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
    protected void setTitleSearching() {
        //empty
    }

    @Override
    protected boolean isStarted(String status, String type) {
        return "STARTED".equalsIgnoreCase(status) && "REMOVE".equalsIgnoreCase(type);
    }

    @Override
    protected void setOnStartedText() {
        try {
            textTitleID = "searching_device";
            butID = "";
            // text1.setText(languageManager.translate("searching_device"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected boolean isSuccess(String status, String type) {
        return "SUCCESSFUL".equalsIgnoreCase(status);
    }

    @Override
    protected void setOnSuccessText(List<Device> devices) {

        try {
            textTitleID = "device_remove";
            text1.setText(languageManager.translate("congratulation"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            text2.setText(languageManager.translate("device_remove_text1"));
            stopCounter(R.drawable.circle_sticker, 0, false);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
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
            text1.setText(languageManager.translate("removing_device2"));
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected void setTextOnCounterStopped() {
        try {
            textTitleID = "restart_fail";
            butID = "";
            text1.setText(languageManager.translate("tap_to_try_again"));

            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
            stopCounter(R.drawable.circle_refresh, 0, true);
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected Object getObjectBody() {
        return commandMap;
    }

    @Override
    protected void restart() {
        setPostCreateText();
        startCounter(COUNTER_TIME);
        start();
    }

    @Override
    protected void setPostCreateText() {
        final String inclusionText = languageManager.translate("trigger_exclusion");
        try {
            if (objectParcel.getName() == null) {
                final String temp = inclusionText.replace("{device}", languageManager.translate("device"));
                text1.setText(temp);
            } else {
                final String temp = inclusionText.replace("{device}", objectParcel.getName());
                text1.setText(temp);
            }
            text2.setText(languageManager.translate("remove_device_text1"));
            textTitleID = "stating_removal";
            butID = "";
            eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
        } catch (Exception e) {
            Log.d(getFragmentTag(), "", e);
        }
    }

    @Override
    protected void injectView(View v) {
        super.injectView(v);
        setFDButton();

    }

    @Override
    protected void onPostViewCreate() {
        super.onPostViewCreate();
        setFDButton();
    }

    private void setFDButton() {

        baseFragmentHandler.obtainMessage(MAIN_UI_VISIBILITY_VISIBLE, butFD).sendToTarget();

        butFD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(languageManager.translate("force_discovery"));
                builder.setMessage(languageManager.translate("force_discovery_message"));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, JDeviceFragment.class, objectParcel), Event.EVENT_TYPE_LAUNCHER));
                        onDestroy();
                    }
                });
                builder.setCancelable(true);
                builder.setNegativeButton(languageManager.translate("cancel"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.cancel();
                        }
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void postFound() {
        if (baseFragmentHandler != null) {
            baseFragmentHandler.post(new Runnable() {
                @Override
                public void run() {
                    butFD.setEnabled(false);
                }
            });
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (eventBus != null) {
            eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, JDeviceFragment.class, objectParcel), Event.EVENT_TYPE_LAUNCHER));
            //onDestroy();
        }
    }

    @Override
    protected void postFail() {

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
