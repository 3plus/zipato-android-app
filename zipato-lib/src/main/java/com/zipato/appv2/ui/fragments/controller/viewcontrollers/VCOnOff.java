/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import butterfork.Bind;
import butterfork.OnClick;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 7/17/2015.
 */
@ViewType("view_controller_state")
public class VCOnOff extends AbsBaseSimpleStatus {

    private static final String TAG = TagFactoryUtils.getTag(VCOnOff.class);

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonOff)
    TextView textViewOff;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonOn)
    TextView textViewOn;

    public VCOnOff(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        textViewOff.setText(capitalizer(languageManager.translate("off").toLowerCase()));
        textViewOn.setText(capitalizer(languageManager.translate("on").toLowerCase()));
    }


    @OnClick(B.id.buttonOff)
    public void onOffClick(View v) {

        defaultBlockResetUpdate();
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.e(TAG, String.format("null item @ %s returning...", "onOffClick method"));
            return;
        }

        final Attribute attribute = getTypeAttributeFor(BaseTypesFragment.STATE_ON_OFF, item);

        if (attribute == null) {
            Log.e(TAG, String.format("null attribute @ %s returning...", "onOffClick method"));
            return;
        }
        textViewValue.setText(capitalizer(attrValueUnitResolver(attribute.getUuid(), "false")));
        sendAttributeValue(attribute.getUuid(), "false");
    }

    @OnClick(B.id.buttonOn)
    public void onOnClick(View v) {
        defaultBlockResetUpdate();
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.e(TAG, String.format("null item @ %s returning...", "onOnClick method"));
            return;
        }
        final Attribute attribute = getTypeAttributeFor(BaseTypesFragment.STATE_ON_OFF, item);
        if (attribute == null) {
            Log.e(TAG, String.format("null attribute @ %s returning...", "onOnClick method"));
            return;
        }
        textViewValue.setText(capitalizer(attrValueUnitResolver(attribute.getUuid(), "true")));
        sendAttributeValue(attribute.getUuid(), "true");
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
        return ((item == null) || (item.getAttributes() == null)) ? -1 : item.getMasterIndex();
    }

    @Override
    public boolean hasLogic() {
        return false;
    }

}
