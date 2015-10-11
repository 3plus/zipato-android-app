/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.zipato.model.BaseEntityType;

/**
 * Created by murielK on 5.6.2014..
 */
public class Endpoint extends BaseEntityType {

    //@JsonSerialize(contentUsing = UuidObjectSerializer.class)
    @JsonManagedReference
    private ClusterEndpoint[] clusterEndpoints;

    @Override
    @JsonIgnore
    public ClusterEndpoint[] getChildren() {
        return clusterEndpoints;
    }

    public ClusterEndpoint[] getClusterEndpoints() {
        return clusterEndpoints;
    }

    public void setClusterEndpoints(ClusterEndpoint[] clusterEndpoints) {
        this.clusterEndpoints = clusterEndpoints;
    }
}
