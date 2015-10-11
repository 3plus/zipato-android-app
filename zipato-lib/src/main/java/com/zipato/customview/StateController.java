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
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zipato.appv2.R;

import java.util.Map;

/**
 * Created by murielK on 7/30/2014.
 */


public class StateController extends View {

    private final int mAngleOffset = -90;
    int middleColorTrue;
    int middleColorFalse;
    int middleColorPressed;
    private Drawable icon;
    private Drawable iconOff;
    private boolean customColor = false;
    private int mMiddleWidth = 6;
    private int mArcWidth = 9;
    //  private boolean startAnimation = true;
    private Shader shaderTrue;
    private Shader shaderFalse;
    private Shader shaderPressed;
    private Matrix mMatrix = new Matrix();
    private int mArcRadius = 0;
    private RectF mArcRectStart = new RectF();
    private RectF mArcRectMiddle = new RectF();
    private RectF mArcRecEnd = new RectF();
    private RectF mCircle = new RectF();
    private Paint mArcPaint;
    private Paint mMiddlePaint;
    private float mTranslateX;
    private float mTranslateY;
    private Paint mCirclePaint;
    //private float layoutWidth;
    private TextPaint textPaint;
    private boolean stateBool = false;
    private States state = States.FALSE;
    private Map<String, String> enumValues;
    private StatesListner listner;

    public StateController(Context context) {
        super(context);
        init(context, null, 0);

    }

    public StateController(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public StateController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {


        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;
        int thumbHalfheight = 0;
        int thumbHalfWidth = 0;
        int thumbHalfheight2 = 0;
        int thumbHalfWidth2 = 0;
        int arcColor = res.getColor(R.color.progress_gray);
        int circleColor = res.getColor(R.color.stat_circle);
        int textColor = res.getColor(R.color.state_text_color);
        middleColorTrue = res.getColor(R.color.state_true);
        middleColorFalse = res.getColor(R.color.grey);
        middleColorPressed = res.getColor(R.color.stat_circle);
        mArcWidth = (int) (mArcWidth * density);
        mMiddleWidth = (int) (mMiddleWidth * density);
        if (attrs != null) {

            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.StateController, defStyle, 0);

            Drawable icon = a.getDrawable(R.styleable.StateController_stateController_icon);
            if (icon != null) {
                this.icon = icon;
                thumbHalfheight = (int) this.icon.getIntrinsicHeight() / 2;
                thumbHalfWidth = (int) this.icon.getIntrinsicWidth() / 2;
                this.icon.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                        thumbHalfheight);
            }

            Drawable iconOff = a.getDrawable(R.styleable.StateController_stateController_iconOff);
            if (iconOff != null) {
                this.iconOff = iconOff;
                thumbHalfheight2 = (int) this.iconOff.getIntrinsicHeight() / 2;
                thumbHalfWidth2 = (int) this.iconOff.getIntrinsicWidth() / 2;
                this.iconOff.setBounds(-thumbHalfWidth2, -thumbHalfheight2, thumbHalfWidth2,
                        thumbHalfheight2);
            }

            customColor = a.getBoolean(R.styleable.StateController_stateController_customColor, customColor);

            arcColor = a.getColor(R.styleable.StateController_stateController_arcColor, arcColor);
            mArcWidth = (int) a.getDimension(R.styleable.StateController_stateController_arcWidth, mArcWidth);
            mMiddleWidth = (int) a.getDimension(R.styleable.StateController_stateController_middleWidth, mMiddleWidth);
            middleColorTrue = a.getColor(R.styleable.StateController_stateController_stateTrueColor, middleColorTrue);
            middleColorFalse = a.getColor(R.styleable.StateController_stateController_stateFalseColor, middleColorFalse);
            middleColorPressed = a.getColor(R.styleable.StateController_stateController_statePressedColor, middleColorPressed);
            textColor = a.getColor(R.styleable.StateController_stateController_textColor, textColor);
            a.recycle();
        }


        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);
        mMiddlePaint = new Paint();
        mMiddlePaint.setAntiAlias(true);
        mMiddlePaint.setStyle(Paint.Style.STROKE);
        mMiddlePaint.setStrokeWidth(mMiddleWidth);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(circleColor);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(textColor);


    }

    @Override
    protected void onDraw(Canvas canvas) {

        final int arcSweep = 360;
        canvas.drawArc(mArcRectStart, mAngleOffset, arcSweep, false, mArcPaint);
        canvas.drawCircle(mTranslateX, mTranslateY, mArcRadius, mCirclePaint);
        String text = enumDisplayResolver(String.valueOf(stateBool)).toUpperCase();

        switch (state) {
            case FALSE:
                if (customColor) {
                    mMiddlePaint.setColor(middleColorFalse);
                } else {
                    mMiddlePaint.setShader(shaderFalse);
                }
                if (iconOff != null)
                    drawIcon(canvas, false);
                else
                    drawBoolean(canvas, text);
                break;
            case TRUE:
                if (customColor) {
                    mMiddlePaint.setColor(middleColorTrue);
                } else {
                    mMiddlePaint.setShader(shaderTrue);
                }
                if (icon != null)
                    drawIcon(canvas, true);
                else
                    drawBoolean(canvas, text);

                break;
            case PRESSED:
                if (customColor) {
                    mMiddlePaint.setColor(middleColorPressed);

                } else {
                    mMiddlePaint.setShader(shaderPressed);
                }
                break;
        }
        canvas.drawArc(mArcRectMiddle, mAngleOffset, arcSweep, false,
                mMiddlePaint);
        canvas.drawArc(mArcRecEnd, mAngleOffset, arcSweep, false,
                mArcPaint);

    }

    private void drawIcon(Canvas canvas, boolean stateBoolean) {
        canvas.save();
        canvas.translate(mTranslateX, mTranslateY);
        if (stateBoolean)
            icon.draw(canvas);
        else
            iconOff.draw(canvas);
        canvas.restore();
    }

    private void drawBoolean(Canvas canvas, String text) {

        float textWidth = textPaint.measureText(text);
        float textSize = textPaint.getTextSize();
        while (textWidth > ((mArcRadius * 2) - mArcWidth)) {
            textSize--;
            textPaint.setTextSize(textSize);
            textWidth = textPaint.measureText(text);
        }
        canvas.drawText(text, mTranslateX, mTranslateY + textPaint.getFontSpacing() / 4, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
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
        int tempArcWidth = (mArcWidth + mMiddleWidth) / 2;
        int tempArcWidth2 = tempArcWidth * 2;
        mArcRectMiddle.set(left + tempArcWidth, top + tempArcWidth, (left + arcDiameter) - tempArcWidth, top + (arcDiameter - tempArcWidth));
        mArcRecEnd.set(left + tempArcWidth2, top + tempArcWidth2, (left + arcDiameter) - tempArcWidth2, top + (arcDiameter - tempArcWidth2));
        mCircle.set(left + tempArcWidth * 3, top + tempArcWidth * 3, (left + arcDiameter) - tempArcWidth * 3, (top + arcDiameter) - tempArcWidth * 3);
        shaderTrue = new SweepGradient(mTranslateX, mTranslateY, new int[]{Color.TRANSPARENT, Color.parseColor("#FAD608"), Color.parseColor("#FC6901")}, null);
        mMatrix.reset();
        mMatrix.setRotate(mAngleOffset * (-1), mTranslateX, mTranslateY);
        shaderTrue.setLocalMatrix(mMatrix);
        shaderFalse = new SweepGradient(mTranslateX, mTranslateY, new int[]{Color.TRANSPARENT, Color.parseColor("#FAD608"), Color.parseColor("#FC6901")}, null);
        shaderFalse.setLocalMatrix(mMatrix);
        shaderPressed = new SweepGradient(mTranslateX, mTranslateY, new int[]{Color.TRANSPARENT, Color.parseColor("#FAD608"), Color.parseColor("#FC6901")}, null);
        shaderPressed.setLocalMatrix(mMatrix);
        mArcRadius = ((arcDiameter / 2) - tempArcWidth2) - mArcWidth / 2;
        float xRectF = (float) Math.pow(mArcRadius, 2) / 2;//not really useful for now but i have a dream :)
        xRectF = (float) Math.sqrt(xRectF);
        textPaint.setTextSize(xRectF / 1.2f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ignoreTouch(event.getX(), event.getY())) {
                    break;
                }
                onPressed();
                if (listner != null)
                    listner.onTouch();
                break;
            case MotionEvent.ACTION_MOVE:

                if (ignoreTouch(event.getX(), event.getY())) {
                    onCancel();
                    break;
                }

                break;
            case MotionEvent.ACTION_UP:
                if (ignoreTouch(event.getX(), event.getY())) {
                    onCancel();
                    break;
                }
                if (isPressed())
                    onUpdate();
                break;
            case MotionEvent.ACTION_CANCEL://
                onCancel();
                break;
        }

        return true;
    }

    private void onPressed() {
        setPressed(true);
        state = States.PRESSED;
        invalidate();
    }

    private void onCancel() {
        setPressed(false);
        updateState(stateBool, true);
        if (listner != null)
            listner.onRelease();
    }

    private void onUpdate() {
        setPressed(false);
        updateState(!stateBool, true);
    }

    public boolean getState() {
        return (state == States.TRUE);
    }


    public void setState(boolean state) {
        updateState(state, false);

    }

    private void updateState(boolean state, boolean fromUser) {
        stateBool = state;
        if (stateBool) {
            this.state = States.TRUE;
        } else {
            this.state = States.FALSE;
        }

        if (listner != null) {
            listner.onStateChange(stateBool, enumDisplayResolver(String.valueOf(stateBool)), fromUser);
            listner.onRelease();
        }
        invalidate();
    }

    public void setOnStateChangeListner(StatesListner listner) {
        this.listner = listner;
    }

    public Map<String, String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(Map<String, String> enumValues) {
        this.enumValues = enumValues;
    }

    public void setMiddleWidth(int mProgressWidth) {
        this.mMiddleWidth = mProgressWidth;
        mMiddlePaint.setStrokeWidth(mProgressWidth);
    }

    public String enumDisplayResolver(String value) {

        if (enumValues != null) {

            return enumValues.get(value);
        }

        return value;
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        int tempArcWidth = (mArcWidth + mMiddleWidth) * 2;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius > mArcRadius + tempArcWidth) {
            ignore = true;
        }
        return ignore;
    }

    private enum States {
        TRUE, PRESSED, FALSE
    }

    public interface StatesListner {
        void onStateChange(boolean state, String value, boolean fromUser);

        void onTouch();

        void onRelease();
    }


}
