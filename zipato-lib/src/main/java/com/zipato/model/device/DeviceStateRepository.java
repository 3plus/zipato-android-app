/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.device;

import android.util.Log;

import com.zipato.model.SimpleRepository;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Created by murielK on 4.6.2014..
 */
public class DeviceStateRepository extends SimpleRepository<UUID, DeviceState> {

    private final Object lock = new Object();
    private String eTag = null;

    public void clearETag() {
        synchronized (lock) {
            eTag = null;
        }
    }

    public void fetchAll() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("If-None-Match", eTag);
        HttpEntity<?> request = new HttpEntity<Object>(headers);
        ResponseEntity<DeviceStateEvent[]> response = factory.getRestTemplate().exchange("v2/devices/statuses?update=true", HttpMethod.GET, request, DeviceStateEvent[].class);
        DeviceStateEvent[] list = response.getBody();
        if (list != null) {
            Log.d("STATE", "loaded: " + list.length);
            eTag = response.getHeaders().getETag();
            for (DeviceStateEvent deviceStateEvent : list) {
                add(deviceStateEvent);
            }
        } else {
            Log.d("STATE", "no device states update");
        }
    }

    public DeviceState fetchOne(UUID uuid) {
        DeviceState deviceState = factory.getRestTemplate().getForObject("v2/devices/{uuid}/status", DeviceState.class, uuid);
        DeviceStateEvent event = new DeviceStateEvent();
        event.setUuid(uuid);
        event.setState(deviceState);
        add(event);
        return deviceState;
    }

    private void add(DeviceStateEvent event) {
        final UUID key = event.getUuid();
        final DeviceState deviceState = event.getState();
//        DeviceState old = get(key);
        put(key, deviceState);
//        if ((old == null) || older(old, deviceState)) {
//            put(key, deviceState);
//            //factory.getEventBus().post(event);
//        }
    }

    private boolean older(DeviceState av1, DeviceState av2) {
        try {
            if (av1.getTimestamp().getTime() > av2.getTimestamp().getTime()) {
                return true;
            }
        } catch (NullPointerException e) {
        }
        return false;
    }

    @Override
    public DeviceState add(DeviceState deviceState) {
        throw new UnsupportedOperationException();
    }
}
