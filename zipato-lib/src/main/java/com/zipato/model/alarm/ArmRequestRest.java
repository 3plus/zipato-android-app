/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murielK on 8/28/2014.
 */
public class ArmRequestRest {

    public ArmMode armMode;
    public String secureSessionId;
    public List<String> bypassZones;

    public ArmMode getArmMode() {
        return armMode;
    }

    public void setArmMode(ArmMode armMode) {
        this.armMode = armMode;
    }

    public String getSecureSessionId() {
        return secureSessionId;
    }

    public void setSecureSessionId(String secureSessionId) {
        this.secureSessionId = secureSessionId;
    }

    public List<String> getBypassZones() {
        return bypassZones;
    }

    public void setBypassZones(List<String> bypassZones) {
        this.bypassZones = bypassZones;
    }

    public void addBypassZones(String uuid) {
        if (bypassZones == null)
            bypassZones = new ArrayList<String>();
        bypassZones.add(uuid);
    }

    public void removeBypassZones(String uuid) {
        if (bypassZones != null)
            bypassZones.remove(uuid);
    }
}
