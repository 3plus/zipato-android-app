/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.security;

import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.alarm.Partition;
import com.zipato.model.alarm.PartitionRepository;
import com.zipato.model.alarm.ZonesRepository;

import javax.inject.Inject;

/**
 * Created by murielK on 8/25/2014.
 */
abstract class BaseSecurityFragment extends BaseTypesFragment {

    @Inject
    protected ZonesRepository zonesRepository;
    @Inject
    PartitionRepository partitionRepository;
    Partition partition;

    protected abstract void init();

}