/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.typereport;

import android.util.SparseIntArray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zipato.model.DynaObject;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.device.DeviceState;
import com.zipato.model.types.UserIcons;

import java.util.Comparator;
import java.util.Date;
import java.util.UUID;

/**
 * Created by murielK on 2.6.2014..
 */
public class TypeReportItem extends DynaObject {

    @JsonIgnore
    private SparseIntArray map;

    private String templateId;
    private UUID uuid;
    private EntityType entityType;
    private String link;
    private String name;
    private String description;
    private String endpointType;
    private boolean show;
    private UiType UiType;
    private int room;
    private String location;
    private Attribute[] attributes;
    private Date lastValueTimestamp;
    private String order;

    public static final Comparator<TypeReportItem> ORDER_NAME_COMPARATOR = new Comparator<TypeReportItem>() {
        @Override
        public int compare(TypeReportItem i1, TypeReportItem i2) {
            if (i1.getOrder() != null) {
                if (i2.getOrder() == null) {
                    return 1;
                }
                int cmp = i1.getOrder().compareTo(i2.getOrder());
                if (cmp != 0) {
                    return cmp;
                }
                return i1.getName().compareTo(i2.getName());
            }
            if (i2.getOrder() != null) {
                return -1;
            }
            return i1.getName().compareTo(i2.getName());
        }
    };
    private String[] tags;
    private int masterIndex;
    @JsonIgnore
    private transient TypeReportKey key;
    private DeviceState deviceState;
    private UserIcons userIcon;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeReportItem)) return false;

        TypeReportItem item = (TypeReportItem) o;

        if (entityType != item.entityType) return false;
        return uuid.equals(item.uuid);

    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = (31 * result) + entityType.hashCode();
        return result;
    }

// Getters & setters

    public void setUpAttributes() {
        if (attributes == null)
            return;
        final int size = attributes.length;
        for (int i = 0; i < size; i++) {
            if (map == null)
                map = new SparseIntArray();
            final Attribute attribute = attributes[i];
            if (attribute.isMaster())
                masterIndex = i;
            map.put(attribute.getAttributeId(), i);
        }

    }

    public Attribute getAttrOfID(int attrID) {
        if ((map == null) || (attributes == null))
            return null;
        return attributes[map.get(attrID)];
    }

    public int getIndexOfID(int attrID) {
        if (map == null)
            return -1;
        return map.get(attrID);
    }

    public UserIcons getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(UserIcons userIcon) {
        this.userIcon = userIcon;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public int getMasterIndex() {
        return masterIndex;
    }

    public void setMasterIndex(int masterIndex) {
        this.masterIndex = masterIndex;
    }

    @JsonIgnore
    public TypeReportKey getKey() {
        if (key == null) {
            key = new TypeReportKey(this);
        }
        return key;
    }


    public void setKey(TypeReportKey key) {
        this.key = key;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public UiType getUiType() {
        return UiType;
    }

    public void setUiType(UiType uiType) {
        UiType = uiType;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public Attribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(Attribute[] attributes) {
        this.attributes = attributes;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getLastValueTimestamp() {
        return lastValueTimestamp;
    }

    public void setLastValueTimestamp(Date lastValueTimestamp) {
        this.lastValueTimestamp = lastValueTimestamp;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

}

