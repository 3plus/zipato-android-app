/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zipato.appv2.B;import com.zipato.appv2.R;

/**
 * Created by murielK on 8/18/2014.
 */

public class SecurityController extends View {


    private static final String MY_TAG = SecurityController.class.getSimpleName();
    private float mTranslateX;
    private float mTranslateY;
    private Drawable normal;
    private Drawable onDisarm;
    private Drawable onArmHome;
    private Drawable onArmAway;
    private Drawable onDisarmPress;
    private Drawable onArmHomePress;
    private Drawable onArmAwayPress;
    private Drawable locker;
    private Matrix matrix;
    private StateMode stateMode = StateMode.NORMAL;
    private StateMode nextState = StateMode.NORMAL;
    private StateMode cacheState = StateMode.NORMAL;
    private Rect bound = new Rect();
    private OnChangeListner listner;
    private OnTouchListener touchListener;

    public SecurityController(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SecurityController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SecurityController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);

    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.SecurityController, defStyleAttr, 0);

            Drawable normal = a.getDrawable(R.styleable.SecurityController_drawable_normal);
//            if (normal == null)
//            throw new NullPointerException("no drawable found for state: normal");
            this.normal = normal;
            Drawable onDisarm = a.getDrawable(R.styleable.SecurityController_drawable_onDisarm);
//            if (onDisarm == null)
//                throw new NullPointerException("no drawable found for state: onDisarm");
            this.onDisarm = onDisarm;
            Drawable onArmHome = a.getDrawable(R.styleable.SecurityController_drawable_onArmHome);
//            if (onArmHome == null)
//                throw new NullPointerException("no drawable found for state: onArmHome");
            this.onArmHome = onArmHome;
            Drawable onArmAway = a.getDrawable(R.styleable.SecurityController_drawable_onArmAway);
//            if (onArmAway == null)
//                throw new NullPointerException("no drawable found for state: onArmAway");
            this.onArmAway = onArmAway;
            Drawable onDisarmPress = a.getDrawable(R.styleable.SecurityController_drawable_onDisarmPress);
//            if (onDisarmPress == null)
//                throw new NullPointerException("no drawable found for state: onDisarmPress");
            this.onDisarmPress = onDisarmPress;
            Drawable onArmHomePress = a.getDrawable(R.styleable.SecurityController_drawable_onArmHomePress);
//            if (onArmHomePress == null)
//                throw new NullPointerException("You no drawable found for state: onArmHomePress");
            this.onArmHomePress = onArmHomePress;
            Drawable onArmAwayPress = a.getDrawable(R.styleable.SecurityController_drawable_getOnArmAwayPress);
//            if (onArmAwayPress == null)
//                throw new NullPointerException("You no drawable found for state: onArmAwayPress");
            this.onArmAwayPress = onArmAwayPress;
            Drawable locker = a.getDrawable(R.styleable.SecurityController_drawable_locker);
//            if (locker == null)
//                throw new NullPointerException("You no drawable found for state: locker");

            this.locker = locker;


        }


    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (stateMode) {
            case NORMAL:
                normal.setBounds(bound);
                normal.draw(canvas);
                break;
            case DISARMED:
                onDisarm.setBounds(bound);
                onDisarm.draw(canvas);
                break;
            case DISARM_PRESSED:
                onDisarmPress.setBounds(bound);
                onDisarmPress.draw(canvas);
                break;
            case HOME:
                onArmHome.setBounds(bound);
                onArmHome.draw(canvas);
                break;
            case AWAY:
                onArmAway.setBounds(bound);
                onArmAway.draw(canvas);
                break;
            case AWAY_PRESSED:
                onArmAwayPress.setBounds(bound);
                onArmAwayPress.draw(canvas);
                break;
            case HOME_PRESSED:
                onArmHomePress.setBounds(bound);
                onArmHomePress.draw(canvas);
                break;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        setMeasuredDimension(width, height);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {

        final int min = Math.min(width, height);
        float top = 0;
        float left = 0;
        int arcDiameter = 0;
        mTranslateX = (width * 0.5f);
        mTranslateY = (height * 0.5f);
        //bound.set(-min,-min,min,min);
        bound.set(0, 0, min, min);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listner != null)
                    listner.onStartTracking();
                cacheState = stateMode;
                setPressed(true);
                actionDownHandler(event);
                invalidate();

                break;
            case MotionEvent.ACTION_MOVE:
                actionDownHandler(event);
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
                if (listner != null)
                    listner.onStopTracking();
                actionUp(event);
                setPressed(false);
                break;
            case MotionEvent.ACTION_CANCEL://
                if (listner != null)
                    listner.onStopTracking();
                actionUp(event);
                setPressed(false);
                break;
        }

        return true;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2));

        if (angle < 0) {
            angle = 360 + angle;
        }
        //angle
        Log.d(MY_TAG, " Angle " + angle);
        return angle;
    }

    private void actionDownHandler(MotionEvent event) {

        double angle = getTouchDegrees(event.getX(), event.getY());
        if ((0 < angle) && (angle < 119)) {
            stateMode = StateMode.AWAY_PRESSED;
            nextState = StateMode.AWAY;
        } else if ((120 < angle) && (angle < 249)) {

            stateMode = StateMode.DISARM_PRESSED;
            nextState = StateMode.DISARMED;
        } else if ((240 < angle) && (angle < 359)) {
            stateMode = StateMode.HOME_PRESSED;
            nextState = StateMode.HOME;
        }

    }

    private void actionUp(MotionEvent event) {
        int cx = (int) (event.getX() - mTranslateX);
        int cy = (int) (event.getY() - mTranslateY);
        double touchRadius = Math.sqrt(cx * cx + cy * cy);
        if (touchRadius > mTranslateX) {
            stateMode = cacheState;

        } else {
            stateMode = nextState;
            switch (stateMode) {
                case HOME:
                    Log.d(MY_TAG, " Arm Away ");
                    if (listner != null)
                        listner.onArmHome(true);
                    break;
                case DISARMED:
                    Log.d(MY_TAG, " Disarm ");
                    if (listner != null)
                        listner.onDisarm(true);
                    break;
                case AWAY:
                    Log.d(MY_TAG, " Arm Home ");
                    if (listner != null)
                        listner.onArmAway(true);
                    break;
                default:
                    break;

            }
        }
        invalidate();
        setPressed(false);

    }

    public void setOnTouchListner(OnTouchListener listner) {
        touchListener = listner;
    }

    public void setOnchangeListner(OnChangeListner listner) {
        this.listner = listner;
    }

    public StateMode getCurrentMode() {

        return stateMode;
    }

    public void setCurrentMode(String stateMode, boolean isOngoing) {
        if (StateMode.DISARMED.name().equalsIgnoreCase(stateMode)) {

            if (isOngoing) {
                this.stateMode = StateMode.DISARM_PRESSED;
            } else {
                this.stateMode = StateMode.DISARMED;
            }
            invalidate();
            if (listner != null)
                listner.onDisarm(false);

        } else if (StateMode.HOME.name().equalsIgnoreCase(stateMode)) {
            if (isOngoing) {
                this.stateMode = StateMode.HOME_PRESSED;
            } else {
                this.stateMode = StateMode.HOME;
            }
            invalidate();
            if (listner != null)
                listner.onArmAway(false);
        } else if (StateMode.AWAY.name().equalsIgnoreCase(stateMode)) {

            if (isOngoing) {
                this.stateMode = StateMode.AWAY_PRESSED;
            } else {
                this.stateMode = StateMode.AWAY;
            }
            invalidate();
            if (listner != null)
                listner.onArmHome(false);
        }

    }

    private enum StateMode {

        NORMAL, DISARMED, AWAY, HOME, DISARM_PRESSED, HOME_PRESSED, AWAY_PRESSED
    }

    public interface OnChangeListner {

        void onArmHome(boolean fromUser);

        void onArmAway(boolean fromUser);

        void onDisarm(boolean fromUser);

        void onStartTracking();

        void onStopTracking();
    }


}
