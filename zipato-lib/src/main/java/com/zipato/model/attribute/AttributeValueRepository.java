/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import android.util.Log;

import com.zipato.model.SimpleRepository;
import com.zipato.v2.client.RestObjectException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.UUID;

/**
 * Created by dbudor on 02/06/2014.
 */

public class AttributeValueRepository extends SimpleRepository<UUID, AttributeValue> {

    private static final String CLASS_NAME = AttributeValueRepository.class.getSimpleName();
    private final Object lock = new Object();
    private String eTag;
    private String eTagMeteo;

    public void clearETag() {
        synchronized (lock) {
            eTagMeteo = null;
            eTag = null;
        }
    }

    public void fetchAll() {
        // clear();
        //AttributeValueEvent[] list = factory.getRestTemplate().getForObject("v2/attributes/values", AttributeValueEvent[].class);
        HttpHeaders headers = new HttpHeaders();
        headers.set("If-None-Match", eTag);
        HttpEntity<?> request = new HttpEntity<Object>(headers);
        ResponseEntity<AttributeValueEvent[]> response = factory.getRestTemplate().exchange("v2/attributes/values?update=true", HttpMethod.GET, request, AttributeValueEvent[].class);
        AttributeValueEvent[] list = response.getBody();
        if (list != null) {
            Log.d("ATTR", "loaded: " + list.length);
            eTag = response.getHeaders().getETag();
            for (AttributeValueEvent event : list) {
                add(event);
            }
        } else {
            Log.d("ATTR", "no attribute update");
        }
    }

    public void fetchMeteoAttr() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("If-None-Match", eTagMeteo);
        HttpEntity<?> request = new HttpEntity<Object>(headers);
        ResponseEntity<AttributeValueEvent[]> response = factory.getRestTemplate().exchange("v2/meteo/attributes/values?update=true", HttpMethod.GET, request, AttributeValueEvent[].class);
        AttributeValueEvent[] list = response.getBody();
        if (list != null) {
            Log.d("ATTR_METEO", "loaded: " + list.length);
            eTagMeteo = response.getHeaders().getETag();
//            for (AttributeValueEvent event : list) {
//                add(event);
//            }
            addAllAttr(list);
        } else {
            Log.d("ATTR_METEO", "no attribute update");
        }
    }

    public AttributeValue fetchOne(UUID uuid) {
        AttributeValueEvent avr = factory.getRestTemplate().getForObject("v2/attributes/{uuid}/value", AttributeValueEvent.class);
        add(avr);
        return avr.value;
    }

    public void add(AttributeValueEvent event) {
        final UUID key = event.uuid;
        final AttributeValue value = event.value;

        AttributeValue av = get(key);
        if ((av == null) || older(av, value)) {
            put(key, value);
            //factory.getEventBus().post(event);
        }
    }

    private void addAllAttr(AttributeValueEvent[] attributeValueEvents) {
        for (AttributeValueEvent attrValue : attributeValueEvents) {
            put(attrValue.getUuid(), attrValue.value);
        }

    }

    private boolean older(AttributeValue av1, AttributeValue av2) {
        if ((av1.timestamp == null) || (av2.timestamp == null))
            return true;

        if (av1.timestamp.getTime() > av2.timestamp.getTime()) {
            return true;
        }

        Date p1 = av1.pendingTimestamp;
        Date p2 = av2.pendingTimestamp;
        if ((p2 != null) && !p2.equals(p1)) {
            return true;
        }
        return (p2 == null) && (p1 == null);
    }

    public boolean putAttributesValue(UUID uuid, String input) {
        final AttributeValue attributeValue = new AttributeValue();
        attributeValue.setValue(input);
        try {
            Log.d("AttributeValueRepo", String.format("Sending value = %s for attribute UUID = %s", input, uuid.toString()));
            factory.getRestTemplate().put("v2/attributes/{uuid}/value", attributeValue, uuid);
            //put(uuid, attributeValue);
            return true;
        } catch (RestObjectException e) {
            Log.d(CLASS_NAME, e.getResponseBody().getError());
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AttributeValue add(AttributeValue attributeValue) {
        throw new UnsupportedOperationException("Add attributeValue is not supported");
    }
}
