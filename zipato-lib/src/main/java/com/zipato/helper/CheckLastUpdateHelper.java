/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by murielK on 1/26/2015.
 */
public class CheckLastUpdateHelper {

    private static final long SEC_TO_MILLIS = 1000L;
    private static final long DEFAULT_DELAY = 30000L;
    private long delay = DEFAULT_DELAY;
    private Timer timer;
    private long lastUpdate;
    private OnNoUpdatedListner listner;

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }


    public void startStatusChecker() {
        stopStatusChecker();
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                if ((System.currentTimeMillis() - lastUpdate) > delay) {
                    if (listner != null)
                        listner.notUpdate();

                }

            }
        };
        timer.scheduleAtFixedRate(timerTask, delay * 3, delay);

    }

    public void startStatusChecker(OnNoUpdatedListner listner) {
        this.listner = listner;
        startStatusChecker();
    }

    public String wasUpdated() {
        if (lastUpdate == 0)
            return "was never fucking updated";
        return "was updated: " + ((System.currentTimeMillis() - lastUpdate) / SEC_TO_MILLIS) + " second ago";
    }

    public void stopStatusChecker() {
        if (timer == null)
            return;
        timer.cancel();
        timer.purge();
        listner = null;
        timer = null;
    }

    public void setListner(OnNoUpdatedListner listner) {
        this.listner = listner;
    }

    public interface OnNoUpdatedListner {

        void notUpdate();
    }
}
