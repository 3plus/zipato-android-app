/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

/**
 * Created by murielK on 10.6.2014..
 */

import android.app.Activity;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class GestureUtils extends SimpleOnGestureListener {

    public static final int MODE_TRANSPARENT = 0;
    public static final int MODE_SOLID = 1;
    public static final int MODE_DYNAMIC = 2;
    private static final int ACTION_FAKE = -13; //just an unlikely number
    private final Activity context;
    private final GestureDetector detector;
    private final SimpleGestureListener listener;
    private int mode = MODE_DYNAMIC;
    private int swipeMinDistance = 100;
    private int swipeMaxDistance = 700;
    private int swipeMinVelocity = 100;
    private boolean running = true;
    private boolean tapIndicator = false;

    public GestureUtils(Activity context, SimpleGestureListener sgl) {
        this.context = context;
        this.detector = new GestureDetector(context, GestureUtils.this);
        this.listener = sgl;
    }

    public void onTouchEvent(MotionEvent event) {
        if (!this.running) {
            return;
        }

        boolean result = this.detector.onTouchEvent(event);

        if (this.mode == MODE_SOLID) {
            event.setAction(MotionEvent.ACTION_CANCEL);
        } else if (this.mode == MODE_DYNAMIC) {

            if (event.getAction() == ACTION_FAKE) {
                event.setAction(MotionEvent.ACTION_UP);
            } else if (result) {
                event.setAction(MotionEvent.ACTION_CANCEL);
            } else if (this.tapIndicator) {
                event.setAction(MotionEvent.ACTION_DOWN);
                this.tapIndicator = false;
            }
        }
        //else just do nothing, it's Transparent
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int m) {
        this.mode = m;
    }

    public void setEnabled(boolean status) {
        this.running = status;
    }

    public int getSwipeMaxDistance() {
        return this.swipeMaxDistance;
    }

    public void setSwipeMaxDistance(int distance) {
        this.swipeMaxDistance = distance;
    }

    public int getSwipeMinDistance() {
        return this.swipeMinDistance;
    }

    public void setSwipeMinDistance(int distance) {
        this.swipeMinDistance = distance;
    }

    public int getSwipeMinVelocity() {
        return this.swipeMinVelocity;
    }

    public void setSwipeMinVelocity(int distance) {
        this.swipeMinVelocity = distance;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        final float xDistance = Math.abs(e1.getX() - e2.getX());
        final float yDistance = Math.abs(e1.getY() - e2.getY());

        if ((xDistance > (float) swipeMaxDistance) || (yDistance > (float) swipeMaxDistance)) {
            return false;
        }

        velocityX = Math.abs(velocityX);
        velocityY = Math.abs(velocityY);
        boolean result = false;

        if ((velocityX > (float) swipeMinVelocity) && (xDistance > (float) swipeMinDistance)) {
            if (e1.getX() > e2.getX()) // right to left
            {
                this.listener.onSwipe(GestureCommand.LEFT);
            } else {
                this.listener.onSwipe(GestureCommand.RIGHT);
            }

            result = true;
        } else if ((velocityY > (float) swipeMinVelocity) && (yDistance > (float) swipeMinDistance)) {
            if (e1.getY() > e2.getY()) // bottom to up
            {
                this.listener.onSwipe(GestureCommand.UP);
            } else {
                this.listener.onSwipe(GestureCommand.DOWN);
            }

            result = true;
        }

        return result;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        this.tapIndicator = true;
        this.listener.onSingleTap();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg) {
        this.listener.onDoubleTap();

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg) {
        if (this.mode == MODE_DYNAMIC) {        // we owe an ACTION_UP, so we fake an
            arg.setAction(ACTION_FAKE);      //action which will be converted to an ACTION_UP later.
            this.context.dispatchTouchEvent(arg);
        }

        return false;
    }

    public enum GestureCommand {
        UP, DOWN, LEFT, RIGHT, ZOOMIN, ZOOMOUT
    }

    public interface SimpleGestureListener {
        void onSwipe(GestureCommand direction);

        void onDoubleTap();

        void onSingleTap();
    }


}