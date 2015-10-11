/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.vcmenu;

import com.zipato.model.typereport.EntityType;
import com.zipato.model.types.UserIcons;

import java.util.UUID;

/**
 * Created by murielK on 11/6/2014.
 */
public interface IconConfig {

    UUID getUUID();

    EntityType getEntityType();

    boolean isNotValid();

    UserIcons getUserIcon();
}
