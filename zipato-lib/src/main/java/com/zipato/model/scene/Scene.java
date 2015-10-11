/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zipato.model.BaseObject;

/**
 * Created by murielK on 30.6.2014..
 */
public class Scene extends BaseObject {

    @JsonIgnore
    public static final int FLAG_DELETED = 1;
    @JsonIgnore
    public static final int FLAG_CREATED = 2;

    @JsonIgnore
    private int flag;
    private String icon;
    private String iconColor;
    private String[] tags;
    private SceneSetting[] settings;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public String[] getTags() {
        return tags;
    }

    @Override
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public SceneSetting[] getSettings() {
        return settings;
    }

    public void setSettings(SceneSetting[] settings) {
        this.settings = settings;
    }

    public String getIconColor() {
        return iconColor;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
