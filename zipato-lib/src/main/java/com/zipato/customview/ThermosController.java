/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zipato.appv2.R;

/**
 * Created by murielK on 7/10/2014.
 */
public class ThermosController extends View {

    public static final int FAHRENHEIT = 1;
    public static final int CELSIUS = 0;
    private static final String TAG = CircularController.class.getSimpleName();
    private static final int INVALID_PROGRESS_VALUE = -1;
    private static final int M_ANGLE_OFFSET = -90;
    private final Matrix mMatrixHeating = new Matrix();
    private final Matrix mMatrixCooling = new Matrix();
    private final RectF mArcRectStart = new RectF();
    private final RectF mArcRectMiddle = new RectF();
    private final RectF mArcRecEnd = new RectF();
    private final RectF textRectF = new RectF();
    private final Paint paintTextRectF = new Paint();
    private Drawable mThumb;
    private double mMax = 35;
    private double mMin = 0;
    private double mProgressCooling = 0;
    private double mProgressMiddle = 0;
    private double mProgressHeating = 0;
    private double mProgressHeatingMargin = 1;
    private double mProgressCoolingMargin = 1;
    private double mProgressMinimumMargin = 0;
    private int mProgressWidth = 6;
    private int mArcWidth = 9;
    private int mStartAngle = 0;
    private int mSweepAngle = 300;
    private int mRotation = 0;
    private boolean mRoundedEdges = false;
    private boolean mTouchInside = true;
    private double currentTemp = 0;
    private int mArcRadius = 0;
    private float mProgressSweepCooling = 0;
    private float mProgressSweepHeating = 0;
    private float mProgressSweepMiddle = 0;
    private float mProgressSweepMinStart = 0;
    private Paint mArcPaint;
    private Paint mProgressHeatingPaint;
    private Paint mProgressCoolingPaint;
    private Paint mProgressMiddlePaint;
    private Paint mPaintMiddleBackG;
    private int mTranslateX;
    private int mTranslateY;
    private int mThumbXPos;
    private int mThumbYPos;
    private double mTouchAngle;
    private float mTouchIgnoreRadius;
    //  private OnSeekArcChangeListener mOnSeekArcChangeListener;
    private float fProgressSweep = 0;
    private OnThermosControllerChangeListener listner;
    private ThermostatMode thermostatMode = ThermostatMode.AUTO;
    private int currentUnit = CELSIUS;

    public ThermosController(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ThermosController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public ThermosController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public ThermostatMode getThermostatMode() {
        return thermostatMode;
    }

    public void setThermostatMode(boolean heating, boolean cooling, boolean fromUser) {
        if (heating || cooling) {
            if (heating && cooling) {
                thermostatMode = ThermostatMode.OFF;
                updateProgress(0, fromUser);

            } else if (heating) {
                thermostatMode = ThermostatMode.COOLING;
                // mProgressMinimumMargin = mProgressCoolingMargin;
                updateProgress(mProgressCooling, fromUser);
            } else {
                thermostatMode = ThermostatMode.HEATING;
                // mProgressMinimumMargin = mProgressHeatingMargin;
                updateProgress(mProgressHeating, fromUser);
            }


        } else {
            thermostatMode = ThermostatMode.AUTO;
            //mProgressMinimumMargin = mProgressHeatingMargin+ mProgressCoolingMargin;
            updateProgress(mProgressHeating, fromUser);
            invalidate();
        }
        listner.onModeChange(thermostatMode, fromUser);

    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;
        int arcColor = res.getColor(R.color.progress_gray);
        int progressCoolingColor = res.getColor(R.color.progress_cooling_color);
        int progressHeatingColor = res.getColor(R.color.progress_heating_color);
        int progressMiddleColor = res.getColor(R.color.progress_middle_color);

        int thumbHalfHeight = 0;
        int thumbHalfWidth = 0;

        mProgressWidth = (int) (mProgressWidth * density);
        mArcWidth = (int) (mArcWidth * density);

        if (attrs != null) {

            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThermosController, defStyle, 0);

            Drawable tempThumb = a.getDrawable(R.styleable.ThermosController_thermos_thumb);
            if (tempThumb != null) {
                mThumb = tempThumb;
            } else throw new NullPointerException("You need to provide the controller thumb");

            thumbHalfHeight = (int) mThumb.getIntrinsicHeight() / 2;
            thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;

            mThumb.setBounds(-thumbHalfWidth, -thumbHalfHeight, thumbHalfWidth,
                    thumbHalfHeight);

            mMax = a.getInteger(R.styleable.ThermosController_thermos_max, (int) mMax);
            mMin = a.getInteger(R.styleable.ThermosController_thermos_min, (int) mMin);
            mProgressCooling = a.getInteger(R.styleable.ThermosController_mProgressCooling, (int) Math.round(mProgressCooling));
            mProgressHeating = a.getInteger(R.styleable.ThermosController_mProgressHeating, (int) Math.round(mProgressHeating));
            mProgressCoolingMargin = a.getInteger(R.styleable.ThermosController_mProgressCoolingMargin, (int) mProgressCoolingMargin);
            mProgressHeatingMargin = a.getInteger(R.styleable.ThermosController_mProgressHeatingMargin, (int) mProgressHeatingMargin);
            mProgressHeatingMargin = (mProgressHeatingMargin >= mMax) ? mMax / 2 : mProgressHeatingMargin;
            mProgressCoolingMargin = (mProgressCoolingMargin >= mMax) ? mMax / 2 : mProgressCoolingMargin;

            mProgressWidth = (int) a.getDimension(
                    R.styleable.ThermosController_thermos_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.CircularController_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.ThermosController_thermos_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.ThermosController_thermos_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.ThermosController_thermos_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.ThermosController_thermos_roundEdges,
                    mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.ThermosController_thermos_touchInside,
                    mTouchInside);

            arcColor = a.getColor(R.styleable.ThermosController_thermos_arcColor, arcColor);
            progressCoolingColor = a.getColor(R.styleable.ThermosController_progressCoolingColor, progressCoolingColor);
            progressHeatingColor = a.getColor(R.styleable.ThermosController_progressHeatingColor, progressHeatingColor);
            progressMiddleColor = a.getColor(R.styleable.ThermosController_progressMiddleColor, progressMiddleColor);
            a.recycle();

        }

        checkMinCoolingValue();

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        mPaintMiddleBackG = new Paint();
        mPaintMiddleBackG.setAntiAlias(true);
        mPaintMiddleBackG.setStyle(Paint.Style.STROKE);
        mPaintMiddleBackG.setStrokeWidth(mProgressWidth);
        mPaintMiddleBackG.setColor(res.getColor(R.color.progress_middle_back));
        mProgressCoolingPaint = new Paint();
        // mProgressCoolingPaint.setColor(progressCoolingColor);
        mProgressCoolingPaint.setAntiAlias(true);
        mProgressCoolingPaint.setStyle(Paint.Style.STROKE);
        mProgressCoolingPaint.setStrokeWidth(mProgressWidth);

        mProgressHeatingPaint = new Paint();
        // mProgressHeatingPaint.setColor(progressHeatingColor);
        mProgressHeatingPaint.setAntiAlias(true);
        mProgressHeatingPaint.setStyle(Paint.Style.STROKE);
        mProgressHeatingPaint.setStrokeWidth(mProgressWidth);

        mProgressMiddlePaint = new Paint();
        mProgressMiddlePaint.setColor(progressMiddleColor);
        mProgressMiddlePaint.setAntiAlias(true);
        mProgressMiddlePaint.setStyle(Paint.Style.STROKE);
        mProgressMiddlePaint.setStrokeWidth(mProgressWidth);
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressCoolingPaint.setStrokeCap(Paint.Cap.ROUND);
            //mProgressMiddlePaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressHeatingPaint.setStrokeCap(Paint.Cap.ROUND);
        }


    }

    public void checkMinCoolingValue() {
        mProgressMinimumMargin = mProgressCoolingMargin + mProgressHeatingMargin;
        double minCooling = mProgressHeating + mProgressMinimumMargin;
        if (minCooling > mProgressCooling) {
            mProgressCooling = minCooling;
        }

        if ((mMax - mProgressMinimumMargin) < mMin) {
            mMin = mMax - mProgressMinimumMargin;
        }
        mProgressHeating = (mProgressHeating < mMin) ? mMin : mProgressHeating;
        mProgressCooling = (mProgressCooling > mMax) ? mMax : mProgressCooling;
        mProgressHeating = (mProgressHeating > (mMax - mProgressMinimumMargin)) ? mMax - mProgressMinimumMargin : mProgressHeating;
        mProgressCooling = (mProgressCooling < (mMin + mProgressMinimumMargin)) ? mMin + mProgressMinimumMargin : mProgressCooling;
        mProgressSweepMinStart = getProgressSweepByValue(mMin);

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if (!mClockwise) {
//            canvas.scale(-1, 1, mArcRectStart.centerX(), mArcRectStart.centerY());
//        }

        final int arcStart = mStartAngle + M_ANGLE_OFFSET + mRotation;
        final int arcSweep = 360;

        canvas.drawArc(mArcRectStart, arcStart, arcSweep, false, mArcPaint);
        canvas.drawArc(mArcRecEnd, arcStart, arcSweep, false,
                mArcPaint);
        canvas.drawArc(mArcRectMiddle, M_ANGLE_OFFSET + mRotation, arcSweep, false,
                mPaintMiddleBackG);
        canvas.drawArc(mArcRectMiddle, arcStart + mProgressSweepHeating - mProgressSweepMinStart, mProgressSweepMiddle, false,
                mProgressMiddlePaint);
        canvas.drawArc(mArcRectMiddle, arcStart, mProgressSweepHeating - mProgressSweepMinStart, false,
                mProgressHeatingPaint);
        canvas.drawArc(mArcRectMiddle, arcStart + mProgressSweepCooling - mProgressSweepMinStart, mSweepAngle - mProgressSweepCooling + mProgressSweepMinStart, false,
                mProgressCoolingPaint);

        //canvas.drawRect(textRectF, paintTextRectF);

        //Draw the thumb nail
        if (currentTemp >= mMin) {
            canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
            canvas.save();
            canvas.rotate(getProgressSweepByValue(currentTemp) + mRotation + mStartAngle - mProgressSweepMinStart);

            mThumb.draw(canvas);

        }
        canvas.restore();

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

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);

        arcDiameter = min - getPaddingLeft();

        top = height / 2 - ((arcDiameter / 2));
        left = width / 2 - (arcDiameter / 2);
        mArcRectStart.set(left, top, left + arcDiameter, top + arcDiameter);
        int tempArcWidth = (mArcWidth + mProgressWidth) / 2;
        mArcRadius = (arcDiameter / 2) - tempArcWidth;
        int tempArcWidth2 = tempArcWidth * 2;
        mArcRectMiddle.set(left + tempArcWidth, top + tempArcWidth, (left + arcDiameter) - tempArcWidth, top + (arcDiameter - tempArcWidth));
        mArcRecEnd.set(left + tempArcWidth2, top + tempArcWidth2, (left + arcDiameter) - tempArcWidth2, top + (arcDiameter - tempArcWidth2));
        mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
        mProgressMiddle = mProgressCooling - mProgressHeating;
        mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);
        mProgressSweepCooling = getProgressSweepByValue(mProgressCooling);
        float arcStart = (getProgressSweepByValue(currentTemp) + mStartAngle + mRotation + 90) - mProgressSweepMinStart;
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));

        float xRectF = (float) Math.pow((mArcRadius - tempArcWidth) - mArcWidth / 2, 2) / 2;
        xRectF = (float) Math.sqrt(xRectF);
        textRectF.set(mTranslateX - xRectF, mTranslateY - xRectF, mTranslateX + xRectF, mTranslateY + xRectF);
        paintTextRectF.setColor(Color.WHITE);
        paintTextRectF.setAntiAlias(true);
        paintTextRectF.setStyle(Paint.Style.STROKE);
        Shader shaderHeating = new SweepGradient(mTranslateX, mTranslateX, new int[]{Color.parseColor("#eaaa3e"), Color.parseColor("#FC6901")}, null);
        mMatrixHeating.setRotate(90, mTranslateX, mTranslateY);
        shaderHeating.setLocalMatrix(mMatrixHeating);
        mProgressHeatingPaint.setShader(shaderHeating);
        Shader shaderCooling = new SweepGradient(mTranslateX, mTranslateX, new int[]{Color.parseColor("#ff114280"), Color.parseColor("#4d87a5"), Color.parseColor("#8added")}, null);
        mMatrixCooling.setRotate(90, mTranslateX, mTranslateY);
        shaderCooling.setLocalMatrix(mMatrixCooling);
        mProgressCoolingPaint.setShader(shaderCooling);
        setTouchInSide(mTouchInside);

    }

    private float valuePerDegree() {
        return (float) (mMax - mMin) / (mSweepAngle);
    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfHeight = (int) mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreRadius = mArcRadius
                    - Math.min(thumbHalfWidth, thumbHalfHeight);
        }
    }

    private float getProgressSweepByValue(double progress) {

        //return Float.parseFloat(new DecimalFormat("##.#").format(progress/mMax * mSweepAngle));
        return (float) (progress / (mMax - mMin)) * (mSweepAngle);

    }

    public void setCurrentUnit(int unit) {

        if ((unit == CELSIUS) && (currentUnit == FAHRENHEIT)) { // convert only if it is not in the that requested mode.
            mMin = ((mMin - 32) * 5) / 9;
            mMax = ((mMin - 32) * 5) / 9;
            currentUnit = CELSIUS;
        } else if ((unit == FAHRENHEIT) && (currentUnit == CELSIUS)) {
            mMin = ((mMin * 9) / 5) + 32;
            mMax = ((mMax * 9) / 5) + 32;
            currentUnit = FAHRENHEIT;
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    private void updateOnTouch(MotionEvent event) {
//        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
//        if (ignoreTouch) {
//            return;
//        }
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        double progress = getProgressForAngle(mTouchAngle);
        //onProgressRefresh(progress, true);
        onRefresh(progress, true);
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        int tempArcWidth = (mArcWidth + mProgressWidth) * 3;
        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if ((touchRadius < mTouchIgnoreRadius) || (touchRadius > (mArcRadius + tempArcWidth))) {
            ignore = true;
        }
        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        //invert the x-coord if we are rotating anti-clockwise

        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;

        }
        angle -= mStartAngle;
        return angle;
    }

    private double getProgressForAngle(double angle) {

        double touchProgress = (valuePerDegree() * angle) + mMin;
        // double touchProgress = (int) Math.round(valuePerDegree() * angle);
        //double touchProgress = Float.parseFloat(new DecimalFormat("##.#").format(valuePerDegree() * angle));//i will be super happy if i got an easier number here
        touchProgress = (touchProgress < mMin) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;
    }

    private void onRefresh(double progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        progress = (progress > mMax) ? mMax : progress;
        progress = (progress < mMin) ? mMin : progress;
        updateProgress(progress, fromUser);
    }

    private void updateProgress(double progress, boolean fromUser) {
        switch (thermostatMode) {
            case HEATING:
                mProgressHeating = progress;
                mProgressHeating = (mProgressHeating > (mMax - mProgressMinimumMargin)) ? (mMax - mProgressMinimumMargin) : mProgressHeating;
                mProgressCooling = mMax;
                mProgressMiddle = mMax - mProgressHeating;
                mProgressSweepCooling = getProgressSweepByValue(mProgressCooling);
                mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
                mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);

                if (listner != null)
                    listner.onProgressChanged(this, mProgressHeating, mProgressCooling, currentTemp, fromUser, thermostatMode);

                break;
            case COOLING:
                mProgressCooling = progress;
                mProgressCooling = (mProgressCooling <= (mProgressMinimumMargin + mMin)) ? (mProgressMinimumMargin + mMin) : mProgressCooling;
                mProgressHeating = mMin;
                mProgressMiddle = mProgressCooling;
                mProgressMiddle = (mProgressMiddle > (mMax - mMin)) ? (mMax - mMin) : mProgressMiddle;
                mProgressSweepCooling = getProgressSweepByValue(mProgressCooling);
                mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
                mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);

                if (listner != null)
                    listner.onProgressChanged(this, mProgressHeating, mProgressCooling, currentTemp, fromUser, thermostatMode);

                break;
            case AUTO:
                double left = progress - mProgressHeating;
                double right = mProgressCooling - progress;
                double margin;
                double current;
                if (left > right) {

                    current = mProgressCooling;
                    mProgressCooling = progress;
                    margin = mProgressCooling - mProgressHeating;

                    if ((margin > mProgressMinimumMargin) || (current < mProgressCooling)) {

                        mProgressMiddle = mProgressCooling - mProgressHeating;
                        mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);
                        mProgressSweepCooling = getProgressSweepByValue(progress);

                    } else {
                        mProgressCooling = (mProgressCooling <= (mProgressMinimumMargin + mMin)) ? (mProgressMinimumMargin + mMin) : mProgressCooling;
                        mProgressMiddle = mProgressMinimumMargin;
                        mProgressHeating = mProgressCooling - mProgressMiddle;
                        mProgressHeating = (mProgressHeating < mMin) ? mMin : mProgressHeating;
                        mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);
                        mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
                        mProgressSweepCooling = getProgressSweepByValue(progress);
                    }

                } else if (left < right) {
                    current = mProgressHeating;
                    mProgressHeating = progress;
                    margin = mProgressCooling - mProgressHeating;
                    if ((margin > mProgressMinimumMargin) || (current > mProgressHeating)) {
                        mProgressMiddle = mProgressCooling - mProgressHeating;
                        mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
                        mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);

                    } else {
                        mProgressMiddle = mProgressMinimumMargin;
                        mProgressCooling = (mProgressHeating + mProgressMiddle);
                        mProgressCooling = (mProgressCooling > mMax) ? mMax : mProgressCooling;
                        mProgressHeating = (mProgressHeating > (mMax - mProgressMinimumMargin)) ? (mMax - mProgressMinimumMargin) : mProgressHeating;
                        mProgressSweepCooling = getProgressSweepByValue(mProgressCooling);
                        mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
                        mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);
                    }
                }

                if (listner != null)
                    listner.onProgressChanged(this, mProgressHeating, mProgressCooling, currentTemp, fromUser, thermostatMode);
                // Log.d("Thermo  ", " Cooling: " + mProgressCooling + " Heating: " + mProgressHeating + " Progress: " + (progress));
                break;
            case OFF:
                mProgressHeating = mMin;
                mProgressMiddle = mMax - mMin;
                mProgressCooling = mMax;
                mProgressSweepCooling = getProgressSweepByValue(mProgressCooling);
                mProgressSweepHeating = getProgressSweepByValue(mProgressHeating);
                mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);
                if (listner != null)
                    listner.onProgressChanged(this, mProgressHeating, mProgressCooling, currentTemp, fromUser, thermostatMode);
                break;

        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ignoreTouch(event.getX(), event.getY()))
                    return true;
                onStartTrackingTouch();
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                onStopTrackingTouch();
                setPressed(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                setPressed(false);
                break;
        }

        return true;
    }

    public void setOnThermosChangeListener(OnThermosControllerChangeListener listner) {

        this.listner = listner;

    }


    public void setProgress(double mProgressHeating, double mProgressCooling) {

        switch (thermostatMode) {
            case OFF:
                break;
            case COOLING:
                this.mProgressCooling = mProgressCooling;
                checkMinCoolingValue();
                updateProgress(this.mProgressCooling, false);

                break;
            case HEATING:
                this.mProgressHeating = mProgressHeating;
                checkMinCoolingValue();
                updateProgress(this.mProgressHeating, false);
                break;
            case AUTO:
                this.mProgressHeating = mProgressHeating;
                this.mProgressCooling = mProgressCooling;
                checkMinCoolingValue();
                mProgressSweepHeating = getProgressSweepByValue(this.mProgressHeating);
                mProgressMiddle = this.mProgressCooling - this.mProgressHeating;
                mProgressSweepMiddle = getProgressSweepByValue(mProgressMiddle);
                mProgressSweepCooling = getProgressSweepByValue(this.mProgressCooling);
                if (listner != null)
                    listner.onProgressChanged(this, this.mProgressHeating, this.mProgressCooling, currentTemp, false, thermostatMode);
                invalidate();

                break;
        }


    }

    public double getmProgressHeating() {
        return mProgressHeating;
    }

    public double getmProgressCooling() {
        return mProgressCooling;
    }

    public double getmProgressHeatingMargin() {
        return mProgressHeatingMargin;
    }

    public double getmProgressCoolingMargin() {
        return mProgressCoolingMargin;
    }

    public double getmProgressMinimumMargin() {
        return mProgressMinimumMargin;
    }

    public double getmProgressMiddle() {
        return mProgressMiddle;
    }

    public void setProgressMargin(double mProgressHeatMargin, double mProgressCoolMargin) {

        mProgressHeatingMargin = mProgressHeatMargin;
        mProgressCoolingMargin = mProgressCoolMargin;
        checkMinCoolingValue();
        if (listner != null)
            listner.onProgressChanged(this, mProgressHeating, mProgressCooling, currentTemp, false, thermostatMode);
        invalidate();

    }


    public void updateCurrentTemperature() {

        float tempSweepAngle = (getProgressSweepByValue(currentTemp) + mStartAngle + mRotation + 90) - mProgressSweepMinStart;
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(tempSweepAngle)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(tempSweepAngle)));
        invalidate();
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressHeatingPaint.setStrokeWidth(mProgressWidth);
        mProgressCoolingPaint.setStrokeWidth(mProgressWidth);
        mProgressMiddlePaint.setStrokeWidth(mProgressWidth);
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public void setArcRotation(int mRotation) {
        this.mRotation = mRotation;
        updateCurrentTemperature();
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        updateCurrentTemperature();
    }

    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressHeatingPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressCoolingPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressMiddlePaint.setStrokeCap(Paint.Cap.ROUND);

        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressHeatingPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressCoolingPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressMiddlePaint.setStrokeCap(Paint.Cap.SQUARE);

        }

    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(double currentTemp) {
        this.currentTemp = currentTemp;
        float arcStart = (getProgressSweepByValue(currentTemp) + mStartAngle + mRotation + 90) - mProgressSweepMinStart;
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));
        invalidate();
    }

    private void onStartTrackingTouch() {
        if (listner != null) {
            listner.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (listner != null) {
            listner.onStopTrackingTouch(this);
        }
    }

    public enum ThermostatMode {
        HEATING, COOLING, AUTO, OFF
    }

    public interface OnThermosControllerChangeListener {

        void onProgressChanged(ThermosController thermosController, double progressHeating, double progressCooling, double currentTemp, boolean fromUser, ThermostatMode thermostatModeIn);

        void onStartTrackingTouch(ThermosController thermosController);

        void onStopTrackingTouch(ThermosController thermosController);

        void onModeChange(ThermostatMode thermostatMode1, boolean fromUser);

    }


}