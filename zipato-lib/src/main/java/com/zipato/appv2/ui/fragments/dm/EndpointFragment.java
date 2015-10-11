/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.endpoint.Endpoint;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.model.typereport.EntityType;

import javax.inject.Inject;

/**
 * Created by murielK on 24.6.2014..
 */
public class EndpointFragment extends DmFragment<Endpoint> {

    private static final String TAG = "EndpointFragment";

    @Inject
    EndpointRepository endpointRepository;

    @Override
    protected Class<? extends DmFragment<?>> getChildFragmentClass() {
        return ClusterEndpointFragment.class;
    }

    @Override
    protected int getIndex() {
        return 2;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected UUIDObjectRepository getRepository() {
        return endpointRepository;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.ENDPOINT;
    }

}