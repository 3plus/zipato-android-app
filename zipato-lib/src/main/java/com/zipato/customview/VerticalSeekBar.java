/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * Created by murielK on 10/21/2014.
 */
public class VerticalSeekBar extends SeekBar {

    protected OnSeekBarChangeListener listener;
    protected int x, y, z, w;

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);
//        float px = this.getWidth() / 2.0f;
//        float py = this.getHeight() / 2.0f;
//        c.scale(-1, 1, px, py);
        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setSelected(true);
                setPressed(true);
                int progress = calculateProgress(event);
                if (listener != null) listener.onStartTrackingTouch(this);
                if (listener != null) listener.onProgressChanged(this, progress, true);
                break;
            case MotionEvent.ACTION_UP:
                setSelected(false);
                setPressed(false);
                if (listener != null) listener.onStopTrackingTouch(this);
                break;
            case MotionEvent.ACTION_MOVE:
                progress = calculateProgress(event);
                if (listener != null) listener.onProgressChanged(this, progress, true);
                break;
            case MotionEvent.ACTION_CANCEL:
                setSelected(false);
                setPressed(false);
                break;
        }
        return true;
    }

    private int calculateProgress(MotionEvent event) {

        int progress = getMax() - (int) (getMax() * event.getY() / getHeight());
        //progress = getMax() - progress;
        progress = (progress < 0) ? 0 : ((progress > getMax()) ? getMax() : progress);
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
        return progress;
    }

    @Override
    public synchronized void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public synchronized void setProgress(int progress) {

        progress = (progress < 0) ? 0 : ((progress > getMax()) ? getMax() : progress);
        super.setProgress(progress);
        onSizeChanged(x, y, z, w);
        if (listener != null) listener.onProgressChanged(this, progress, false);
    }

    @Override
    protected synchronized void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
        this.x = w;
        this.y = h;
        this.z = oldw;
        this.w = oldh;
    }

}

