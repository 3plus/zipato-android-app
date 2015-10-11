/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by murielK on 7/23/2015.
 */
public class ColorPickerView extends BaseCWRGBView {

    private static final String TAG = ColorPickerView.class.getSimpleName();
    private static final int HUE_MAX_VALUE = 360;
    private final float[] colorHSV = new float[3];
    private ColorPickerListener listener;

    public ColorPickerView(Context context) {
        super(context);
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float[] getColorHSV() {
        return colorHSV;
    }

    public void setListener(ColorPickerListener listener) {
        this.listener = listener;
    }

    public void setColor(int color) {
        Color.colorToHSV(color, colorHSV);
        xCursorPosition = ((colorHSV[0] * (float) getRealWidth()) / HUE_MAX_VALUE) + (float) (getRealLeft() - getLeft());
        yCursorPosition = (colorHSV[1] * (float) getRealHeight()) + (float) (getRealTop() - getTop());
        invalidate();
    }

    private void calculateHSV() {
        final float touchDis = xCursorPosition - (float) (getRealLeft() - getLeft());
        colorHSV[0] = (touchDis * HUE_MAX_VALUE) / (float) getRealWidth();
        final float touchHeight = yCursorPosition - (float) (getRealTop() - getTop());
        colorHSV[1] = touchHeight / (float) getRealHeight();
        colorHSV[2] = 1f;
        Log.d(TAG, String.format("===== DISTANCE =====" +
                " touchDIs = %f touchHeight = %f realDistance = %d realHeight = %d", touchDis, touchHeight, getRealWidth(), getRealHeight()));

    }

    private void handleTracking(MotionEvent event) {
        xCursorPosition = event.getX();
        yCursorPosition = event.getY();
        validatePosition();
        calculateHSV();
        invalidate();
    }


    @Override
    protected void onStartTracking(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleTracking(event);
        if (listener != null)
            listener.onStart(Color.HSVToColor(colorHSV));
    }

    @Override
    protected void onTracking(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleTracking(event);
        if (listener != null)
            listener.onTracking(Color.HSVToColor(colorHSV));
    }

    @Override
    protected void onCancel(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleTracking(event);
        if (listener != null)
            listener.onStop(Color.HSVToColor(colorHSV));
    }


    @Override
    protected void onStopTracking(BaseCWRGBView baseCWRGBView, MotionEvent event) {
        handleTracking(event);
        if (listener != null)
            listener.onStop(Color.HSVToColor(colorHSV));
    }

    @Override
    protected Paint[] getPaints(int length, int height) {
        final float[] hsv = {0f, 1f, 1f};
        final int[] colors = new int[13];
        final int[] sat = {Color.WHITE, Color.TRANSPARENT};
        final int colorSize = colors.length;
        final int sample = HUE_MAX_VALUE / (colorSize - 1);

        for (int i = 0; i < colorSize; i++) {
            hsv[0] = (float) (i * sample);
            colors[i] = Color.HSVToColor(hsv);
        }

        colors[colorSize - 1] = colors[0];

        LinearGradient colorsGradient = new LinearGradient((float) getPaddingLeft(), 0, (float) (length + getPaddingLeft()), 0, colors, null, Shader.TileMode.CLAMP);
        LinearGradient satGradient = new LinearGradient(0, (float) getPaddingTop(), 0, (float) (height + getPaddingTop()), sat, null, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(colorsGradient);
        Paint paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setDither(true);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setShader(satGradient);
        return new Paint[]{paint, paint2};
    }

    public interface ColorPickerListener {

        void onStart(int color);

        void onTracking(int color);

        void onStop(int color);
    }
}
