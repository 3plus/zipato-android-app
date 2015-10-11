/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.endpoint;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.zipato.model.BaseEntityType;
import com.zipato.model.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murielK on 5.6.2014..
 */
public class ClusterEndpoint extends BaseEntityType {

    @JsonManagedReference
    private List<Attribute> attributes;

    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    @JsonIgnore
    public Attribute[] getChildren() {
        if (attributes == null)
            return null;
        return attributes.toArray(new Attribute[attributes.size()]);
    }

    public void addAttribute(Attribute attr) {
        if (attributes == null)
            attributes = new ArrayList<Attribute>();
        attributes.add(attr);
    }
}
