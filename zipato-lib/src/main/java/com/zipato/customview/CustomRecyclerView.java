/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by murielK on 7/30/2015.
 */
public class CustomRecyclerView extends RecyclerView {

    private boolean enableScroll = true;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isEnableScroll() {
        return enableScroll;
    }

    public void setEnableScroll(boolean enableScroll) {
        this.enableScroll = enableScroll;
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (enableScroll)
            return super.onInterceptTouchEvent(e);
        return false;
    }
}
