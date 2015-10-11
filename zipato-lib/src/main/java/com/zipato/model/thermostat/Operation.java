/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.thermostat;

import android.util.SparseIntArray;

import com.zipato.model.BaseObject;
import com.zipato.model.attribute.Attribute;


/**
 * Created by murielK on 7/23/2014.
 */
public class Operation extends BaseObject {

    private ThermoOutputs[] outputs;
    private EnumOperation operation;
    private boolean showIcon;
    private Attribute[] attributes;
    private ThermosConfiguration config;
    private SparseIntArray attributeIntMap = new SparseIntArray();

    public ThermoOutputs[] getOutputs() {
        return outputs;
    }

    public void setOutputs(ThermoOutputs[] outputs) {
        this.outputs = outputs;
    }

    public ThermosConfiguration getConfig() {
        return config;
    }

    public void setConfig(ThermosConfiguration config) {
        this.config = config;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public SparseIntArray getAttributeIntMap() {
        return attributeIntMap;
    }

    public void setAttributeIntMap(SparseIntArray attributeIntMap) {
        this.attributeIntMap = attributeIntMap;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
    }
}
