/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.attribute.AttributeType;
import com.zipato.model.attribute.AttributeValue;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.translation.LanguageManager;

import java.util.UUID;

/**
 * Created by murielK on 8/24/2015.
 */
public class AttributesHelper {

    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final LanguageManager languageManager;

    public AttributesHelper(AttributeRepository attributeRepository, AttributeValueRepository attributeValueRepository, LanguageManager languageManager) {
        this.attributeRepository = attributeRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.languageManager = languageManager;
    }

    public String attrValueResolver(final UUID attrUUID, final String value) {
        final Attribute attribute = attributeRepository.get(attrUUID);
        if (attribute == null)
            return "-";
        if (value == null)
            return "-";

        if ((attribute.getConfig() == null))
            return languageManager.translate(value.toLowerCase());

        if ((attribute.getConfig().getEnumValues() != null) && (attribute.getConfig().getEnumValues().get(value) != null))
            return languageManager.translate(attribute.getConfig().getEnumValues().get(value).toLowerCase());

        return value + ((attribute.getConfig().getUnit() == null) ? "" : attribute.getConfig().getUnit());
    }

    public String attrValueResolver(final UUID attrUUID) {
        String value = "";
        try {
            value = attributeValueRepository.get(attrUUID).getValue().toString();
        } catch (Exception e) {
            //empty
        }
        return attrValueResolver(attrUUID, value);
    }

    public boolean isStateIconTrue(final UUID attrUUID) {

        final AttributeValue attributeValue = attributeValueRepository.get(attrUUID);
        if (attributeValue == null)
            return false;
        final String value = (attributeValue.getValue() == null) ? null : attributeValue.getValue().toString();
        return isStateIconTrue(value, attrUUID);
    }

    public boolean isStateIconTrue(final String value, final UUID attrUUID) {
        if (value == null)
            return false;
        final Attribute attribute = attributeRepository.get(attrUUID);
        if (attribute == null)
            return false;
        try {
            if ((AttributeType.BOOLEAN == attribute.getDefinition().getAttributeType()) && "true".equalsIgnoreCase(value) /* && (attribute.getConfig().getEnumValues() != null)*/) {
                return true;
            } else if ("com.zipato.cluster.LevelControl".equals(attribute.getDefinition().getCluster())) {
                int level = Integer.valueOf(value);
                if (level > 0)
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Attribute getTypeReportAttrFor(int attrID, TypeReportItem item) {
        if (item == null)
            return null;
        if (item.getEntityType() == EntityType.ATTRIBUTE)
            return attributeRepository.get(item.getUuid());
        return item.getAttrOfID(attrID);
    }
}
