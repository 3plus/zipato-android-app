/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.typereport.EntityType;

import javax.inject.Inject;

/**
 * Created by murielK on 8/7/2014.
 */
public class AttributesFragment extends DmFragment<Attribute> {

    private static final String TAG = "AttributesFragment";

    @Inject
    AttributeRepository attributeRepository;

    @Override
    protected Class<? extends DmFragment<?>> getChildFragmentClass() {
        return null;
    }

    @Override
    protected int getIndex() {
        return 4;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    protected UUIDObjectRepository<Attribute> getRepository() {
        return attributeRepository;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.ATTRIBUTE;
    }
}
