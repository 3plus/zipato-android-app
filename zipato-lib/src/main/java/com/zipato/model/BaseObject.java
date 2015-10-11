/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import java.util.Comparator;
import java.util.UUID;

/**
 * Created by dbudor on 07/06/2014.
 */
public class BaseObject extends DynaObject implements UUIDObject {

    private String link;
    private UUID uuid;
    private String name;
    private String description;
    private String[] tags;
    private String order;
    public static final Comparator<BaseObject> ORDER_NAME_COMPARATOR = new Comparator<BaseObject>() {
        @Override
        public int compare(BaseObject i1, BaseObject i2) {
            if (i1.getOrder() != null) {
                if (i2.getOrder() == null) {
                    return 1;
                }
//                int cmp = i1.getOrder().compareTo(i2.getOrder());
//                if (cmp != 0) {
//                    return cmp;
//                }
                return (Integer.parseInt(i1.getOrder()) < Integer.parseInt(i2.getOrder())) ? -1 : ((Integer.parseInt(i1.getOrder()) > Integer.parseInt(i2.getOrder())) ? 1 : i1.getName().compareTo(i2.getName()));
            }
            if (i2.getOrder() != null) {
                return -1;
            }

            return i1.getName().compareTo(i2.getName());
        }
    };

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getOrder() {
        if (order == null) {
            return "0";
        }
        if ("".equals(order))
            return "0";
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseObject)) return false;

        BaseObject that = (BaseObject) o;

        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
