/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.thermostat;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.box.Box;

import java.util.UUID;

/**
 * Created by murielK on 7/23/2014.
 */
public class ThermostatRepository extends UUIDObjectRepository<Thermostat> {
    public void fetchAll() {
        Thermostat[] thermostats = factory.getRestTemplate().getForObject("v2/thermostats", Thermostat[].class);
        for (Thermostat thermostat : thermostats) {

            fetchOne(thermostat.getUuid());
        }
    }

    public void fetchOne(UUID uuid) {

        final Thermostat thermostat = factory.getRestTemplate().getForObject("v2/thermostats/{uuid}?operations=true&attributes=true&config=true&bindings=true", Thermostat.class, uuid);
        final Box box = factory.getRestTemplate().getForObject("v2/box", Box.class);
        if ((box != null) && (box.getConfig() != null)) {
            final String temperatureScale = box.getConfig().getTemperatureScale();
            if ((temperatureScale != null) && "F".equalsIgnoreCase(temperatureScale)) {
                thermostat.setFahrenheit(true);
            }
        }
        if (thermostat.getOperations() != null) {
            int size = thermostat.getOperations().length;
            for (int i = 0; i < size; i++) {
                if (thermostat.getOperations()[i].getAttributes() != null) {
                    int sizeAttr = thermostat.getOperations()[i].getAttributes().length;
                    for (int g = 0; g < sizeAttr; g++) {
                        thermostat.getOperations()[i].getAttributeIntMap().put(thermostat.getOperations()[i].getAttributes()[g].getAttributeId(), g);
                    }
                }
                thermostat.getOperationIntMap().put(thermostat.getOperations()[i].getOperation(), i);
            }
        }
        thermostat.setName(thermostat.getName());
        add(thermostat);
    }


}
