/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.vcmenu.IconConfigColorFragment;
import com.zipato.model.BaseEntityType;
import com.zipato.model.Configuration;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.model.event.Event;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.types.UserIcons;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by murielK on 11/6/2014.
 */
public class DMIconConfigColorFragment<T extends BaseEntityType> extends IconConfigColorFragment {

    private static final String TAG = DMIconConfigColorFragment.class.getSimpleName();
    @Inject
    protected EndpointRepository endpointRepository;
    @Inject
    protected ClusterEndpointRepository clusterEndpointRepository;
    @Inject
    ApiV2RestTemplate restTemplate;
    EntityType entityType;
    T object;
    @Inject
    DeviceRepository deviceRepository;
    private UUID uuid;

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean isNotValid() {
        return object == null;
    }

    @Override
    public UserIcons getUserIcon() {

        if (object.getUserIcon() == null)
            return null;
        return object.getUserIcon();
    }

    public void onEventMainThread(ConfigObject object) {
        try {
            entityType = object.entityType;
            uuid = object.uuid;
            switch (entityType) {
                case ATTRIBUTE:
                    this.object = (T) attributeRepository.get(uuid);
                    break;
                case CLUSTER_ENDPOINT:
                    this.object = (T) clusterEndpointRepository.get(uuid);
                    break;
                case ENDPOINT:
                    this.object = (T) endpointRepository.get(uuid);
                    Configuration configuration = this.object.getConfig();
                    break;
                case DEVICE:
                    this.object = (T) deviceRepository.get(uuid);
                    break;
            }
            fetchIcons();
        } catch (Exception e) {
            handlerException(e, TAG);
        }
    }

    @Override
    public void init(Bundle saveInstance) {
        baseFragmentHandler.post(new Runnable() {
            @Override
            public void run() {
                layoutListViews.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onEventMainThread(Event event) {

    }

    @Override
    protected void updateUserIcon() {

        try {
            object.setUserIcon(userIcons);
        } catch (Exception e) {

            Log.d(TAG, "", e);
        }
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_color_icon;
    }
}
