/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.zipato.model.BaseEntityType;
import com.zipato.model.endpoint.Endpoint;

/**
 * Created by murielK on 4.6.2014..
 */
public class Device extends BaseEntityType {
    private String templateId;
    //@JsonManagedReference
    //@JsonSerialize(contentUsing = UuidObjectSerializer.class)
    @JsonManagedReference
    private Endpoint[] endpoints;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Override
    @JsonIgnore
    public Endpoint[] getChildren() {
        return endpoints;
    }

    public Endpoint[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoint[] endpoints) {
        this.endpoints = endpoints;
    }


}
