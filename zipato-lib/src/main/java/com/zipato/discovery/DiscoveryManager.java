/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.discovery;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectBonjour;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import de.greenrobot.event.EventBus;

@SuppressWarnings("NonPrivateFieldAccessedInSynchronizedContext")
public class DiscoveryManager {
    private static final String ZIPABOX_TCP_LOCAL = "_zipabox._tcp.local.";
    private final Object lock = new Object();
    @Inject
    EventBus eventBus;
    @Inject
    ExecutorService executor;
    private WifiManager.MulticastLock multicastLock;
    private JmDNS jmdns;
    private Set<ZipatoServiceInfo> services;
    private ServiceListener listener = new ServiceListener() {
        @Override
        public void serviceResolved(ServiceEvent ev) {
            synchronized (lock) {
                for (String serverAddress : ev.getInfo().getHostAddresses()) {
                    ZipatoServiceInfo zipatoServiceInfo = new ZipatoServiceInfo(ev.getInfo(), serverAddress);
                    services.add(zipatoServiceInfo);
                    eventBus.post(new Event(new ObjectBonjour(ObjectBonjour.ADD, zipatoServiceInfo), Event.EVENT_TYPE_BONJOUR_EVENT));
                }
            }
        }

        @Override
        public void serviceRemoved(ServiceEvent ev) {
            synchronized (lock) {
                for (String serverAddress : ev.getInfo().getHostAddresses()) {
                    ZipatoServiceInfo zipatoServiceInfo = new ZipatoServiceInfo(ev.getInfo(), serverAddress);
                    services.remove(zipatoServiceInfo);
                    eventBus.post(new Event(new ObjectBonjour(ObjectBonjour.REMOVE, zipatoServiceInfo), Event.EVENT_TYPE_BONJOUR_EVENT));
                }
            }
        }

        @Override
        public void serviceAdded(ServiceEvent event) {
            jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
        }
    };
    private boolean listening = false;

    public void start(final Context context) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                _start(context);
            }
        });
    }

    private void _start(Context context) {
        synchronized (lock) {
            try {
                listening = true;
                services = new HashSet<ZipatoServiceInfo>();
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                multicastLock = wifi.createMulticastLock("mylockthereturn2");
                multicastLock.setReferenceCounted(true);
                multicastLock.acquire();
                String ip = Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress());
                InetAddress iNet = InetAddress.getByName(ip);
                jmdns = JmDNS.create(iNet, ZIPABOX_TCP_LOCAL);
                jmdns.addServiceListener(ZIPABOX_TCP_LOCAL, listener);
            } catch (IOException e) {
                e.printStackTrace();
                stop();
            }
        }
    }

    public void stop() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                _stop();
            }
        });
    }

    private void _stop() {
        synchronized (lock) {
            if (jmdns != null) {
                listening = false;
                jmdns.removeServiceListener(ZIPABOX_TCP_LOCAL, listener);
                jmdns.unregisterAllServices();
                try {
                    jmdns.close();
                    jmdns = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                services = null;
            }
            if (multicastLock != null) {
                multicastLock.release();
                multicastLock = null;
            }
        }
    }

    public List<ZipatoServiceInfo> getServices() {
        if (services != null) {
            return new ArrayList<ZipatoServiceInfo>(services);
        } else {
            return null;
        }
    }

    public boolean isListening() {
        return listening;
    }
}