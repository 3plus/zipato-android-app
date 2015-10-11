/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.endpoint;

import com.zipato.model.Configuration;
import com.zipato.model.UUIDObjectRepository;

import java.util.UUID;

/**
 * Created by dbudor on 10/06/2014.
 */
public class ClusterEndpointRepository extends UUIDObjectRepository<ClusterEndpoint> {

    public Configuration getConfig(UUID uuid) {
        Configuration config = factory.getRestTemplate().getForObject("v2/clusterEndpoints/{uuid}/config", Configuration.class, uuid);
        return config;
    }

    public void removeClusterEndpoint(UUID uuidCE) {
        factory.getRestTemplate().delete("v2/clusterEndpoints/{uuidCE}", uuidCE);
    }

}
