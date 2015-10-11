/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.customview;

/**
 * Created by murielK on 7/7/2014.
 */

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zipato.appv2.B;import com.zipato.appv2.R;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class CircularController extends View {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_COLOR_PICKER = 1;
    public static final int MODE_WARM_COLD_PICKER = 2;
    public static final int WC_POSITION_LEFT = 3;
    public static final int WC_POSITION_RIGHT = 4;
    public static final int WC_POSITION_TOP = 5;
    public static final int WC_POSITION_BOTTOM = 6;
    private static final int INT_MAX_DEC = 360;
    private static final String TAG = CircularController.class.getSimpleName();
    private static final int INVALID_PROGRESS_VALUE = -1;
    // The initial rotational offset -90 means we start at 12 o'clock
    private static final int ANGLE_OFFSET = -90;
    private final Matrix mMatrix = new Matrix();
    private final RectF mArcRectStart = new RectF();
    private final RectF mArcRectMiddle = new RectF();
    private final RectF mArcRecEnd = new RectF();
    private final float[] colorHSV = new float[3];
    private final int[] colors = new int[3];
    boolean isSlider;
    int arcColor, progressColor, wcPosition;
    float xPosInside;
    float yPosInside;
    int temperatureValue;
    private int controllerMode;
    private int mInsideCirRadius;
    private boolean progressTypeFill = false;
    /**
     * The Drawable for the seek arc thumbnail
     */
    private Drawable mThumb;
    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mMax = 100;
    /**
     * The Current value that the SeekArc is set to
     */
    private int mProgress = 0;
    /**
     * The width of the progress line for this SeekArc
     */
    private int mProgressWidth = 6;
    /**
     * The Width of the background arc for the SeekArc
     */
    private int mArcWidth = 9;
    /**
     * The Angle to start drawing this Arc from
     */
    private int mStartAngle = 0;
    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private int mSweepAngle = 360;
    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private int mRotation = 0;
    /**
     * Give the SeekArc rounded edges
     */
    private boolean mRoundedEdges = false;
    /**
     * Enable touch inside the SeekArc
     */
    private boolean mTouchInside = true;
    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    private boolean mClockwise = true;
    // Internal variables
    private boolean startAnimation = true;
    private int mArcRadius;
    private float mProgressSweep;
    private Paint mArcPaint;
    private Paint mProgressPaint;
    private float mTranslateX;
    private float mTranslateY;
    private int mThumbXPos;
    private int mThumbYPos;
    private float mTouchIgnoreRadius;
    private OnSeekArcChangeListener mOnSeekArcChangeListener;
    private float fProgressSweep;
    private AnimMode animMode = AnimMode.NORMAL;
    private Bitmap bitmap;
    private Paint mCursorPaint;
    private int mCursorRadius;

    public CircularController(Context context) {
        super(context);
        init(context, null, 0);
    }

    public CircularController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public CircularController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private static Bitmap drawRGBBitmap(int squarePerimeter) {
        float[] hsv = {0f, 1f, 1f};
        int[] colors = new int[13];
        Bitmap bitmap = Bitmap.createBitmap(squarePerimeter, squarePerimeter, ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        int size = colors.length;
        int sample = (INT_MAX_DEC / (size - 1));
        for (int i = 0; i < size; i++) {
            hsv[0] = (i * sample + 240) % INT_MAX_DEC;
            colors[i] = Color.HSVToColor(hsv);
        }
        colors[size - 1] = colors[0];
        RadialGradient saturation = new RadialGradient(canvas.getWidth() / 2, canvas.getHeight() / 2, squarePerimeter / 2, 0xFFFFFFFF,
                0x00FFFFFF, Shader.TileMode.CLAMP);
        SweepGradient hue = new SweepGradient(canvas.getWidth() / 2, canvas.getHeight() / 2, colors, null);
        ComposeShader shader = new ComposeShader(hue, saturation, PorterDuff.Mode.SRC_OVER);
        //  Matrix matrix = new Matrix();
        //  matrix.setRotate(120, canvas.getWidth() / 2, canvas.getHeight() / 2);
        //  shader.setLocalMatrix(matrix);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(shader);

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, squarePerimeter / 2, paint);
        return bitmap;
    }

    private static Bitmap drawColdWarmBitmap(int squarePerimeter, int wcPosition) {
        Bitmap bitmap = Bitmap.createBitmap(squarePerimeter, squarePerimeter, ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        LinearGradient linearGradient;

        switch (wcPosition) {
            case WC_POSITION_TOP:
                linearGradient = new LinearGradient(0, 0, 0, canvas.getHeight(), new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            case WC_POSITION_BOTTOM:
                linearGradient = new LinearGradient(0, canvas.getHeight(), 0, 0, new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            case WC_POSITION_LEFT:
                linearGradient = new LinearGradient(0, 0, canvas.getWidth(), 0, new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            case WC_POSITION_RIGHT:
                linearGradient = new LinearGradient(canvas.getWidth(), 0, 0, 0, new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
            default:
                linearGradient = new LinearGradient(0, 0, 0, canvas.getHeight(), new int[]{Color.parseColor("#638FBC"), Color.parseColor("#FDFDF1"), Color.parseColor("#F59D18")}, null, Shader.TileMode.CLAMP);
                break;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(linearGradient);
        canvas.drawCircle((canvas.getWidth() / 2), canvas.getHeight() / 2, squarePerimeter / 2, paint);
        return bitmap;
    }

    public int getWcPosition() {
        return wcPosition;
    }

    public void setWcPosition(int wcPosition) {
        this.wcPosition = wcPosition;
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings
        arcColor = res.getColor(R.color.progress_gray);
        progressColor = res.getColor(android.R.color.holo_blue_light);
        int thumbHalfheight = 0;
        int thumbHalfWidth = 0;

        colors[0] = Color.TRANSPARENT;
        colors[1] = Color.parseColor("#FAD608");
        colors[2] = Color.parseColor("#FC6901");
        // Convert progress width to pixels for current density
        mProgressWidth *= density;
        mArcWidth *= density;


        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.CircularController, defStyle, 0);

            final Drawable thumb = a.getDrawable(R.styleable.CircularController_thumb);
            if (thumb != null) {
                mThumb = thumb;
            } else throw new NullPointerException("you need to provide a thumb for the controller");


            thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
            thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
            mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);

            mMax = a.getInteger(R.styleable.CircularController_max, mMax);
            mProgress = a.getInteger(R.styleable.CircularController_progress, mProgress);
            mProgressWidth = (int) a.getDimension(
                    R.styleable.CircularController_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.CircularController_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.CircularController_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.CircularController_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.CircularController_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.CircularController_roundEdges,
                    mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.CircularController_touchInside,
                    mTouchInside);
            mClockwise = a.getBoolean(R.styleable.CircularController_clockwise,
                    mClockwise);

            progressTypeFill = a.getBoolean(R.styleable.CircularController_progressTypeFill, progressTypeFill);
            startAnimation = a.getBoolean(R.styleable.CircularController_startAnimation, startAnimation);
            arcColor = a.getColor(R.styleable.CircularController_arcColor, arcColor);
            progressColor = a.getColor(R.styleable.CircularController_progressColor,
                    progressColor);
            controllerMode = a.getInt(R.styleable.CircularController_CMode, controllerMode);

            colors[0] = a.getInt(R.styleable.CircularController_sweepColor1, colors[0]);
            colors[1] = a.getInt(R.styleable.CircularController_sweepColor2, colors[1]);
            colors[2] = a.getInt(R.styleable.CircularController_sweepColor3, colors[2]);

            a.recycle();
        }

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);
        //mArcPaint.setAlpha(45);
        Paint mPaintMiddleBackG = new Paint();
        mPaintMiddleBackG.setAntiAlias(true);
        mPaintMiddleBackG.setStyle(Paint.Style.STROKE);
        mPaintMiddleBackG.setStrokeWidth(mProgressWidth);
        mPaintMiddleBackG.setColor(res.getColor(R.color.progress_middle_back));
        mCursorPaint = new Paint();
        mCursorPaint.setAntiAlias(true);
        mCursorPaint.setColor(Color.BLACK);
        mCursorPaint.setStyle(Paint.Style.STROKE);
        mCursorPaint.setStrokeWidth(4 * density);
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mCursorRadius = (int) (density * 6);

        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mClockwise) {
            canvas.scale(-1, 1, mArcRectStart.centerX(), mArcRectStart.centerY());
        }

        // Draw the arcs
        final int arcStart = mStartAngle + ANGLE_OFFSET + mRotation;
        final int arcSweep = 360;

        canvas.drawArc(mArcRectStart, arcStart, arcSweep, false, mArcPaint);
        canvas.drawArc(mArcRecEnd, arcStart, arcSweep, false,
                mArcPaint);

        if (progressTypeFill) {
            fProgressSweep = mSweepAngle;
        } else {

            if (startAnimation) {
                switch (animMode) {
                    case NORMAL:
                        fProgressSweep += 5;
                        if ((fProgressSweep >= mProgressSweep)) {
                            fProgressSweep = mProgressSweep;
                            startAnimation = false;
                        }
                        break;
                    case REVERSE:
                        fProgressSweep -= 5;
                        if (fProgressSweep <= mProgressSweep) {
                            fProgressSweep = mProgressSweep;
                            startAnimation = false;
                        }
                        break;
                }

                int arcStartAnim = (int) fProgressSweep + mStartAngle + mRotation + 90;
                mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStartAnim)));
                mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStartAnim)));
                int tempProgress = Math.round(fProgressSweep * valuePerDegree());
                if (mOnSeekArcChangeListener != null) {
                    mOnSeekArcChangeListener
                            .onProgressChanged(this, tempProgress, false);
                }

            } else {
                fProgressSweep = mProgressSweep;
            }
        }

//        canvas.drawArc(mArcRectMiddle, ANGLE_OFFSET + mRotation, arcSweep, false,
//                mPaintMiddleBackG);
        canvas.drawArc(mArcRectMiddle, arcStart, fProgressSweep, false,
                mProgressPaint);
        if (controllerMode != MODE_NORMAL) {
            canvas.drawBitmap(bitmap, mTranslateX - (mInsideCirRadius), mTranslateY - (mInsideCirRadius), null);
            canvas.drawCircle(xPosInside, yPosInside, mCursorRadius, mCursorPaint);
        }

        // Draw the thumb nail
        canvas.save();
        canvas.translate(mTranslateX - mThumbXPos, mTranslateY - mThumbYPos);
        canvas.rotate(fProgressSweep + mRotation + mStartAngle);
        mThumb.draw(canvas);
        canvas.restore();

        if (startAnimation)
            invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        setMeasuredDimension(width, height);
        // super.onMeasure(width, height);
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged called");
        final int min = Math.min(width, height);
        float top = 0;
        float left = 0;
        int arcDiameter = 0;
        mTranslateX = (width * 0.5f);
        mTranslateY = (height * 0.5f);
        arcDiameter = min - getPaddingLeft();

        top = height / 2 - ((arcDiameter / 2));
        left = width / 2 - (arcDiameter / 2);
        mArcRectStart.set(left, top, left + arcDiameter, top + arcDiameter);

        //mArcRadius = arcDiameter / 2;
        int tempArcWidth = (getArcWidth() + getProgressWidth()) / 2;
        mArcRadius = (arcDiameter / 2) - tempArcWidth;
        int tempArcWidth2 = tempArcWidth * 2;
        mArcRectMiddle.set(left + tempArcWidth, top + tempArcWidth, (left + arcDiameter) - tempArcWidth, top + (arcDiameter - tempArcWidth));
        mArcRecEnd.set(left + tempArcWidth2, top + tempArcWidth2, (left + arcDiameter) - tempArcWidth2, top + (arcDiameter - tempArcWidth2));
        int arcStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart)));
        final Shader shader = new SweepGradient(mTranslateX, mTranslateY, colors, null);
        mMatrix.reset();
        mMatrix.setRotate(90, mTranslateX, mTranslateY);
        shader.setLocalMatrix(mMatrix);
        mProgressPaint.setShader(shader);
        mInsideCirRadius = mArcRadius - tempArcWidth - mArcWidth / 2;//real inside size after both arcs (arc start middle and arc end)
        mInsideCirRadius -= tempArcWidth2;
        switch (controllerMode) {
            case MODE_COLOR_PICKER:

                setColorCoordinate();
                break;
            case MODE_WARM_COLD_PICKER:
                setTempCoordinate();
                break;
            default:
                xPosInside = mTranslateX;
                yPosInside = mTranslateY;
                break;
        }
        prepareBitmap(mInsideCirRadius * 2);
        setTouchInSide(mTouchInside);

    }

    private void prepareBitmap(int squarePerimeter) {

        switch (controllerMode) {
            case 1:
                bitmap = drawRGBBitmap(squarePerimeter);
                break;
            case 2:
                bitmap = drawColdWarmBitmap(squarePerimeter, wcPosition);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (startAnimation)
            return false;
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
                if (!isSlider) {
                    mArcPaint.setColor(arcColor);
                    mProgressPaint.setColor(progressColor);
                    invalidate();
                } else {
                    isSlider = false;
                }
                setPressed(false);
                break;
            case MotionEvent.ACTION_CANCEL://
                onStopTrackingTouch();
                if (!isSlider) {
                    mArcPaint.setColor(arcColor);
                    mProgressPaint.setColor(progressColor);
                    invalidate();
                } else {
                    isSlider = false;
                }
                setPressed(false);
                break;
        }
        return true;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if ((mThumb != null) && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
        invalidate();
    }

    private void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this);
        }
    }

    private void updateOnTouch(MotionEvent event) {
//        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
//        if (ignoreTouch) {
//            return;
//        }
        setPressed(true);
        if ((controllerMode == MODE_NORMAL) || isSlider) {
            double mTouchAngle = getTouchDegrees(event.getX(), event.getY());
            int progress = getProgressForAngle(mTouchAngle);
            onProgressRefresh(progress, true);
        } else {
            switch (controllerMode) {
                case MODE_COLOR_PICKER:
                    calculateHSV(event.getX(), event.getY());
                    break;
                case MODE_WARM_COLD_PICKER:
                    calculateColdWarm(event.getX(), event.getY());
                    break;
            }
        }
    }

    private void setIsSlider(float mTouchRadius) {

        if ((mTouchRadius > mInsideCirRadius) && !isPressed()) {
            isSlider = true;
            setTouchInSide(mTouchInside);
        } else if (!isPressed()) {
            mTouchIgnoreRadius = 1;
        }

    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        int tempArcWidth = (getArcWidth() + getProgressWidth()) * 3;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (controllerMode != MODE_NORMAL)
            setIsSlider(touchRadius);
        if ((touchRadius < mTouchIgnoreRadius) || (touchRadius > (mArcRadius + tempArcWidth))) {
            ignore = true;
        }
        //Log.d("Slider: ", "TouchRadius: " + touchRadius + " mTouIgnoreRadius: " + mTouchIgnoreRadius);

        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        //invert the x-coord if we are rotating anti-clockwise
        x = (mClockwise) ? x : -x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;

        }
        angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {

        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;


    }

    private float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }

    private void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    private void updateThumbPosition() {
        int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
        mThumbXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle)));
        mThumbYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle)));
    }

    private void updateProgress(int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, progress, fromUser);
        }

        progress = (progress > mMax) ? mMax : progress;
        progress = (mProgress < 0) ? 0 : progress;

        mProgress = progress;
        float previousSweep = mProgressSweep;
        mProgressSweep = (float) progress / mMax * mSweepAngle;
        if (mProgressSweep >= previousSweep) {
            animMode = AnimMode.NORMAL;
        } else {
            animMode = AnimMode.REVERSE;
        }

        updateThumbPosition();

        invalidate();
    }

    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        mOnSeekArcChangeListener = l;
    }

    public void setProgress(int progress) {

        updateProgress(progress, false);
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public int getArcRotation() {
        return mRotation;
    }

    public void setArcRotation(int mRotation) {
        this.mRotation = mRotation;
        updateThumbPosition();
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        updateThumbPosition();
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
        updateThumbPosition();
    }

    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
        }

    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfheight = (int) mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = (int) mThumb.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreRadius = mArcRadius
                    - Math.min(thumbHalfWidth, thumbHalfheight) - (mArcRadius * 15) / 100;
        }
    }

    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }

    public boolean isStartAnimation() {
        return startAnimation;
    }

    public void setStartAnimation(boolean startAnimation) {
        this.startAnimation = startAnimation;
    }

    public int getControllerMode() {
        return controllerMode;
    }

    public void setControllerMode(int controllerMode) {
        this.controllerMode = controllerMode;
        setTouchInSide(mTouchInside);
        prepareBitmap(mInsideCirRadius * 2);
        invalidate();
    }

    private void calculateHSV(float x, float y) {

        int cx = (int) (x - mTranslateX);
        int cy = (int) (y - mTranslateY);
        double touchRadius = Math.sqrt(cx * cx + cy * cy);
        colorHSV[0] = (float) (Math.toDegrees(Math.atan2(cy, cx)) + 240);
        if (colorHSV[0] > 360)
            colorHSV[0] -= 360;
        if (touchRadius < mInsideCirRadius) {
            colorHSV[1] = Math.max(0f, Math.min(1f, (float) (touchRadius / mInsideCirRadius)));
            colorHSV[2] = 1f;
            xPosInside = x;
            yPosInside = y;
        } else {
            colorHSV[1] = 1f;
            xPosInside = (float) (mInsideCirRadius * Math.cos(Math.toRadians(colorHSV[0] - 60)));
            yPosInside = (float) (mInsideCirRadius * Math.sin(Math.toRadians(colorHSV[0] - 60)));
            xPosInside = mTranslateX - xPosInside;
            yPosInside = mTranslateY - yPosInside;
        }
        Log.d("Color", "Hue: " + colorHSV[0] + " Saturation: " + colorHSV[1]);
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onColorChanged(Color.HSVToColor(colorHSV));
        }
        mProgressPaint.setColor(Color.parseColor("#00FFFFFF"));
        mArcPaint.setColor(Color.HSVToColor(colorHSV));
        invalidate();
    }

    private void calculateColdWarm(float x, float y) {

        int cx = (int) (x - mTranslateX);
        int cy = (int) (y - mTranslateY);
        double touchRadius = Math.sqrt(cx * cx + cy * cy);
        double value;
        if (touchRadius < mInsideCirRadius) {
            yPosInside = y;
            value = (cy * Math.sin(Math.toRadians((Math.toDegrees(Math.atan2(cy, cx)) + 180))));

        } else {

            yPosInside = (float) (mInsideCirRadius * Math.sin(Math.toRadians((Math.toDegrees(Math.atan2(cy, cx)) + 180))));
            yPosInside = mTranslateY - yPosInside;
            value = (mInsideCirRadius * Math.sin(Math.toRadians((Math.toDegrees(Math.atan2(cy, cx)) + 180))));
        }

        xPosInside = mTranslateX;
        if (yPosInside > mTranslateY) {
            if (mOnSeekArcChangeListener != null) {
                mOnSeekArcChangeListener.onCWChanged(255 - getTemperature((int) value), 255);
            }
        } else if (yPosInside == mTranslateY) {
            if (mOnSeekArcChangeListener != null) {
                mOnSeekArcChangeListener.onCWChanged(255, 255);
            }
        } else if (yPosInside < mTranslateY) {
            if (mOnSeekArcChangeListener != null) {
                mOnSeekArcChangeListener.onCWChanged(255, 255 - getTemperature((int) value));
            }
        }

        Log.d("ColdWarm: ", "x: " + xPosInside + "y: " + yPosInside);
        invalidate();
    }

    private int getTemperature(int y) {


        return (Math.abs(y) * 255) / mInsideCirRadius;
    }

    public void setColor(int color) {

        Color.colorToHSV(color, colorHSV);
        colorHSV[0] = (colorHSV[0] - 60) % INT_MAX_DEC;
        if (colorHSV[0] < 0) {
            colorHSV[0] = 360 + colorHSV[0];
        }

        setColorCoordinate();
        //mProgress = (int) (colorHSV[2]*100);
        //updateProgress(mProgress, false);
        invalidate();

    }


    public void setTemperature(int value) {

        temperatureValue = (value * mInsideCirRadius) / 100;
        setTempCoordinate();
        invalidate();

    }

    private void setTempCoordinate() {
        yPosInside = -temperatureValue;
//      yPosInside = (yPosInside <0)? yPosInside+mCursorRadius : yPosInside-mCursorRadius;
        yPosInside = mTranslateY - yPosInside;
        xPosInside = mTranslateX;
    }

    private void setColorCoordinate() {

        xPosInside = (float) (colorHSV[1] * mInsideCirRadius * Math.cos(Math.toRadians(colorHSV[0])));
        yPosInside = (float) (colorHSV[1] * mInsideCirRadius * Math.sin(Math.toRadians(colorHSV[0])));
//        xPosInside = (xPosInside <0)? xPosInside+mCursorRadius : xPosInside-mCursorRadius;
//        yPosInside = (yPosInside <0)? yPosInside-mCursorRadius : yPosInside+mCursorRadius;
        xPosInside = mTranslateX - xPosInside;
        yPosInside = mTranslateY - yPosInside;
        // Log.d("Color", "Radius: " + mInsideCirRadius + " xPosInside: " + xPosInside +" yPosInside"+yPosInside);

    }

    private enum AnimMode {
        REVERSE, NORMAL
    }

    public interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param circularController The SeekArc whose progress has changed
         * @param progress           The current progress level. This will be in the range
         *                           0..max where max was set by
         *                           {@link ProgressArc#setMax(int)}. (The default value for
         *                           max is 100.)
         * @param fromUser           True if the progress change was initiated by the user.
         */
        void onProgressChanged(CircularController circularController, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param circularController The SeekArc in which the touch gesture began
         */
        void onStartTrackingTouch(CircularController circularController);

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param circularController The SeekArc in which the touch gesture began
         */
        void onStopTrackingTouch(CircularController circularController);

        void onColorChanged(int color);

        void onCWChanged(int cold, int warm);
    }


}
