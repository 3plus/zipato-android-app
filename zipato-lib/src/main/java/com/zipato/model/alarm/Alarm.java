/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.BaseObject;

/**
 * Created by murielK on 8/26/2014.
 */
public class Alarm extends BaseObject {


    private Partition[] partitions;

    public Partition[] getPartitions() {
        return partitions;
    }

    public void setPartitions(Partition[] partitions) {
        this.partitions = partitions;
    }
}
