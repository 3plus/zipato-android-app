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
public class EndpointRepository extends UUIDObjectRepository<Endpoint> {

    public Configuration getConfig(UUID uuid) {
        Configuration config = factory.getRestTemplate().getForObject("v2/endpoints/{uuid}/config", Configuration.class, uuid);
        return config;
    }

    public void removeEndpoint(UUID uuidE) {

        factory.getRestTemplate().delete("v2/endpoints/{uuidE}", uuidE);

    }
}
