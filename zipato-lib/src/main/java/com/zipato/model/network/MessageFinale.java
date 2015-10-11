/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.network;

import com.zipato.model.DynaObject;

import java.util.Date;

/**
 * Created by murielK on 8/20/2014.
 */
public class MessageFinale extends DynaObject {

    private Date timestamp;
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
