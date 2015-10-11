/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by murielK on 7/30/2014.
 */
public class CustomViewPager extends ViewPager {

    private static final String TAG = CustomViewPager.class.getSimpleName();
    private boolean enableSwipe = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnableSwipe(boolean enableSwipe) {
        this.enableSwipe = enableSwipe;
        Log.d(TAG, "Viewpager can swipe? " + enableSwipe);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return enableSwipe && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enableSwipe && super.onInterceptTouchEvent(ev);
    }
}
