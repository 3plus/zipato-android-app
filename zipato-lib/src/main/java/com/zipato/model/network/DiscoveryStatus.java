/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.network;

import com.zipato.model.BaseObject;
import com.zipato.model.device.Device;

import java.util.Date;

/**
 * Created by murielK on 8/20/2014.
 */
public class DiscoveryStatus extends BaseObject {

    private Network network;
    private Device[] devices;
    private Date createTimestamp;
    private Date startTimestamp;
    private Date stopTimestamp;
    private boolean running;
    private MessageFinale[] messages;

    public MessageFinale[] getMessages() {
        return messages;
    }

    public void setMessages(MessageFinale[] messages) {
        this.messages = messages;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Device[] getDevices() {
        return devices;
    }

    public void setDevices(Device[] devices) {
        this.devices = devices;
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Date createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Date getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Date startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Date getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(Date stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
