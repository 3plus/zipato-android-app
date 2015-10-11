/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

/**
 * Created by murielK on 8/3/2015.
 */
@ViewType(R.layout.view_controller_rgb_hue)
public class VCPhilipsHue extends VCZipaRGBW {

    private static final String TAG = TagFactoryUtils.getTag(VCPhilipsHue.class);

    public VCPhilipsHue(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    protected int getTargetAttrID() {
        return BaseTypesFragment.BRIGHTNESS;
    }

    @Override
    protected int getDeviceActualTemp(TypeReportItem item) {
        return 0;
    }


    @Override
    protected boolean isColorOn(TypeReportItem item) {
        return current > 0;
    }

    @Override
    protected void onColorChange(TypeReportItem item, int color) {
        sentJustRGB(item, color);
    }

    @Override
    protected int getControllerType() {
        return CONTROLLER_TYPE_RGB_ONLY;
    }

    @Override
    protected void onModeSwitch(TypeReportItem item, int mode) {
        //Don't do shit!!
    }

    @Override
    public void onOnOffClick(View v) {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter == null) {
            Log.e(TAG, String.format("null adapter @ %s returning...", "onOffClick method"));
            return;
        }
        defaultBlockResetUpdate();
        boolean phState = false;
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.e(TAG, String.format("null item @ %s returning...", "onOffClick method"));
            return;
        }
        final Attribute stateAttr = item.getAttrOfID(BaseTypesFragment.STATE_ON_OFF);
        try {
            phState = Boolean.valueOf(getValueForAttr(stateAttr.getUuid()));
        } catch (Exception e) {
            //
        }
        if (stateAttr == null) {
            Log.e(TAG, String.format("null attribute @ %s returning...", "onOffClick method"));
            return;
        }
        textViewAtrName.setText(languageManager.translate(stateAttr.getName()).toUpperCase());// now it is going to work
        textViewValue.setText(attrValueUnitResolver(stateAttr.getUuid(), String.valueOf(!phState)).toUpperCase());
        sendAttributeValue(stateAttr.getUuid(), String.valueOf(!phState));
    }

    @Override
    protected int getCurrentValue() {
        int currentValue = 0;
        try {
            final Attribute brightAttr = getTypeReportItem().getAttrOfID(getTargetAttrID());
            currentValue = Integer.valueOf(getValueForAttr(brightAttr.getUuid()));

        } catch (Exception e) {
            //empty
        }

        return currentValue;
    }

}
