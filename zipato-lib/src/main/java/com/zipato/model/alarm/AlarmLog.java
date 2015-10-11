/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.alarm;

import com.zipato.model.DynaObject;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by murielK on 9/2/2014.
 */
public class AlarmLog extends DynaObject {

    private AlarmLevel level;
    private int alarmId;
    private Date timestamp;
    public static final Comparator<AlarmLog> DATE_COMPARATOR = new Comparator<AlarmLog>() {

        @Override
        public int compare(AlarmLog i1, AlarmLog i2) {

            return i2.getTimestamp().getTime() < i1.getTimestamp().getTime() ? -1 :
                    i2.getTimestamp().getTime() > i1.getTimestamp().getTime() ? 1 : 0;
        }
    };
    private String message;
    private int code;
    private String subtype;
    private int subtypeId;
    private String subtypeUuid;
    private boolean needAck;
    private int id;

    public AlarmLevel getLevel() {
        return level;
    }

    public void setLevel(AlarmLevel level) {
        this.level = level;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public int getSubtypeId() {
        return subtypeId;
    }

    public void setSubtypeId(int subtypeId) {
        this.subtypeId = subtypeId;
    }

    public String getSubtypeUuid() {
        return subtypeUuid;
    }

    public void setSubtypeUuid(String subtypeUuid) {
        this.subtypeUuid = subtypeUuid;
    }

    public boolean isNeedAck() {
        return needAck;
    }

    public void setNeedAck(boolean needAck) {
        this.needAck = needAck;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
