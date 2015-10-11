/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.appv2.R;
import com.zipato.appv2.R.id;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import butterknife.InjectView;
import butterknife.OnClick;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 7/17/2015.
 */
public abstract class AbsBaseSimpleStatus extends AbsHeader {

    private static final String VC_CACHE_CURSOR_PSO = "VC_CACHE_CURSOR_PSO";
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("status")
    @InjectView(id.textViewStatus)
    TextView textViewAtrName;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewValue)
    TextView textViewValue;
    @InjectView(R.id.layoutTextValue)
    LinearLayout layout;

    public AbsBaseSimpleStatus(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);

    }

    private static void openCloseAnimation(final View v1, final View v2, final AnimationListener animationListener) {

        final float decelerationFactor = 2.3f;
        final long openDuration = 200L;
        final long closeDuration = 600L;

        final ViewPropertyAnimator vpa1 = v1.animate();
        final float prevV1TranslationY = v1.getTranslationY();
        final int viewHeight1 = v1.getHeight();


        final ViewPropertyAnimator vpa2 = v2.animate();
        final float prevV2TranslationY = v2.getTranslationY();
        final int viewHeight2 = v1.getHeight();

        vpa2.setDuration(openDuration).translationY(prevV2TranslationY + viewHeight2);
        vpa1.setDuration(openDuration).translationY(prevV1TranslationY - viewHeight1).setListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (animationListener != null)
                    animationListener.onStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animationListener != null)
                    animationListener.onOpen();
                v1.animate().translationY(prevV1TranslationY).setInterpolator(new DecelerateInterpolator(decelerationFactor)).setListener(new AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animationListener != null)
                            animationListener.onFinish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        if (animationListener != null)
                            animationListener.onFinish();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).setDuration(closeDuration).start();
                v2.animate().translationY(prevV2TranslationY).setInterpolator(new DecelerateInterpolator(decelerationFactor)).setListener(null).setDuration(closeDuration).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
//                v1.setTranslationY(prevV1TranslationY);
//                v2.setTranslationY(prevV2TranslationY);
                if (animationListener != null)
                    animationListener.onCancel();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        vpa2.start();
        vpa1.start();

    }

    protected abstract boolean isCustomUnit();

    protected abstract String getCustomUnit(Attribute attr);

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        final TypeReportItem item = (TypeReportItem) object;
        if ((item.getEntityType() != EntityType.ATTRIBUTE) && ((item.getAttributes() == null) || (item.getAttributes().length == 0))) {
            Log.e(TagFactoryUtils.getTag(this), String.format("no attributes @ %s returning...", "dispatchOnBind method"));
            return;
        }

        if (validateIndexFor(getMainIndexAttrToDisplay(item), item) && isEntryInVCCache(item.getKey(), VC_CACHE_CURSOR_PSO)) {

            removeEntryToVCCache(item.getKey(), VC_CACHE_CURSOR_PSO);
            updateDisplayWithAnimation(getMainIndexAttrToDisplay(item), item);

        } else if (validateIndexFor(getMainIndexAttrToDisplay(item), item)) {

            updateDisplayFor(getMainIndexAttrToDisplay(item), item);

        } else if (item.getEntityType() == EntityType.ATTRIBUTE) {

            final Attribute attribute = attributeRepository.get(item.getUuid());
            updateDisplayFor(attribute);

        }
    }

    private void updateDisplayFor(int index, TypeReportItem typeReportItem) {
        final Attribute attribute = attributeRepository.get(typeReportItem.getAttributes()[index].getUuid());
        updateDisplayFor(attribute);
    }

    private void updateDisplayFor(Attribute attribute) {
        if (attribute == null) {
            textViewValue.setText("-");
            textViewAtrName.setText("-");
            return;
        }

        String value = attrValueUnitResolver(attribute.getUuid());
        if (isCustomUnit()) {
            final String customUnit = getCustomUnit(attribute);
            if ((customUnit != null) && !value.contains(customUnit))
                value += getCustomUnit(attribute);
        }

        textViewValue.setText(capitalizer(value));
        textViewAtrName.setText(capitalizer(languageManager.translate(attribute.getName())));
    }

    protected boolean handleMultiAttr() {
        final TypeReportItem typeReportItem = getTypeReportItem();
        if ((typeReportItem == null) || (typeReportItem.getEntityType() == EntityType.ATTRIBUTE)
                || (typeReportItem.getAttributes() == null) || !(typeReportItem.getAttributes().length > 1))
            return false;

        int tempCursorPos = getMainIndexAttrToDisplay(typeReportItem);
        final Object cache = getValueFromVCCache(typeReportItem.getKey(), VC_CACHE_CURSOR_PSO);
        if (cache != null)
            tempCursorPos = (Integer) cache;

        tempCursorPos++;

        if (tempCursorPos >= typeReportItem.getAttributes().length)
            tempCursorPos = 0;

        if (tempCursorPos == getMainIndexAttrToDisplay(typeReportItem))
            removeEntryToVCCache(typeReportItem.getKey(), VC_CACHE_CURSOR_PSO);

        else putToVCCache(typeReportItem.getKey(), VC_CACHE_CURSOR_PSO, tempCursorPos);

        updateDisplayWithAnimation(tempCursorPos, typeReportItem);

        return true;
    }


    @OnClick(id.layoutTextValue)
    public void onClick(View v) {
        if (handleMultiAttr())
            defaultBlockResetUpdate();

    }

    private void updateDisplayWithAnimation(final int index, final TypeReportItem typeReportItem) {
        openCloseAnimation(textViewValue, textViewAtrName, new AnimationListener() {
            @Override
            public void onStart() {
                layout.setEnabled(false);
            }

            @Override
            public void onOpen() {
                updateDisplayFor(index, typeReportItem);
            }

            @Override
            public void onCancel() {
                updateDisplayFor(index, typeReportItem);

            }

            @Override
            public void onFinish() {
                layout.setEnabled(true);
            }
        });
    }

    interface AnimationListener {

        void onStart();

        void onOpen();

        void onCancel();

        void onFinish(); // or closed
    }

}
