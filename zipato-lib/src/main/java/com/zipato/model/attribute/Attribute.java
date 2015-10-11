/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.attribute;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zipato.model.BaseEntityType;
import com.zipato.model.BaseObject;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 2.6.2014..
 */
public class Attribute extends BaseEntityType {


    private boolean master;
    private AttributeValue value;
    private AttributeDefinition definition;

    private transient TypeReportItem typeReportItem;
    @JsonBackReference("parent")
    private ClusterEndpoint clusterEndpoint;
    private int attributeId;

    @JsonIgnore
    public TypeReportItem getTypeReportItem() {
        return typeReportItem;
    }

    @JsonIgnore
    public void setTypeReportItem(TypeReportItem typeReportItem) {
        this.typeReportItem = typeReportItem;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    public AttributeDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(AttributeDefinition definition) {
        this.definition = definition;
    }
    // Getters & setters

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public AttributeValue getValue() {
        return value;
    }

    public void setValue(AttributeValue value) {
        this.value = value;
    }

    //@JsonSerialize(using = UuidObjectSerializer.class)
    public ClusterEndpoint getClusterEndpoint() {
        return clusterEndpoint;
    }

    public void setClusterEndpoint(ClusterEndpoint clusterEndpoint) {
        this.clusterEndpoint = clusterEndpoint;
    }

    @Override
    public BaseObject getParent() {
        return clusterEndpoint;
    }

    @Override
    @JsonIgnore
    public BaseObject[] getChildren() {
        return null;
    }

}
