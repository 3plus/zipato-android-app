/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import com.zipato.model.typereport.EntityType;

import java.util.UUID;

/**
 * Created by Mur0 on 10/26/2014.
 */
public class ConfigObject {

    public UUID uuid;
    public EntityType entityType;

    public ConfigObject(UUID uuid, EntityType entityType) {
        this.uuid = uuid;
        this.entityType = entityType;
    }
}