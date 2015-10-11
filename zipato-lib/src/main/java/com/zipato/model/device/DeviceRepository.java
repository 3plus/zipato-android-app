/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.device;

import android.util.Log;

import com.zipato.model.Configuration;
import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.client.RestObject;

import java.util.UUID;

/**
 * Created by murielK on 4.6.2014..
 */
public class DeviceRepository extends UUIDObjectRepository<Device> {

    public void fetchAll() {
        Device[] device = factory.getRestTemplate().getForObject("v2/devices", Device[].class);
        clear();
        addAll(device);
    }

    public Configuration getConfig(UUID uuid) {
        Configuration config = factory.getRestTemplate().getForObject("v2/devices/{uuid}/config", Configuration.class, uuid);
        return config;
    }

    public void removeDevice(UUID uuidDevice) {
        Log.d("DeviceRepo", "Removing device: " + get(uuidDevice).getName());
        factory.getRestTemplate().delete("v2/devices/{uuidDevice}", uuidDevice);
        Log.d("DeviceRepo", "Removing terminate: ");

    }

    public RestObject reapplyDesc(UUID uuidDevice) {
        RestObject resp = factory.getRestTemplate().postForObject("v2/devices/{uuidDevice}/reapply", null, RestObject.class, uuidDevice);
        return resp;
    }
}
