/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.zipato.appv2.B;import com.zipato.appv2.R;

/**
 * Created by murielK on 7/24/2015.
 */
public class TempPickerView extends BaseCWRGBView {

    public static final int WC_POSITION_LEFT = 0;
    public static final int WC_POSITION_RIGHT = 1;
    public static final int WC_POSITION_TOP = 2;
    public static final int WC_POSITION_BOTTOM = 3;
    private static final String TAG = TempPickerView.class.getSimpleName();
    private final int[] coldWarmValue = new int[2];
    private int wcPosition;
    private WCPickerListener listener;

    public TempPickerView(Context context) {
        super(context);
    }

    public TempPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TempPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static int calculate(float value, int range) {

        return (int) ((Math.abs(value) * 255) / (float) range);
    }

    public static int convertToTemp(int[] coldWarm) {
        int temperature = 0;
        if ((coldWarm[0] == 255) && (coldWarm[1] == 255)) {
            temperature = 0;
        } else if (coldWarm[0] == 255) {
            temperature = ((coldWarm[1] * 100) / 255) - 100;
        } else if (coldWarm[1] == 255) {
            temperature = 100 - ((coldWarm[0] * 100) / 255);
        }
        Log.d(TAG, "Converted temp: " + temperature);
        return temperature;
    }

    public static int[] convertToWC(int temperature, int[] coldWarm) {
        if (coldWarm == null) coldWarm = new int[2];
        if (temperature == 0) {
            coldWarm[0] = 255;
            coldWarm[1] = 255;
        } else if (temperature < 0) {
            coldWarm[0] = 255;
            coldWarm[1] = 255 + ((255 * temperature) / 100);
        } else {
            coldWarm[0] = 255 - ((255 * temperature) / 100);
            coldWarm[1] = 255;
        }

        Log.d(TAG, "Cold: " + coldWarm[0] + " Warn: " + coldWarm[1]);
        return coldWarm;
    }

    public int[] getColdWarmValue() {
        return coldWarmValue;
    }

    public void setListener(WCPickerListener listener) {
        this.listener = listener;
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);

        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TempPickerView, defStyle, 0);

            try {
                wcPosition = a.getInt(R.styleable.TempPickerView_wcPosition, WC_POSITION_LEFT);
            } finally {
                a.recycle();
            }
        }

    }

    @Override
    protected void validatePosition() {
        super.validatePosition();
        switch (wcPosition) {
            case WC_POSITION_LEFT:
            case WC_POSITION_RIGHT:
                yCursorPosition = (float) (getHeight() / 2);
                break;
            case WC_POSITION_TOP:
            case WC_POSITION_BOTTOM:
                xCursorPosition = (float) (getWidth() / 2);
                break;
        }
    }

    @Override
    protected void onStartTracking(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleEvent(event);
        if (listener != null)
            listener.onStart(coldWarmValue);
    }

    @Override
    protected void onTracking(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleEvent(event);
        if (listener != null)
            listener.onTracking(coldWarmValue);
    }

    @Override
    protected void onCancel(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleEvent(event);
        if (listener != null)
            listener.onStop(coldWarmValue);
    }

    @Override
    protected void onStopTracking(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleEvent(event);
        if (listener != null)
            listener.onStop(coldWarmValue);
    }

    @Override
    protected Paint[] getPaints(int length, int height) {
        final LinearGradient linearGradient;
        switch (wcPosition) {
            case WC_POSITION_TOP:
                linearGradient = new LinearGradient(0, (float) getPaddingTop(), 0, (float) (height + getPaddingTop()), new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            case WC_POSITION_BOTTOM:
                linearGradient = new LinearGradient(0, (float) (height + getPaddingTop()), 0, (float) getPaddingTop(), new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            case WC_POSITION_LEFT:
                linearGradient = new LinearGradient((float) getPaddingLeft(), 0, (float) (length + getPaddingLeft()), 0, new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            case WC_POSITION_RIGHT:
                linearGradient = new LinearGradient((float) (length + getPaddingLeft()), 0, (float) getPaddingLeft(), 0, new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            default:
                linearGradient = new LinearGradient((float) getPaddingLeft(), 0, (float) (length + getPaddingLeft()), 0, new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(linearGradient);
        return new Paint[]{paint};
    }

    public void setTemperature(int value) {
        convertToWC(value, coldWarmValue);
        switch (wcPosition) {
            case WC_POSITION_LEFT:
            case WC_POSITION_RIGHT:
                final int middleWidth = getRealWidth() / 2;
                final int temp = (value * middleWidth) / 100;
                if (wcPosition == WC_POSITION_LEFT)
                    xCursorPosition = (float) ((middleWidth + temp) + (getRealLeft() - getLeft()));
                else
                    xCursorPosition = (float) ((middleWidth - temp) + (getRealLeft() - getLeft()));
                yCursorPosition = (float) (getHeight() / 2);
                break;
            case WC_POSITION_TOP:
            case WC_POSITION_BOTTOM:
                final int middleHeight = getRealHeight() / 2;
                final int temp2 = (value * middleHeight) / 100;
                if (wcPosition == WC_POSITION_TOP)
                    yCursorPosition = (float) ((middleHeight + temp2) + (getRealTop() - getTop()));
                else
                    yCursorPosition = (float) ((middleHeight - temp2) + (getRealTop() - getTop()));
                xCursorPosition = (float) (getWidth() / 2);
                break;
        }

        invalidate();
    }

    public void setTemperature(int[] coldWarmValue) {
        setTemperature(convertToTemp(coldWarmValue));
    }

    private void calculateCW() {
        final int middleWidth = getRealWidth() / 2;
        final int middleHeight = getRealHeight() / 2;
        Log.d(TAG, String.format("xCursorPosition %f paddingLeft %d yCursorPosition %f paddingRight %d \n " +
                "realWidth %d realHeight %d", xCursorPosition, getPaddingLeft(), yCursorPosition, getPaddingRight(), getRealWidth(), getRealHeight()));
        switch (wcPosition) {
            case WC_POSITION_LEFT:
                if ((xCursorPosition - getPaddingLeft()) > middleWidth) {
                    coldWarmValue[0] = 255 - calculate((xCursorPosition - getPaddingLeft() - middleWidth), middleWidth);
                    coldWarmValue[1] = 255;
                } else if ((xCursorPosition - getPaddingLeft()) < middleWidth) {
                    coldWarmValue[0] = 255;
                    coldWarmValue[1] = calculate(xCursorPosition - getPaddingLeft(), middleWidth);
                } else
                    coldWarmValue[0] = coldWarmValue[1] = 255;
                break;
            case WC_POSITION_RIGHT:
                if ((xCursorPosition - getPaddingLeft()) > middleWidth) {
                    coldWarmValue[0] = 255;
                    coldWarmValue[1] = 255 - calculate((xCursorPosition - getPaddingLeft() - middleWidth), middleWidth);
                } else if ((xCursorPosition - getPaddingLeft()) < middleWidth) {
                    coldWarmValue[0] = calculate(xCursorPosition - getPaddingLeft(), middleWidth);
                    coldWarmValue[1] = 255;
                } else
                    coldWarmValue[0] = coldWarmValue[1] = 255;
                break;
            case WC_POSITION_TOP:
                if ((yCursorPosition - getPaddingTop()) > middleHeight) {
                    coldWarmValue[0] = 255 - calculate((yCursorPosition - getPaddingTop() - middleHeight), middleHeight);
                    coldWarmValue[1] = 255;
                } else if ((yCursorPosition - getPaddingTop()) < middleHeight) {
                    coldWarmValue[0] = 255;
                    coldWarmValue[1] = calculate(yCursorPosition - getPaddingTop(), middleHeight);
                } else
                    coldWarmValue[0] = coldWarmValue[1] = 255;
                break;
            case WC_POSITION_BOTTOM:
                if ((yCursorPosition - getPaddingTop()) > middleHeight) {
                    coldWarmValue[0] = 255;
                    coldWarmValue[1] = 255 - calculate((yCursorPosition - getPaddingTop() - middleHeight), middleHeight);
                } else if (yCursorPosition < middleHeight) {
                    coldWarmValue[0] = calculate(yCursorPosition - getPaddingTop(), middleHeight);
                    coldWarmValue[1] = 255;
                } else
                    coldWarmValue[0] = coldWarmValue[1] = 255;
                break;
        }

        if (coldWarmValue[0] > 255) // check if both warm and cold are in the correct border
            coldWarmValue[0] = 255;
        if (coldWarmValue[0] < 0)
            coldWarmValue[0] = 0;
        if (coldWarmValue[1] > 255)
            coldWarmValue[1] = 255;
        if (coldWarmValue[1] < 0)
            coldWarmValue[1] = 0;
    }

    private void handleEvent(MotionEvent event) {
        xCursorPosition = event.getX();
        yCursorPosition = event.getY();
        validatePosition();
        calculateCW();
        Log.d(TAG, String.format("Calculated cold: %d warm: %d ", coldWarmValue[0], coldWarmValue[1]));
        invalidate();
    }

    public interface WCPickerListener {

        void onStart(int[] coldWarm);

        void onTracking(int[] coldWarm);

        void onStop(int[] coldWarm);
    }
}
