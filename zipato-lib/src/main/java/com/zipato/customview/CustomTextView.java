/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by murielK on 7/31/2014.
 */
public class CustomTextView extends TextView {
    public CustomTextView(Context context) {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public int getExtendedPaddingTop() {
        return 0;
    }

    @Override
    public int getExtendedPaddingBottom() {
        return 0;
    }

    @Override
    public int getTotalPaddingTop() {
        return 0;
    }

    @Override
    public int getTotalPaddingBottom() {
        return 0;
    }

    @Override
    public int getCompoundPaddingTop() {
        return 0;
    }

    @Override
    public int getCompoundPaddingBottom() {
        return 0;
    }
}
