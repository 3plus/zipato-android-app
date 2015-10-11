/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.typereport.EntityType;

import javax.inject.Inject;

/**
 * Created by murielK on 24.6.2014..
 */
public class ClusterEndpointFragment extends DmFragment<ClusterEndpoint> {

    private static final String TAG = "ClusterEndpointFragment";

    @Inject
    ClusterEndpointRepository clusterEndpointRepository;

    @Override
    protected Class<? extends DmFragment<?>> getChildFragmentClass() {
        return AttributesFragment.class;
    }

    @Override
    protected int getIndex() {
        return 3;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected UUIDObjectRepository<ClusterEndpoint> getRepository() {
        return clusterEndpointRepository;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.CLUSTER_ENDPOINT;
    }

}