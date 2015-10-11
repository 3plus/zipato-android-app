/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by murielK on 27.6.2014..
 */
public class Configuration extends DynaObject {

    private boolean master;
    private boolean reported;
    private String unit;
    private Map<String, String> enumValues = new LinkedHashMap<String, String>();
    private String status;
    private boolean hidden;
    private int slots;
    private String category;
    private int room;
    private String type;
    private UUID uuid;
    private String name;
    private String description;
    private String[] slotNames;

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public String[] getSlotNames() {
        return slotNames;
    }

    public void setSlotNames(String[] slotNames) {
        this.slotNames = slotNames;
    }

    public Map<String, String> getEnumValues() {

        return enumValues;
    }

//    public void setEnumValues(Map<String, String> enumValues) {
//        this.enumValues = enumValues;
//    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = enumValues;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public boolean isReported() {
        return reported;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
