/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.client.RestObject;

import java.util.UUID;

/**
 * Created by dbudor on 02/06/2014.
 */
public class AttributeRepository extends UUIDObjectRepository<Attribute> {

    public void fetchAll() {
        Attribute[] list = factory.getRestTemplate().getForObject("v2/attributes/full?clusterEndpoint=true&definition=true&config=true&parent=true",
                Attribute[].class);
        clear();
        addAll(list);
    }

    public Attribute fetchOne(UUID uuid) {
        Attribute attr = factory.getRestTemplate().getForObject("v2/attributes/{uuid}?full=true", Attribute.class);
        put(uuid, attr);
        return attr;
    }

    public RestObject putValue(UUID uuid, Object value) {
        RestObject ret = factory.getRestTemplate().postForObject("v2/attributes/{uuid}", value, RestObject.class);
        return ret;
    }

}
