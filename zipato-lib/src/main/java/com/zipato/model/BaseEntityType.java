/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.zipato.model.typereport.UiType;
import com.zipato.model.types.UserIcons;

/**
 * Created by murielK on 10/24/2014.
 */
public class BaseEntityType extends BaseObject implements Parent {

    @JsonBackReference
    private BaseObject parent;
    private boolean showIcon;
    private UserIcons userIcon;
    private UiType icon;
    private Configuration config;


    public BaseObject getParent() {
        return parent;
    }

    // @JsonIgnore
    public void setParent(BaseObject parent) {
        this.parent = parent;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public UserIcons getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(UserIcons userIcon) {
        this.userIcon = userIcon;
    }

    public UiType getIcon() {
        return icon;
    }

    public void setIcon(UiType icon) {
        this.icon = icon;
    }

    public Configuration getConfig() {
        return config;
    }

    public void setConfig(Configuration config) {
        this.config = config;
    }

    @Override
    public BaseObject[] getChildren() {
        return new BaseObject[0];
    }
}
