/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter.Command;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.customview.TempPickerView;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 7/29/2015.
 */
@ViewType("view_controller_rgbw_zipato")
public class VCZipaRGBW extends AbsRGBW {

    private static final String TAG = VCZipaRGBW.class.getSimpleName();


    public VCZipaRGBW(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    protected static String rgbToHex(int r, int g, int b) {
        final String argb = Integer.toHexString(Color.rgb(r, g, b)); //or could have used this : String.format("%02x%02x%02x", r&0xff, g&0xff, b&0xff);
        final String rgb = argb.substring(2).toUpperCase();
        Log.d(TAG, "RGB value to be sent:" + rgb);
        return rgb;
    }

    private boolean isDeviceOn() {
        return current > 0;
    }

    @Override
    protected boolean isColorOn(TypeReportItem item) {
        int color = getDeviceActualColor(item);
        return (color != -16777216) && isDeviceOn();
    }

    @Override
    protected boolean isWCOn(TypeReportItem item) {
        return !isColorOn(item) && isDeviceOn();
    }

    @Override
    protected int getControllerType() {
        return CONTROLLER_TYPE_RGB_WARM;
    }

    @Override
    protected void onModeSwitch(TypeReportItem item, int mode) {
        //TODO send command to the server to switch mode
        switch (mode) {
            case MODE_COLOR:
                int color = getControllerColor();
                if ((color == -16777216) || (color == 0)) {
                    color = getDeviceActualColor(item);
                }
                if ((color == -16777216) || (color == 0)) {
                    color = Color.RED;
                }
                final int finalColor = color;
                setControllerColor(color); // because views are not invalidate when invisible
                onColorChange(item, color);
                break;
            case MODE_WC:
                int[] wc = new int[2];
                TempPickerView.convertToWC(getDeviceActualTemp(item), wc);
                if ((wc[0] == 0) && (wc[1] == 0))
                    wc = getControllerWC();
                if ((wc[0] == 0) && (wc[1] == 0)) {
                    wc[0] = 255;
                    wc[1] = 255;
                }
                setControllerTemperature(wc);
                onWCChange(item, wc);
                break;
        }
    }

    @Override
    protected void onColorChange(TypeReportItem item, int color) {
        resetWC(item);
        if ("RGBW".equalsIgnoreCase(item.getTemplateId())) {
            sentRGB(item, color);
        } else {
            sentJustRGB(item, color);
        }
    }

    protected void resetWC(TypeReportItem item) {
        try {
            final Attribute coldAttr = item.getAttrOfID(BaseTypesFragment.COLD_WHITE);
            final Attribute warmAttr = item.getAttrOfID(BaseTypesFragment.WARM_WHITE);
            final Command coldCmd = new Command(coldAttr.getUuid(), String.valueOf(0));
            final Command warmCmd = new Command(warmAttr.getUuid(), String.valueOf(0));
            sendCommand(coldCmd, warmCmd);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    protected void sentRGB(TypeReportItem item, int color) {
        try {
            final Attribute redAttr = item.getAttrOfID(BaseTypesFragment.COLOR_RED);
            final Attribute greenAttr = item.getAttrOfID(BaseTypesFragment.COLOR_GREEN);
            final Attribute blueAttr = item.getAttrOfID(BaseTypesFragment.COLOR_BLUE);
            final Command redCmd = new Command(redAttr.getUuid(), String.valueOf(Color.red(color)));
            final Command greenCmd = new Command(greenAttr.getUuid(), String.valueOf(Color.green(color)));
            final Command blueCmd = new Command(blueAttr.getUuid(), String.valueOf(Color.blue(color)));
            sendCommand(redCmd, greenCmd, blueCmd);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    protected void sentJustRGB(TypeReportItem item, int color) {
        try {
            final Attribute attrColor = item.getAttrOfID(BaseTypesFragment.COLOR_RGB);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            sendAttributeValue(attrColor.getUuid(), String.valueOf(rgbToHex(red, green, blue)));
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected void onWCChange(TypeReportItem item, int[] coldWarm) {
        if ("RGBW".equalsIgnoreCase(item.getTemplateId())) {
            resetRGB(item);
        } else restJustRGB(item);

        sentWC(item, coldWarm);
    }

    protected void restJustRGB(TypeReportItem item) {
        try {
            final Attribute colorAttr = item.getAttrOfID(BaseTypesFragment.COLOR_RGB);
            sendAttributeValue(colorAttr.getUuid(), String.valueOf(0));
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    protected void resetRGB(TypeReportItem item) {
        try {
            final Attribute redAttr = item.getAttrOfID(BaseTypesFragment.COLOR_RED);
            final Attribute greenAttr = item.getAttrOfID(BaseTypesFragment.COLOR_GREEN);
            final Attribute blueAttr = item.getAttrOfID(BaseTypesFragment.COLOR_BLUE);
            final Command redCmd = new Command(redAttr.getUuid(), String.valueOf(Color.red(0)));
            final Command greenCmd = new Command(greenAttr.getUuid(), String.valueOf(Color.green(0)));
            final Command blueCmd = new Command(blueAttr.getUuid(), String.valueOf(Color.blue(0)));
            sendCommand(redCmd, greenCmd, blueCmd);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    protected void sentWC(TypeReportItem item, int[] coldWarm) {
        try {
            final Attribute tempAttr = item.getAttrOfID(BaseTypesFragment.TEMPERATURE);
            final Attribute warmAttr = item.getAttrOfID(BaseTypesFragment.WARM_WHITE);
            final Attribute coldAttr = item.getAttrOfID(BaseTypesFragment.COLD_WHITE);
            final Command warmCmd = new Command(warmAttr.getUuid(), String.valueOf(coldWarm[1]));
            final Command coldCmd = new Command(coldAttr.getUuid(), String.valueOf(coldWarm[0]));
            final int temperature = TempPickerView.convertToTemp(coldWarm);
            final Command tempCmd = new Command(tempAttr.getUuid(), String.valueOf(temperature));

            sendCommand(warmCmd, coldCmd, tempCmd);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected int getDeviceActualTemp(TypeReportItem item) {
        int temperature = 0;

        try {
            temperature = Integer.valueOf(getValueForAttr(item.getAttrOfID(BaseTypesFragment.TEMPERATURE).getUuid()));
        } catch (Exception e) {
        }

        return temperature;
    }

    @Override
    protected int getDeviceActualColor(TypeReportItem item) {
        int red = 0;
        int green = 0;
        int blue = 0;
        try {
            final Attribute redAttr = item.getAttrOfID(BaseTypesFragment.COLOR_RED);
            final Attribute greenAttr = item.getAttrOfID(BaseTypesFragment.COLOR_GREEN);
            final Attribute blueAttr = item.getAttrOfID(BaseTypesFragment.COLOR_BLUE);

            red = Integer.valueOf(getValueForAttr(redAttr.getUuid()));
            green = Integer.valueOf(getValueForAttr(greenAttr.getUuid()));
            blue = Integer.valueOf(getValueForAttr(blueAttr.getUuid()));
        } catch (Exception e) {
            Log.d(TAG, "fail resolving red green and blue color =====>", e);
        }

        return Color.rgb(red, green, blue);
    }

    @Override
    protected int getTargetAttrID() {
        return BaseTypesFragment.INTENSITY;
    }

    @Override
    protected boolean isCustomUnit() {
        return false;
    }

    @Override
    protected String getCustomUnit(Attribute attr) {
        return null;
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return ((item == null) || (item.getAttributes() == null)) ? -1 : item.getIndexOfID(BaseTypesFragment.INTENSITY);
    }

    @Override
    public boolean hasLogic() {
        return false;
    }
}
