/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by murielK on 7/27/2015.
 */
public class LongPressHelper extends Handler {

    private static final String THREAD_NAME = "THREAD_LONG_PRESS_HELPER";
    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_MIN = 0;
    private static final int DEFAULT_START_INTERVAL = 400;
    private static final int DEFAULT_STEPPER = 2;
    private static final int DEFAULT_DEC_INTERVAL = 100;
    private static final int DEFAULT_MIN_INTERVAL = 20;
    private static final int WHAT_INC_DEC = 1;

    private final WeakReference<LongPressController> weakViewController;

    private final int startInterval;
    private final int decInterval;
    private final int minInterval;
    private final int stepper;
    private final int min;
    private final int max;

    private boolean isRunning;
    private boolean decrement;
    private double current;
    private int currentInterval;
    private int viewID;
    private int stepperCount;

    private LongPressHelper(Looper looper, WeakReference<LongPressController> weakViewController,
                            int decInterval, int startInterval, int stepper,
                            int max, int min, int minInterval) {
        super(looper);
        this.decInterval = decInterval;
        this.startInterval = startInterval;
        this.max = max;
        this.min = min;
        this.minInterval = minInterval;
        this.stepper = stepper;
        this.weakViewController = weakViewController;
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == WHAT_INC_DEC)
            performIncDec();
        super.handleMessage(msg);
    }

    private void performIncDec() {
        if (isRunning) {
            stepperCount++;
            if (decrement) {
                current--;
                if (current < min) {
                    current = min;
                    isRunning = false;
                }
            } else {
                current++;
                if (current > max) {
                    current = max;
                    isRunning = false;
                }
            }
            final LongPressController longPressController = weakViewController.get();
            if (longPressController != null)
                longPressController.longPressUpdate(current, viewID);
            else {
                Log.e("LongPressHelper", "Controller null canceling...");
                isRunning = false;
            }
            if (isRunning) {
                if ((stepper != -1) && ((stepperCount % stepper) == 0)) {
                    currentInterval -= decInterval;
                    stepperCount = 0;
                }
                sendEmptyMessageDelayed(WHAT_INC_DEC, (long) ((currentInterval < minInterval) ? minInterval : currentInterval));
            }
        }
    }

    private void reset(double current, boolean decrement, int viewID) {
        currentInterval = startInterval;
        stepperCount = 0;
        this.viewID = viewID;
        this.current = current;
        isRunning = true;
        this.decrement = decrement;
        removeMessages(WHAT_INC_DEC);
    }

    public void start(double current, boolean decrement, int viewID) {
        reset(current, decrement, viewID);
        sendEmptyMessage(WHAT_INC_DEC);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        isRunning = false;
    }

    public interface LongPressController {
        void longPressUpdate(double current, int viewID);
    }

    public static class Builder {
        private final WeakReference<LongPressController> weakViewController;
        private int startInterval;
        private int decInterval;
        private int min;
        private int max;
        private int minInterval;
        private int stepper;

        public Builder(LongPressController longPressController) {
            weakViewController = new WeakReference<>(longPressController);
            startInterval = DEFAULT_START_INTERVAL;
            decInterval = DEFAULT_DEC_INTERVAL;
            min = DEFAULT_MIN;
            max = DEFAULT_MAX;
            minInterval = DEFAULT_MIN_INTERVAL;
            stepper = DEFAULT_STEPPER;
        }

        public Builder setDecInterval(int decInterval) {
            this.decInterval = decInterval;
            return this;
        }

        public Builder setStartInterval(int startInterval) {
            this.startInterval = startInterval;
            return this;
        }

        public Builder setMax(int max) {
            this.max = max;
            return this;
        }

        public Builder setMin(int min) {
            this.min = min;
            return this;
        }

        public Builder setMinInterval(int minInterval) {
            this.minInterval = minInterval;
            return this;
        }

        public Builder setStepper(int stepper) {
            this.stepper = stepper;
            return this;
        }

        public LongPressHelper build() {
            HandlerThread handlerThread = new HandlerThread(THREAD_NAME);
            handlerThread.start();
            return new LongPressHelper(handlerThread.getLooper(), weakViewController, decInterval, startInterval, stepper, max, min, minInterval);
        }
    }

}
