/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.UUIDObjectRepository;

/**
 * Created by murielK on 8/27/2014.
 */
public class AlarmRepository extends UUIDObjectRepository<Alarm> {
    public void fetchAll() {

        Alarm alarm = factory.getRestTemplate().getForObject("v2/alarm/full", Alarm.class);
        if (alarm.getPartitions() != null) {

            for (Partition partition : alarm.getPartitions()) {
                PartitionRepository partitionRepository = factory.getRepository(PartitionRepository.class);
                partitionRepository.put(partition.getUuid(), partition);
            }
        }
    }

}
