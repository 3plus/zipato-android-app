/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.UUIDObjectRepository;

import java.util.UUID;

/**
 * Created by murielK on 8/26/2014.
 */
public class PartitionRepository extends UUIDObjectRepository<Partition> {


    public void fetchAll() {
        Alarm alarms = factory.getRestTemplate().getForObject("v2/alarm/full", Alarm.class);
        for (Partition partition : alarms.getPartitions()) {
            checkState(partition);
            // zonesRepository.addAll(partition.getZones());
            Partition localPartition = get(partition.getUuid());
            if (localPartition != null)
                partition.setZones(localPartition.getZones());
            add(partition);
        }
    }

    public void fetchOne(UUID uuid) {
        Partition partition = factory.getRestTemplate().getForObject("v2/alarm/partitions/{uuid}?zones=true&attributes=false&state=true", Partition.class, uuid);
        checkState(partition);
    }

    public void checkState(Partition partition) {
        PartitionState state = partition.getState();
        if (state == null) {
            PartitionState state2 = new PartitionState();
            state2.setArmMode(ArmMode.DISARMED);
            partition.setState(state2);
        } else if (state.getArmMode() == null) {
            state.setArmMode(ArmMode.DISARMED);
        }
    }


}
