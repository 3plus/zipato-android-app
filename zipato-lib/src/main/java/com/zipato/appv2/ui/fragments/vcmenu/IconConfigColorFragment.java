/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.vcmenu;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectIcon;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.types.UserIcons;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.OnItemClick;

/**
 * Created by murielK on 11/4/2014.
 */
public class IconConfigColorFragment extends ScenesIconColorFragment implements IconConfig {

    private static final String TAG = IconConfigColorFragment.class.getSimpleName();
    protected boolean isStarted;
    protected UserIcons userIcons;
    @Inject
    ExecutorService executors;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    PreferenceHelper preferenceHelper;
    List<UserIcons> userIconsList;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        layout.setVisibility(View.VISIBLE);
        button1.setText(languageManager.translate("default"));
        button2.setText(languageManager.translate("save"));
        return v;
    }

    @Override
    @OnItemClick(B.id.gridViewSceneColor)
    public void onColorClick(int position) {
        checkUserIcon();
        colorGridAdapter.removeSelection();
        colorGridAdapter.toggleSelection(position);
        userIcons.setColor(colorGridAdapter.getItem(position));
        eventBus.post(new Event(new ObjectIcon(ObjectIcon.TYPE_COLOR, colorGridAdapter.getItem(position)), Event.EVENT_TYPE_KITKAT_ICON));
    }

    @Override
    @OnItemClick(B.id.gridViewSceneIcon)
    public void onIconClick(int position) {
        checkUserIcon();
        iconGridAdapter.removeSelection();
        iconGridAdapter.toggleSelection(position);
        try {
            userIcons.setName(userIconsList.get(position).getName());
            userIcons.setRelativeUrl(userIconsList.get(position).getRelativeUrl());
        } catch (Exception e) {

        }
        eventBus.post(new Event(new ObjectIcon(ObjectIcon.TYPE_ICON, iconGridAdapter.getItem(position)), Event.EVENT_TYPE_KITKAT_ICON));
    }

    @Override
    public void onDefaultClick(View v) {
        try {
            iconGridAdapter.removeSelection();
            colorGridAdapter.removeSelection();
            onDefault();
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    @Override
    public void onSaveClick(View v) {
        onSave();
    }

    private void checkUserIcon() {
        if (userIcons == null)
            userIcons = new UserIcons();
    }


    @Override
    public void init(Bundle savedInstanceState) {
        fetchIcons();
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_color_icon;
    }

    protected void fetchIcons() {

        if (!checkInternet() || !restTemplate.isAuthenticated())
            return;

        isStarted = true;
        layoutListViews.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        button1.setEnabled(false);
        button2.setEnabled(false);

        executors.execute(new Runnable() {
            @Override
            public void run() {

                UserIcons[] uiArray;
                if (userIconsList == null)
                    userIconsList = new ArrayList<>();
                userIconsList.clear();

                final List<String> fonts = new ArrayList<>();

                try {
                    uiArray = restTemplate.getForObject("v2/types/user/?x=relativeUrl,color", UserIcons[].class);

                    for (UserIcons userIcons1 : uiArray) {
                        if (userIcons1.getRelativeUrl().contains("font")) {
                            String[] temp = userIcons1.getRelativeUrl().split(":");
                            String font = temp[temp.length - 1];
                            fonts.add(font);
                            userIconsList.add(userIcons1);
                        }
                    }

                } catch (Exception e) {
                    //
                } finally {
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (colorGridAdapter == null) {
                                colorGridAdapter = new ColorGridListAdapter();
                                colorGridAdapter.setColors(new String[]{"#FFFFFF"});// temporary deactivating all other colors
                            }
                            if (iconGridAdapter == null)
                                iconGridAdapter = new IconGridListAdapter();

                            iconGridAdapter.setIcons(fonts.toArray(new String[fonts.size()]));

                            setListView();

                            if ((gridViewColor.getAdapter() == null) || (gridViewIcon.getAdapter() == null)) {

                                gridViewColor.setAdapter(colorGridAdapter);
                                gridViewIcon.setAdapter(iconGridAdapter);

                            } else {

                                colorGridAdapter.notifyDataSetChanged();
                                iconGridAdapter.notifyDataSetChanged();
                            }

                            layoutListViews.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            button1.setEnabled(true);
                            button2.setEnabled(true);
                            isStarted = false;
                        }
                    });
                }


            }
        });

    }

    protected void setListView() {
        try {
            if ((getUserIcon() != null)) {
                if (getUserIcon().getRelativeUrl().contains("font")) {
                    String[] temp = getUserIcon().getRelativeUrl().split(":");
                    String font = temp[temp.length - 1];
                    int iconIndex = getSelectedIndex(font, iconGridAdapter.getIcons());
                    iconGridAdapter.removeSelection();
                    iconGridAdapter.toggleSelection(iconIndex);
                    colorGridAdapter.removeSelection();
                    if (getUserIcon().getColor() != null) {
                        int colorIndex = getSelectedIndex(getUserIcon().getColor(), colorGridAdapter.getColors());
                        colorGridAdapter.toggleSelection(colorIndex);
                    }
                }
            }
        } catch (Exception e) {

        }

    }

    protected void updateUserIcon() {

        try {
            getItem().setUserIcon(userIcons);
            preferenceHelper.putBooleanPref(Preference.REFRESH_ON_RESUME, true);
            eventBus.post(new Event(null, Event.EVENT_TYPE_REFRESH_REQUEST));
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    private void onSave() {

        if (restTemplate.isUseLocal()) {
            toast(languageManager.translate("local_error"));
            return;
        }
        if (isNotValid()) {
            toast(languageManager.translate("error_getting_entity"));
            return;
        }
        if (!checkInternet())
            return;

        if (userIcons == null) {
            toast(languageManager.translate("please_select_icon"));
            return;
        }
        showProgressDialog(languageManager.translate("saving_configurations"), false);
        executors.execute(new Runnable() {
            @Override
            public void run() {
                handleResult(sendUserIcon());
            }
        });
    }

    private void onDefault() {
        if (isNotValid()) {
            toast(languageManager.translate("error_getting_entity"));
            return;
        }

        if (!checkInternet())
            return;

        userIcons = null;

        showProgressDialog(languageManager.translate("saving_configurations"), false);

        executors.execute(new Runnable() {
            @Override
            public void run() {
                handleResult(resetDefault());
            }
        });
    }


    private void handleResult(boolean success) {

        if (!success)
            sendMessage(MAIN_UI_TOAST, languageManager.translate("fail_to_save_icon"));
        else
            baseFragmentHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateUserIcon();
                }
            });
        sendMessage(MAIN_UI_DISMISS_P_DIALOG, null);


    }

    private boolean resetDefault() {
        switch (getEntityType()) {
            case ENDPOINT:
                try {
                    restTemplate.postForObject("v2/endpoints/{uuid}/icon?userIcon=", null, HashMap.class, getUUID().toString());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }
                return true;
            case CLUSTER_ENDPOINT:
                try {
                    restTemplate.postForObject("v2/clusterEndpoints/{uuid}/icon?userIcon=", null, HashMap.class, getUUID().toString());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }

                return true;
            case ATTRIBUTE:
                try {
                    restTemplate.postForObject("v2/attributes/{uuid}/icon?userIcon=", null, HashMap.class, getUUID().toString());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }

                return true;
            case DEVICE:
                try {
                    restTemplate.postForObject("v2/devices/{uuid}/icon?userIcon=", null, HashMap.class, getUUID().toString());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }

                return true;
            case NETWORK:
//                try {
//                    restTemplate.put("v2/network/{uuid}/icon?userIcon={iconName}", userIcons.getName(), getItem().getUuid());
//                }catch (Exception e ){
//                    Log.e(TAG, "", e);
//                    return false;
//                }
                return true;

        }
        return false;
    }

    private boolean sendUserIcon() {
        switch (getEntityType()) {

            case ENDPOINT:
                try {
                    restTemplate.postForObject("v2/endpoints/{uuid}/icon?userIcon={iconName}", null, HashMap.class, getUUID().toString(), userIcons.getName());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }
                return true;
            case CLUSTER_ENDPOINT:
                try {
                    restTemplate.postForObject("v2/clusterEndpoints/{uuid}/icon?userIcon={iconName}", null, HashMap.class, getUUID().toString(), userIcons.getName());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }

                return true;
            case ATTRIBUTE:
                try {
                    restTemplate.postForObject("v2/attributes/{uuid}/icon?userIcon={iconName}", null, HashMap.class, getUUID().toString(), userIcons.getName());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }

                return true;
            case DEVICE:
                try {
                    restTemplate.postForObject("v2/devices/{uuid}/icon?userIcon={iconName}", null, HashMap.class, getUUID().toString(), userIcons.getName());
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }

                return true;
            case NETWORK:
//                try {
//                    restTemplate.put("v2/network/{uuid}/icon?userIcon={iconName}", userIcons.getName(), getItem().getUuid());
//                }catch (Exception e ){
//                    Log.e(TAG, "", e);
//                    return false;
//                }
                return true;

        }
        return false;
    }

    @Override
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        if (event.eventType == Event.EVENT_TYPE_REPO_SYNCED) {
            if (!isStarted)
                fetchIcons();
        }

    }

    @Override
    public UUID getUUID() {
        return (getItem() == null) ? null : getItem().getUuid();
    }

    @Override
    public EntityType getEntityType() {
        return (getItem() == null) ? null : getItem().getEntityType();
    }

    @Override
    public boolean isNotValid() {
        return getItem() == null;
    }

    @Override
    public UserIcons getUserIcon() {
        return (getItem() == null) ? null : getItem().getUserIcon();
    }
}
