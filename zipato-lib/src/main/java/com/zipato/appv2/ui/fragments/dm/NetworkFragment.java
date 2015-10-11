/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.network.Network;
import com.zipato.model.network.NetworkRepository;
import com.zipato.model.typereport.EntityType;

import javax.inject.Inject;

/**
 * Created by murielK on 24.6.2014..
 */
public class NetworkFragment extends DmFragment<Network> {

    public static final String TAG = "NetworkFragment";

    @Inject
    NetworkRepository networkRepository;

    @Override
    protected Class<? extends DmFragment<?>> getChildFragmentClass() {
        return DeviceFragment.class;
    }

    @Override
    protected int getIndex() {
        return 0;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected UUIDObjectRepository<Network> getRepository() {
        return networkRepository;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.NETWORK;
    }

}