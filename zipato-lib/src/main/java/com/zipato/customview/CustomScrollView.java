/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Mur0 on 11/15/2014.
 */
public class CustomScrollView extends ScrollView {
    private static final String TAG = CustomViewPager.class.getSimpleName();
    private boolean enableScrolling = true;

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setEnableScrolling(boolean enableScrolling) {
        this.enableScrolling = enableScrolling;
        Log.d(TAG, "Viewpager can swipe? " + enableScrolling);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {


        return enableScrolling && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return enableScrolling && super.onInterceptTouchEvent(ev);
    }
}
