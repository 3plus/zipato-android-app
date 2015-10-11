/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.device.Device;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.typereport.EntityType;

import javax.inject.Inject;

/**
 * Created by murielK on 24.6.2014..
 */
public class DeviceFragment extends DmFragment<Device> {
    private static final String TAG = "DevicesFragment";

    @Inject
    DeviceRepository deviceRepository;

    @Override
    protected Class<? extends DmFragment<?>> getChildFragmentClass() {
        return EndpointFragment.class;
    }

    @Override
    protected int getIndex() {
        return 1;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected UUIDObjectRepository getRepository() {
        return deviceRepository;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.DEVICE;
    }

}