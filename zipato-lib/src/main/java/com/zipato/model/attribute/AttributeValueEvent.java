/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zipato.model.DynaObject;

import java.util.UUID;

/**
 * Created by dbudor on 02/06/2014.
 */
public class AttributeValueEvent extends DynaObject {

    UUID uuid;
    @JsonProperty
    AttributeValue value;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
