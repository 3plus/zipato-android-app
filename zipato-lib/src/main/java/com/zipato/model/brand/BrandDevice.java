/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.brand;

import com.zipato.model.BaseObject;
import com.zipato.model.typereport.UiType;

/**
 * Created by murielK on 8/13/2014.
 */
public class BrandDevice extends BaseObject {

    private int id;
    private String manufacturer;
    private String model;
    private String firmware;
    private UiType icon;
    private DiscoveryData discoveryData;


    public DiscoveryData getDiscoveryData() {
        return discoveryData;
    }

    public void setDiscoveryData(DiscoveryData discoveryData) {
        this.discoveryData = discoveryData;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    public UiType getIcon() {
        return icon;
    }

    public void setIcon(UiType icon) {
        this.icon = icon;
    }

}
