/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.customview.TempPickerView;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 8/3/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_OS_RAM_RGBW)
public class VCOsRamRGBW extends VCZipaRGBW {

    private static final int MIN_TEMP_RANGE = 2700;
    private static final int MAX_TEMP_RANGE = 6500;
    private static final String TAG = VCOsRamRGBW.class.getSimpleName();

    public VCOsRamRGBW(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    private static int convertTempToZigBee(int temperature) {
        Log.d(TAG, "temperature: " + temperature);
        final int tempRange = 100 - (-100);
        final int zigBeeTempRage = MAX_TEMP_RANGE - MIN_TEMP_RANGE;
        int zigBeeTemp = (((temperature - (-100)) * zigBeeTempRage) / tempRange) + MIN_TEMP_RANGE;
        zigBeeTemp = (MIN_TEMP_RANGE + MAX_TEMP_RANGE)/*  to invert, ie inMax become min and inMin become min and vis versa!*/ - zigBeeTemp;
        Log.d(TAG, " ZigBee value: " + temperature + " after conversion");
        return zigBeeTemp;
    }

    private static int convertZigBeeToTemp(int zigBee) {
        Log.d(TAG, "ZigBee value: " + zigBee);
        zigBee = (MIN_TEMP_RANGE + MAX_TEMP_RANGE) - zigBee;
        Log.d(TAG, "Inverted ZigBee value: " + zigBee);
        final int tempRange = 100 - (-100);
        final int zigBeeTempRage = MAX_TEMP_RANGE - MIN_TEMP_RANGE;
        final int temperature = (((zigBee - MIN_TEMP_RANGE) * tempRange) / zigBeeTempRage) - 100;
        Log.d(TAG, " temperature: " + temperature + " after conversion");
        return temperature;
    }

    @Override
    protected int getDeviceActualTemp(TypeReportItem item) {
        return convertZigBeeToTemp(super.getDeviceActualTemp(item));

    }

    @Override
    protected int getTargetAttrID() {
        return super.getTargetAttrID();
    }

    @Override
    protected int getControllerType() {
        return super.getControllerType();
    }

    @Override
    protected void onColorChange(TypeReportItem item, int color) {
        sentJustRGB(item, color);
    }


    @Override
    protected void onWCChange(TypeReportItem item, int[] coldWarm) {
        try {
            int temp = TempPickerView.convertToTemp(coldWarm);
            final Attribute attr = item.getAttrOfID(BaseTypesFragment.TEMPERATURE);
            sendAttributeValue(attr.getUuid(), String.valueOf(convertTempToZigBee(temp)));
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }
}
