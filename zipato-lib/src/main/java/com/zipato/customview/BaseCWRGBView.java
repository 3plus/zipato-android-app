/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zipato.appv2.R;


/**
 * Created by murielK on 7/22/2015.
 */
public abstract class BaseCWRGBView extends View { //Base class than will handle both the color picker and cold warm controller

    private static final String TAG = BaseCWRGBView.class.getSimpleName();

    protected float xCursorPosition;
    protected float yCursorPosition;

    private Paint[] paints;
    private Paint cursorPaint;
    private int cursorRadius;

    private int right;
    private int left;
    private int top;
    private int bottom;


    public BaseCWRGBView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public BaseCWRGBView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public BaseCWRGBView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) { // draw the paint which will be in case of color picker an approximate color representation  of HUE + the the gradient of the saturation
        if (paints == null)
            return;

        for (Paint paint : paints) {
            canvas.drawRect(getPaddingLeft(), getPaddingTop(), getRealWidth() + getPaddingLeft(), getRealHeight() + getPaddingTop(), paint);
        }
        canvas.drawCircle(xCursorPosition, yCursorPosition, cursorRadius, cursorPaint); //  draw the cursor/ tracker
    }

    protected void init(Context context, AttributeSet attrs, int defStyle) { // resolve value from the xml : cursorRadius, cursor color ...
        float density = context.getResources().getDisplayMetrics().density;
        cursorRadius = (int) (density * 6);
        float cursorStroke = 4 * density;
        int cursorColor = Color.BLACK;

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BaseCWRGBView, defStyle, 0);

            try {
                cursorStroke = a.getDimension(R.styleable.BaseCWRGBView_cursorStrokeSize, cursorStroke);
                cursorRadius = (int) a.getDimension(R.styleable.BaseCWRGBView_cursorRadius, cursorRadius);
                cursorColor = a.getColor(R.styleable.BaseCWRGBView_cursorColor, cursorColor);
            } finally {
                a.recycle();
            }
        }

        cursorPaint = new Paint();
        cursorPaint.setAntiAlias(true);
        cursorPaint.setColor(cursorColor);
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setStrokeWidth(cursorStroke);
        cursorPaint = new Paint();
        cursorPaint.setAntiAlias(true);
        cursorPaint.setStyle(Paint.Style.STROKE);
    }

    protected abstract void onStartTracking(BaseCWRGBView baseCWRGBView, MotionEvent event);

    protected abstract void onTracking(BaseCWRGBView baseCWRGBView, MotionEvent event);

    protected abstract void onCancel(BaseCWRGBView baseCWRGBView, MotionEvent event);

    protected abstract void onStopTracking(BaseCWRGBView baseCWRGBView, MotionEvent event);

    protected abstract Paint[] getPaints(int length, int height);

    public int getRealWidth() {
        return right - left;
    }

    public int getRealHeight() {
        return bottom - top;
    }

    public int getRealRight() {
        return right;
    }

    public int getRealLeft() {
        return left;
    }

    public int getRealTop() {
        return top;
    }

    public int getRealBottom() {
        return bottom;
    }


    protected void validatePosition() {//  check if the cursor position are inside the actual rectangle bound
        if (xCursorPosition < (float) (left - getLeft()))
            xCursorPosition = (float) (left - getLeft());
        if (xCursorPosition > (float) (getRealWidth() + getPaddingLeft()))
            xCursorPosition = (float) (getRealWidth() + getPaddingLeft());
        if (yCursorPosition < (float) (top - getTop()))
            yCursorPosition = (float) (top - getTop());
        if (yCursorPosition > (float) (getRealHeight() + getPaddingBottom()))
            yCursorPosition = (float) (getRealHeight() + getPaddingBottom());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        right = getRight() - getPaddingRight();
        left = getLeft() + getPaddingLeft();
        top = getTop() + getPaddingTop();
        bottom = getBottom() - getPaddingBottom();
        yCursorPosition = (float) (getHeight() / 2);
        xCursorPosition = (float) (getWidth() / 2);
        paints = getPaints(getRealWidth(), getRealHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = getDefaultSize(minW, widthMeasureSpec);
        int minH = getPaddingTop() + getPaddingBottom() + getSuggestedMinimumHeight();
        int h = getDefaultSize(minH, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) { // populate the events to the subclass
        Log.d(TAG, String.format("X position = %f Y position = %f left = %d, right = %d, realTop = %d top = %d, bottom = %d", event.getX(), event.getY(), left, right, top, getTop(), bottom));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                onStartTracking(this, event);
                break;
            case MotionEvent.ACTION_MOVE:
                onTracking(this, event);
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                onStopTracking(this, event);
                break;
            case MotionEvent.ACTION_CANCEL://
                setPressed(false);
                onCancel(this, event);
                break;
        }
        return true;
    }

}
