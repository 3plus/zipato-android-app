/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.mqtt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.zipato.model.attribute.AttributeValueEvent;
import com.zipato.model.attribute.AttributeValueRepository;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * Created by dbudor on 02/06/2014.
 */
public class MqttService extends Service {
    public static final String DEBUG_TAG = "MqttService"; // Debug TAG

    private static final String MQTT_THREAD_NAME = "MqttService[" + DEBUG_TAG + "]"; // Handler Thread ID
    private static final String ACTION_START = DEBUG_TAG + ".START"; // Action to start
    private static final String ACTION_STOP = DEBUG_TAG + ".STOP"; // Action to stop
    private static final String ACTION_KEEPALIVE = DEBUG_TAG + ".KEEPALIVE"; // Action to keep alive used by alarm manager
    private static final String ACTION_RECONNECT = DEBUG_TAG + ".RECONNECT"; // Action to reconnect
    private static final String MQTT_BROKER = "m2m.eclipse.org"; // Broker URL or IP Address
    private static final boolean MQTT_CLEAN_SESSION = true; // Start a clean session?
    private static final String MQTT_URL_FORMAT = "tcp://%s:%d"; // URL Format normally don't change
    private static final String DEVICE_ID_FORMAT = "andr_%s"; // Device ID Format, add any prefix you'd like
    private static final long MQTT_KEEPALIVE = 30000;
    /**
     * Receiver that listens for connectivity changes
     * via ConnectivityManager
     */
    private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(DEBUG_TAG, "Connectivity Changed...");
        }
    };
    // Note: There is a 23 character limit you will get
    // An NPE if you go over that limit
    private boolean started = false; // Is the Client started?
    private AlarmManager alarmManager;            // Alarm manager to perform repeating tasks
    private ConnectivityManager connectivityManager; // To check for connectivity changes
    private MQTT mqtt;
    private CallbackConnection connection;
    private Handler connHandler;
    private transient AttributeValueRepository attributeValueRepository;

    public static void actionStart(Context ctx) {
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    public static void actionStop(Context ctx) {
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    public static void actionKeepalive(Context ctx) {
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        ctx.startService(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread thread = new HandlerThread(MQTT_THREAD_NAME);
        thread.start();

        connHandler = new Handler(thread.getLooper());
        mqtt = new MQTT();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        Log.e(DEBUG_TAG, "Service Started" + action);
        // Log.i(DEBUG_TAG, "Received action of " + action);

        if (action == null) {
            Log.i(DEBUG_TAG, "Starting service with no action\n Probably from a crash");
        } else {
            if (action.equals(ACTION_START)) {
                Log.i(DEBUG_TAG, "Received ACTION_START");

                start();
            } else if (action.equals(ACTION_STOP)) {
                stop();
            } else if (action.equals(ACTION_KEEPALIVE)) {
                keepAlive();
            } else if (action.equals(ACTION_RECONNECT)) {
                if (isNetworkAvailable()) {
                    reconnectIfNecessary();
                }
            }
        }

        return START_REDELIVER_INTENT;
    }

    private synchronized void start() {
        if (started) {
            Log.i(DEBUG_TAG, "Attempt to start while already started");
            return;
        }

        if (hasScheduledKeepAlives()) {
            stopKeepAlives();
        }

        connect();
        registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private synchronized void stop() {
        if (!started) {
            Log.i(DEBUG_TAG, "Attempting to stop connection that isn't running");
            return;
        }

        if (connection != null) {
            connHandler.post(new Runnable() {
                @Override
                public void run() {
                    started = false;
                    stopKeepAlives();
                }
            });
        }

        unregisterReceiver(mConnectivityReceiver);
    }

    /**
     * Connects to the broker with the appropriate datastore
     */
    private synchronized void connect() {
        try {
            mqtt.setHost("tcp://172.16.1.182:1883");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String username = null;
        String password = null;
        String serial = null;
        mqtt.setUserName(username);
        mqtt.setPassword(password);
        mqtt.setClientId(serial);
        mqtt.setReconnectAttemptsMax(10);

        connection = mqtt.callbackConnection();
        final Topic attr = new Topic("attr", QoS.AT_LEAST_ONCE);

        connHandler.post(new Runnable() {
            @Override
            public void run() {
                connection.subscribe(new Topic[]{attr}, new Callback<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        started = true; // Service is now connected
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        connection.disconnect(new Callback<Void>() {
                                                  @Override
                                                  public void onSuccess(Void aVoid) {

                                                  }

                                                  @Override
                                                  public void onFailure(Throwable throwable) {

                                                  }
                                              }
                        );
                    }
                });

                connection.listener(new Listener() {
                    @Override
                    public void onConnected() {

                    }

                    @Override
                    public void onDisconnected() {
                        connection = null;
                    }

                    @Override
                    public void onPublish(UTF8Buffer topic, Buffer payload, Runnable ack) {
                        String t = new String(topic.getData());
                        if (t.startsWith("a")) {
                            publishAttribute(payload);
                        }
                        ack.run();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        connection = null;
                    }
                });

                Log.i(DEBUG_TAG, "Successfully connected and subscribed starting keep alives");
                startKeepAlives();
                connection.getDispatchQueue().execute(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });
    }

    private void publishAttribute(Buffer payload) {
        ByteBuffer bb = payload.toByteBuffer();
        AttributeValueEvent avr = AttributeCodec.decode(bb);
        attributeValueRepository.add(avr);
    }

    /**
     * Schedules keep alives via a PendingIntent
     * in the Alarm Manager
     */
    private void startKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + MQTT_KEEPALIVE,
                MQTT_KEEPALIVE, pi);
    }

    /**
     * Cancels the Pending Intent
     * in the alarm manager
     */
    private void stopKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
    }

    /**
     * Publishes a KeepALive to the topic
     * in the broker
     */
    private synchronized void keepAlive() {
        if (isConnected()) {
            try {
                sendKeepAlive();
                return;
            } catch (MqttConnectivityException ex) {
                ex.printStackTrace();
                reconnectIfNecessary();
            } catch (Exception e) {

                stop();
            }
        }
    }

    /**
     * Checkes the current connectivity
     * and reconnects if it is required.
     */
    private synchronized void reconnectIfNecessary() {
        if (started && connection == null) {
            connect();
        }
    }

    /**
     * Query's the NetworkInfo via ConnectivityManager
     * to return the current connected state
     *
     * @return boolean true if we are connected false otherwise
     */
    private boolean isNetworkAvailable() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        return (info != null) && info.isConnected();
    }

    /**
     * Verifies the client State with our local connected state
     *
     * @return true if its a match we are connected false if we aren't connected
     */
    private boolean isConnected() {
        if (started && connection != null) {
            return true;
        }
        return false;
    }

    private synchronized void sendKeepAlive() throws MqttConnectivityException {

    }

    /**
     * Query's the AlarmManager to check if there is
     * a keep alive currently scheduled
     *
     * @return true if there is currently one scheduled false otherwise
     */
    private synchronized boolean hasScheduledKeepAlives() {
        Intent i = new Intent();
        i.setClass(this, MqttService.class);
        i.setAction(ACTION_KEEPALIVE);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);

        return (pi != null);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * MqttConnectivityException Exception class
     */
    private class MqttConnectivityException extends Exception {
        private static final long serialVersionUID = -7385866796799469420L;
    }
}

