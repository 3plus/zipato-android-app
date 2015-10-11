/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.discovery;

import javax.jmdns.ServiceInfo;

public class ZipatoServiceInfo {

    private final String mac;
    private final String serial;
    private final String name;
    private final String bonjourName;
    private final String ip;
    private final int port;

    public ZipatoServiceInfo(ServiceInfo info, String serverAddress) {
        bonjourName = info.getName();
//        if (bonjourName == null) {
//            throw new NullPointerException();
//        }
        name = info.getPropertyString("name");
        ip = serverAddress;
        mac = info.getPropertyString("mac");
        serial = info.getPropertyString("serial");
        port = info.getPort();
    }

    public String fullName() {
        String fullName = "";

        if (this.name != null) {
            fullName += name + "\n";
        }

        fullName += bonjourName.replace("Zipabox-", "") + " (" + address() + ")";

        return fullName;
    }

    public String address() {
        return "http://" + ip + ":" + port + "/";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ZipatoServiceInfo)) return false;

        ZipatoServiceInfo that = (ZipatoServiceInfo) o;

        if (!ip.equals(that.ip)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ip.hashCode();
    }

    public String getMac() {
        return mac;
    }

    public String getSerial() {
        return serial;
    }

    public String getName() {
        return name;
    }

    public String getBonjourName() {
        return bonjourName;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}