/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import android.util.Log;

import com.zipato.model.UUIDObjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by murielK on 8/28/2014.
 */
public class ZonesRepository extends UUIDObjectRepository<Zone> {

    private transient Map<UUID, List<UUID>> bypassedZones;

    public void clearBypassed() {
        if (bypassedZones != null)
            bypassedZones.clear();
    }

    public void addBypassedZone(UUID partitionUUID, UUID zoneUUID) {
        if ((partitionUUID == null) || (zoneUUID == null))
            return;
        if (bypassedZones == null)
            bypassedZones = new ConcurrentHashMap<>();
        List<UUID> zones = null;
        if (bypassedZones.get(partitionUUID) == null) {
            zones = new ArrayList<>();
            bypassedZones.put(partitionUUID, zones);
        } else
            zones = bypassedZones.get(partitionUUID);
        if (!zones.contains(zoneUUID))
            zones.add(zoneUUID);
    }

    public void upDateByPassedZone(UUID partitionUUID, Zone zone) {
        if ((partitionUUID == null) || (zone == null))
            return;
        final ZoneState state = zone.getZoneState();
        if (state == null)
            return;

        if (state.isBypassed()) addBypassedZone(partitionUUID, zone.getUuid());
        else removeBypassedZone(partitionUUID, zone.getUuid());
    }

    public boolean isZoneBypassed(UUID partitionUUID, UUID zoneUUID) {
        if ((partitionUUID == null) || ((zoneUUID == null) || (bypassedZones == null)))
            return false;

        return (bypassedZones.get(partitionUUID) != null) && bypassedZones.get(partitionUUID).contains(zoneUUID);
    }

    public void removeBypassedZone(UUID partitionUUID, UUID zoneUUID) {
        if ((partitionUUID == null) || (zoneUUID == null) || (bypassedZones == null))
            return;
        if (bypassedZones.containsKey(partitionUUID) && bypassedZones.get(partitionUUID).contains(zoneUUID))
            bypassedZones.get(partitionUUID).remove(zoneUUID);
    }


    public List<UUID> getBypassedZones(UUID partitionUUD) {
        return (bypassedZones == null) ? null : bypassedZones.get(partitionUUD);
    }


    public void fetchOne(UUID uuidPartition, Zone zone) {
        if (zone != null) {
            ZoneState zoneState = factory.getRestTemplate().getForObject("v2/alarm/partitions/{uuidPartition}/zones/{uuidZone}/status", ZoneState.class, uuidPartition, zone.getUuid());
            Zone zoneDevice = factory.getRestTemplate().getForObject("v2/alarm/partitions/{uuidPartition}/zones/{uuidZone}?device=true", Zone.class, uuidPartition, zone.getUuid());
            if ((zoneDevice != null) && (zoneDevice.getDevice() != null))
                zone.setDevice(zoneDevice.getDevice());
            if (zoneState != null)
                zone.setZoneState(zoneState);
            try {
                String newZoneName = zone.getName().replaceAll("one", "");
                zone.setName(newZoneName);
            } catch (Exception e) {
                Log.d("ZoneRepository", "", e);
            }
            add(zone);
        }
    }


    public void removeZone(UUID uuidPartition, UUID uuidZone) {
        factory.getRestTemplate().delete("v2/alarm/partitions/{uuidPartition}/zones/{uuidZone}", uuidPartition, uuidZone);
    }


}
