/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R.color;
import com.zipato.appv2.R.id;
import com.zipato.appv2.activities.BaseActivity;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.customview.ColorPickerView;
import com.zipato.customview.ColorPickerView.ColorPickerListener;
import com.zipato.customview.TempPickerView;
import com.zipato.customview.TempPickerView.WCPickerListener;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import butterknife.InjectView;
import butterknife.OnClick;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 7/27/2015.
 */
public abstract class AbsRGBW extends AbsLevelWithOnOff implements ColorPickerListener, WCPickerListener {

    public static final int CONTROLLER_TYPE_RGB_WARM = 0;
    public static final int CONTROLLER_TYPE_WARM_ONLY = 1;
    public static final int CONTROLLER_TYPE_RGB_ONLY = 2;

    static final int MODE_COLOR = 0;
    static final int MODE_WC = 1;
    private static final String TAG = TagFactoryUtils.getTag(AbsRGBW.class);
    private static final long DURATION = 400L;
    @InjectView(id.rgbWStatus)
    View rgbWStatus;
    @InjectView(id.frameRGBW)
    FrameLayout controllersLayout;
    @InjectView(id.colorPicker)
    ColorPickerView colorPickerView;
    @InjectView(id.wcPicker)
    TempPickerView wcPickerView;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.whiteButton)
    TextView whiteButton;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.rgbButton)
    TextView rgbButton;
    private boolean rgbWCMode;

    protected AbsRGBW(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        whiteButton.setText(capitalizer(languageManager.translate("white").toLowerCase()));
        rgbButton.setText(languageManager.translate("rgb").toUpperCase()); // has to look like this: RGB

        switch (getControllerType()) {
            case CONTROLLER_TYPE_RGB_WARM:
                whiteButton.setVisibility(View.VISIBLE);
                rgbButton.setVisibility(View.VISIBLE);
                colorPickerView.setListener(this);
                wcPickerView.setListener(this);
                break;
            case CONTROLLER_TYPE_RGB_ONLY:
                whiteButton.setVisibility(View.GONE);
                rgbButton.setVisibility(View.VISIBLE);
                colorPickerView.setListener(this);
                break;
            case CONTROLLER_TYPE_WARM_ONLY:
                whiteButton.setVisibility(View.VISIBLE);
                rgbButton.setVisibility(View.GONE);
                wcPickerView.setListener(this);
                break;
        }

        colorPickerView.setVisibility(View.GONE);
        wcPickerView.setVisibility(View.GONE);
    }

    private void updateControllers(TypeReportItem typeReportItem) {
        final int actualColor = getDeviceActualColor(typeReportItem);
        final int actualTemp = getDeviceActualTemp(typeReportItem);
        colorPickerView.setColor(actualColor);
        wcPickerView.setTemperature(actualTemp);
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        final TypeReportItem typeReportItem = (TypeReportItem) object;
        updateControllers(typeReportItem);
        resetColorWcSwitch(typeReportItem);
    }

    private void resetColorWcSwitch(TypeReportItem typeReportItem) {

        updateStatusBar(typeReportItem);

        if (rgbWCMode)
            hideColorWCControllers();
    }

    protected void updateStatusBar(TypeReportItem typeReportItem) {
        final int actualColor = getDeviceActualColor(typeReportItem);
        final int actualWC = getDeviceActualTemp(typeReportItem);
        final Resources res = getAdapter().getContext().getResources();
        switch (getControllerType()) {
            case CONTROLLER_TYPE_RGB_WARM:
                if (isColorOn(typeReportItem)) {
                    rgbWStatus.setBackgroundColor(actualColor);
                } else if (isWCOn(typeReportItem)) {
                    updateWCStatus(actualWC);
                } else {
                    rgbWStatus.setBackgroundColor(res.getColor(color.color_view_controller_rgbw_off));
                }
                break;
            case CONTROLLER_TYPE_RGB_ONLY:
                if (isColorOn(typeReportItem))
                    rgbWStatus.setBackgroundColor(actualColor);
                else
                    rgbWStatus.setBackgroundColor(res.getColor(color.color_view_controller_rgbw_off));
                break;
            case CONTROLLER_TYPE_WARM_ONLY:
                if (isWCOn(typeReportItem))
                    updateWCStatus(actualWC);
                else
                    rgbWStatus.setBackgroundColor(res.getColor(color.color_view_controller_rgbw_off));
                break;
        }
    }

    private void updateWCStatus(int temp) {
        if (temp < 0)
            rgbWStatus.setBackgroundColor(Color.parseColor("#638FBC"));
        else
            rgbWStatus.setBackgroundColor(Color.parseColor("#F59D18"));
    }

    protected abstract boolean isColorOn(TypeReportItem item);

    protected abstract boolean isWCOn(TypeReportItem item);

    protected abstract int getControllerType();


    private void switchToColorPicker() {
        if (controllersLayout.getVisibility() == View.GONE)
            showControllerLayout(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    colorPickerView.setVisibility(View.VISIBLE);
                    wcPickerView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        else {
            colorPickerView.setVisibility(View.VISIBLE);
            wcPickerView.setVisibility(View.GONE);
        }
        rgbWCMode = true;
        final GenericAdapter adapter = getAdapter();
        if (adapter == null) {
            Log.d(TAG, "null adapter on switchToColorPicker returning");
            return;
        }
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.d(TAG, "null item on switchToColorPicker returning");
            return;
        }
        onModeSwitch(item, MODE_COLOR);
    }

    private void switchToWhite() {
        if (controllersLayout.getVisibility() == View.GONE)
            showControllerLayout(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    colorPickerView.setVisibility(View.GONE);
                    wcPickerView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        else {
            colorPickerView.setVisibility(View.GONE);
            wcPickerView.setVisibility(View.VISIBLE);
        }
        rgbWCMode = true;
        final GenericAdapter adapter = getAdapter();
        if (adapter == null) {
            Log.d(TAG, "null adapter on switchToWhite returning");
            return;
        }

        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.d(TAG, "null item on switchToColorPicker returning");
            return;
        }
        onModeSwitch(item, MODE_WC);
    }

    private void showControllerLayout(final AnimatorListener listener) {
        final ViewPropertyAnimator animate = controllersLayout.animate();
        animate.setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                controllersLayout.setVisibility(View.VISIBLE);
                if (listener != null)
                    listener.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (listener != null)
                    listener.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (listener != null)
                    listener.onAnimationCancel(animation);
            }
        });
        if (controllersLayout.getAlpha() != 0.0f)
            controllersLayout.setAlpha(0.0f);

        animate.alpha(1.0f);
        animate.setDuration(DURATION);
        animate.start();
    }

    private void hideControllerLayout(final AnimatorListener listener) {
        final ViewPropertyAnimator animate = controllersLayout.animate();
        animate.setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null)
                    listener.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                controllersLayout.setVisibility(View.GONE);
                if (listener != null)
                    listener.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (listener != null)
                    listener.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (listener != null)
                    listener.onAnimationCancel(animation);
            }
        });
        animate.alpha(0.0f);
        animate.setDuration(DURATION);
        animate.start();
    }

    private void hideColorWCControllers() {
        rgbWCMode = false;
        hideControllerLayout(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                colorPickerView.setVisibility(View.GONE);
                wcPickerView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    @OnClick(id.whiteButton)
    public void onClickWhite(View v) {
        defaultBlockResetUpdate();
        if (wcPickerView.getVisibility() == View.VISIBLE) {
            hideControllerLayout(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    wcPickerView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            return;
        }
        switchToWhite();
        moveUp();
    }

    @OnClick(id.rgbButton)
    public void onClickRGB(View v) {
        defaultBlockResetUpdate();
        if (colorPickerView.getVisibility() == View.VISIBLE) {
            hideControllerLayout(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    colorPickerView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            return;
        }

        switchToColorPicker();
        moveUp();
    }

    private void moveUp() {
        try {
            final int position = getAdapterPosition();
            if (position > 0)
                getRecyclerView().smoothScrollToPosition(position);
        } catch (Exception e) {
            //Empty
        }
    }

    @Override
    public void onOnOffClick(View v) {
//        if (rgbWCMode) {
//            hideColorWCControllers();
//            return;
//        }
        super.onOnOffClick(v);
    }

    protected int[] getControllerWC() {
        return wcPickerView.getColdWarmValue();
    }

    protected int getControllerColor() {
        return Color.HSVToColor(colorPickerView.getColorHSV());
    }

    protected void setControllerColor(final int color) {
        colorPickerView.post(new Runnable() {
            @Override
            public void run() {
                colorPickerView.setColor(color);
            }
        });

    }

    protected void setControllerTemperature(final int temperature) {
        wcPickerView.post(new Runnable() {
            @Override
            public void run() {
                wcPickerView.setTemperature(temperature);
            }
        });
    }

    protected void setControllerTemperature(final int[] coldWarm) {
        wcPickerView.post(new Runnable() {
            @Override
            public void run() {
                wcPickerView.setTemperature(coldWarm);
            }
        });

    }

    protected abstract void onModeSwitch(TypeReportItem item, int mode);

    protected abstract void onColorChange(TypeReportItem item, int color);

    protected abstract void onWCChange(TypeReportItem item, int[] coldWarm);

    protected abstract int getDeviceActualTemp(TypeReportItem item);

    protected abstract int getDeviceActualColor(TypeReportItem item);

    @Override
    public void onStart(int color) {
        BaseActivity baseActivity = (BaseActivity) getContext();
        baseActivity.setSlidingEnabled(false);
        disableRecyclerScrolling();
        disableAdapterUpdate();
        rgbWStatus.setBackgroundColor(color);
    }

    @Override
    public void onTracking(int color) {
        rgbWStatus.setBackgroundColor(color);
    }

    @Override
    public void onStop(int color) {
        BaseActivity baseActivity = (BaseActivity) getContext();
        baseActivity.setSlidingEnabled(true);
        enableRecyclerScrolling();
        resetAdapterUpdate(ViewController.DEFAULT_RESET_DELAY);
        final TypeReportItem item = getTypeReportItem();
        if (item != null)
            onColorChange(item, color);
        // colorPickerView.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void onStart(int[] coldWarm) {
        BaseActivity baseActivity = (BaseActivity) getContext();
        baseActivity.setSlidingEnabled(false);
        disableRecyclerScrolling();
        disableAdapterUpdate();
        // colorPickerView.setBackgroundColor(Color.BLACK);
        updateWCStatus(TempPickerView.convertToTemp(coldWarm));
    }

    @Override
    public void onTracking(int[] coldWarm) {
        updateWCStatus(TempPickerView.convertToTemp(coldWarm));
    }

    @Override
    public void onStop(int[] coldWarm) {
        BaseActivity baseActivity = (BaseActivity) getContext();
        baseActivity.setSlidingEnabled(true);
        enableRecyclerScrolling();
        resetAdapterUpdate(ViewController.DEFAULT_RESET_DELAY);
        final TypeReportItem item = getTypeReportItem();
        if (item != null)
            onWCChange(item, coldWarm);
    }

    @Override
    protected boolean handleMultiAttr() {
        return false;
    }

    @Override
    protected void handleOnOffClick(View v) {
        super.handleOnOffClick(v);
        final TypeReportItem typeReportItem = getTypeReportItem();
        if (typeReportItem == null)
            return;
        updateStatusBar(typeReportItem);
    }

    protected void update(String value, long delay) {
        super.update(value, delay);
        final TypeReportItem typeReportItem = getTypeReportItem();
        if (typeReportItem == null)
            return;
        updateStatusBar(typeReportItem);
    }

}
