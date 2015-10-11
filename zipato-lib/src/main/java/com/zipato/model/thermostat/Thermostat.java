/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.thermostat;

import com.zipato.model.BaseObject;

import java.util.HashMap;

/**
 * Created by murielK on 7/23/2014.
 */
public class Thermostat extends BaseObject {

    private boolean showIcon;
    private boolean fahrenheit;
    private Operation[] operations;
    private HashMap<EnumOperation, Integer> operationIntMap = new HashMap<EnumOperation, Integer>();
    private ThermosConfiguration config;

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(boolean fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public ThermosConfiguration getConfig() {
        return config;
    }

    public void setConfig(ThermosConfiguration config) {
        this.config = config;
    }

    public HashMap<EnumOperation, Integer> getOperationIntMap() {
        return operationIntMap;
    }

    public void setOperationIntMap(HashMap<EnumOperation, Integer> operationIntMap) {
        this.operationIntMap = operationIntMap;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public void setOperations(Operation[] operations) {
        this.operations = operations;
    }


}
