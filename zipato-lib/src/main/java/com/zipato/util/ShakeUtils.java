/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

/**
 * Created by murielK on 10/30/2014.
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Muriel on 4/5/2014.
 */
public class ShakeUtils implements SensorEventListener {

    private static final int TIME_THRESHOLD = 225;
    private static final int SHAKE_DURATION = 300;
    private static final int SHAKE_COUNT = 3;
    private static ShakeUtils shakeUtilsInstance;
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Context context;
    private boolean isShake;
    private int forceThreshold = 500;
    private int shakeTimeout = 500;
    private float x;
    private float y;
    private float z;
    private float lastX = -1.0f, lastY = -1.0f, lastZ = -1.0f;
    private long lastTime;
    private OnShakeListener mShakeListener;
    private int shakeCount = 0;
    private long lastShake;
    private long lastForce;

    public ShakeUtils(Context context) {
        this.context = context;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public static ShakeUtils getShakeInstance(Context context) {

        if (shakeUtilsInstance == null) {
            synchronized (ShakeUtils.class) {
                if (shakeUtilsInstance == null)
                    shakeUtilsInstance = new ShakeUtils(context);
            }
        }
        return shakeUtilsInstance;
    }

    public void setForceTimeOut(int force, int timeOut) {

        forceThreshold = force;
        shakeTimeout = timeOut;
    }

    public void setOnShakeListener(OnShakeListener onShakeListener) {
        mShakeListener = onShakeListener;
    }

    public void setEnableShake(boolean enable) {

        isShake = enable;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isShake)
            return;
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

//        Log.d("Shake", String.format("X  = %f", x));
//        Log.d("Shake", String.format("Y  = %f", y));
//        Log.d("Shake", String.format("Z  = %f", z));
        long now = System.currentTimeMillis();

        if ((now - lastForce) > shakeTimeout) {
            shakeCount = 0;
        }

        if ((now - lastTime) > TIME_THRESHOLD) {
            long diff = now - lastTime;

            float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diff * 10000;
            if (speed > forceThreshold) {
                if ((++shakeCount >= SHAKE_COUNT) && (now - lastShake > SHAKE_DURATION)) {
                    lastShake = now;
                    shakeCount = 0;
                    if (isShake) {
                        if (mShakeListener != null)
                            mShakeListener.onShake();
                    }
                }
                lastForce = now;
            }
            lastTime = now;
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void registerToSensor() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        Log.d("myShake", "Registered");
        Log.d("myShake", String.format("Shake time out = %d", shakeTimeout));
        Log.d("myShake", String.format("Shake force threshold = %d", forceThreshold));

    }

    public void unRegisterToSensor() {
        sensorManager.unregisterListener(this);
        Log.d("myShake", "unRegistered");
        Log.d("myShake", String.format("Shake time out = %d", shakeTimeout));
        Log.d("myShake", String.format("Shake force threshold = %d", forceThreshold));
    }

    public OnShakeListener getShakeListener() {
        return mShakeListener;
    }

    public interface OnShakeListener {
        void onShake();
    }
}