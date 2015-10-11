/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;


import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;

import com.zipato.appv2.ui.fragments.vcmenu.ConfigFragment;
import com.zipato.model.BaseEntityType;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.device.Device;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.endpoint.Endpoint;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.UiType;
import com.zipato.model.types.SystemTypes;
import com.zipato.model.types.UserIcons;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by murielK on 10/24/2014.
 */
public class DMConfigFragment<T extends BaseEntityType> extends ConfigFragment {


    public static final String UUID_KEY = "UUID_KEY";
    public static final String ENTITY_KEY = "ENTITY_KEY";
    private static final String TAG = DMConfigFragment.class.getSimpleName();
    @Inject
    DeviceRepository deviceRepository;
    @Inject
    ApiV2RestTemplate restTemplate;
    EntityType entityType;
    T object;
    private UUID uuid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {

                entityType = (EntityType) getArguments().getSerializable(ENTITY_KEY);
                final ParcelUuid parcelUuid = getArguments().getParcelable(UUID_KEY);
                uuid = parcelUuid.getUuid();
                switch (entityType) {
                    case ATTRIBUTE:
                        object = (T) attributeRepository.get(uuid);
                        break;
                    case CLUSTER_ENDPOINT:
                        object = (T) clusterEndpointRepository.get(uuid);
                        break;
                    case ENDPOINT:
                        object = (T) endpointRepository.get(uuid);
                        break;
                    case DEVICE:
                        object = (T) deviceRepository.get(uuid);
                        break;
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onPostViewCreate() {
        super.onPostViewCreate();
    }

    @Override
    public void onActivityCreated(Bundle saveInstaceState) {
        super.onActivityCreated(saveInstaceState);
        // getView().setBackgroundDrawable(getSherlockActivity().getResources().getDrawable(R.drawable.img_background_blur));

    }

    @Override
    protected void init() {
        if (isDetached() || !checkInternet())
            return;

        isStarted = true;
        scrollView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        executor.execute(new Runnable() {
            @Override
            public void run() {


                SystemTypes[] stArray = null;
                UserIcons[] uiArray = null;
                try {
                    switch (getEntityType()) {
                        case ATTRIBUTE:
                            Attribute attribute = restTemplate.getForObject("v2/attributes/{uuid}?config=true&icons=true", Attribute.class, uuid);
                            updateObject((T) attribute);
                            break;
                        case CLUSTER_ENDPOINT:
                            ClusterEndpoint clusterEndpoint = restTemplate.getForObject("v2/clusterEndpoints/{uuid}?config=true&icons=true&type=true", ClusterEndpoint.class, uuid);
                            updateObject((T) clusterEndpoint);
                            break;
                        case ENDPOINT:
                            Endpoint endpoint = restTemplate.getForObject("v2/endpoints/{uuid}?config=true&icons=true&type=true", Endpoint.class, uuid);
                            updateObject((T) endpoint);
                            break;
                        case DEVICE:
                            Device device = restTemplate.getForObject("v2/devices/{uuid}?config=true&icons=true&type=true", Device.class, uuid);
                            updateObject((T) device);
                            break;
                        case NETWORK:
                            break;
                    }
                    stArray = restTemplate.getForObject("v2/types/system/?x=endpointType", SystemTypes[].class);
                    uiArray = restTemplate.getForObject("v2/types/user/?x=endpointType", UserIcons[].class);
                    if (roomRepository.isEmpty())
                        roomRepository.fetchAll();
                } catch (Exception e) {
                    handlerException(e, TAG);
                    return;
                } finally {
                    isStarted = false;
                    sendMessage(MAIN_UI_VISIBILITY_GONE, progressBar);
                    eventBus.post(new ConfigObject(uuid, entityType));
                    if (!notOk(stArray, uiArray)) collectData(stArray, uiArray);
                }
            }
        });
    }

    private void updateObject(T t) {
        if (t.getConfig() != null)
            object.setConfig(t.getConfig());
        if (t.getIcon() != null)
            object.setIcon(t.getIcon());
//        if (t.getUserIcon() != null)
//            object.setUserIcon(t.getUserIcon());
        object.setShowIcon(t.isShowIcon());
    }

    @Override
    protected String endPointType() {
        if (object.getIcon() == null)
            return null;
        return object.getIcon().getEndpointType();
    }

    @Override
    protected boolean notOk(SystemTypes[] stArray,
                            UserIcons[] uiArray) {
        return ((stArray == null) || (uiArray == null) || (object == null));
    }

    @Override
    protected EntityType getEntityType() {
        return entityType;
    }

    @Override
    protected boolean getShow() {
        return object.isShowIcon();
    }

    @Override
    protected UiType getUiTYpe() {
        return object.getIcon();
    }

    @Override
    protected UserIcons getUserIcons() {
        return object.getUserIcon();
    }

    @Override
    protected String getName() {
        return object.getName();
    }

    @Override
    protected String getDescription() {

        return object.getDescription();
    }

    @Override
    protected int getRoom() {
        return object.getConfig().getRoom();
    }

    @Override
    protected boolean getMaster() {
        return (entityType == EntityType.ATTRIBUTE) && object.getConfig().isMaster();
    }

    @Override
    protected void updateItemConfig() {
    }

    protected UUID getUUID() {
        return object.getUuid();
    }

    @Override
    protected boolean setBoolToRefresh() {
        return true;
    }

    @Override
    protected boolean getHidden() {
        return (object != null) && object.getConfig().isHidden();
    }
}
