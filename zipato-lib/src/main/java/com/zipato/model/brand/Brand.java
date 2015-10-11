/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.brand;

import com.zipato.model.BaseObject;
import com.zipato.model.network.Network;

/**
 * Created by murielK on 8/13/2014.
 */
public class Brand extends BaseObject {

    private Availability avail;
    private Network[] networks;
    private BrandDevice[] devices;
    private boolean available;

    public Availability getAvail() {
        return avail;
    }

    public void setAvail(Availability avail) {
        this.avail = avail;
    }

    public Network[] getNetworks() {
        return networks;
    }

    public void setNetworks(Network[] networks) {
        this.networks = networks;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public BrandDevice[] getDevices() {
        return devices;
    }

    public void setDevices(BrandDevice[] devices) {
        this.devices = devices;
    }
}
