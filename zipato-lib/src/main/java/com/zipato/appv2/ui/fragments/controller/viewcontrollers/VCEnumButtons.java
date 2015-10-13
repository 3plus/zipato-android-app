/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Created by Mur0 on 8/23/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_VC_ENUM_BUTTONS)
public class VCEnumButtons extends AbsPagePointerAdapter {

    private final String TAG = TagFactoryUtils.getTag(VCEnumButtons.class);
    private final List<Entry<String, String>> listKeyValue = new ArrayList<>();
    private String tempUserSetKey;

    public VCEnumButtons(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return 0;
    }

    @Override
    public Animation[] getOnNextAnimation() {
        Animation anim0 = new AlphaAnimation(0.0f, 1.0f); // at least it wont be stored in the ram until this is killed!
        anim0.setDuration(450);
        Animation anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(450);
        anim1.setStartOffset(150);
        Animation anim2 = new AlphaAnimation(0.0f, 1.0f);
        anim2.setDuration(450);
        anim2.setStartOffset(250);
        Animation anim3 = new AlphaAnimation(0.0f, 1.0f);
        anim3.setDuration(350);
        anim3.setStartOffset(350);
        Animation anim4 = new AlphaAnimation(0.0f, 1.0f);
        anim4.setDuration(300);
        anim4.setStartOffset(400);
        Animation anim5 = new AlphaAnimation(0.0f, 1.0f);
        anim5.setDuration(250);
        anim5.setStartOffset(500);
        return new Animation[]{anim0, anim1, anim2, anim3, anim4, anim5};
    }

    @Override
    public Animation[] getOnPrevAnimation() {
        Animation anim0 = new AlphaAnimation(0.0f, 1.0f); // at least it wont be stored in the ram until this is killed!
        anim0.setDuration(450);
        Animation anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(450);
        anim1.setStartOffset(150);
        Animation anim2 = new AlphaAnimation(0.0f, 1.0f);
        anim2.setDuration(450);
        anim2.setStartOffset(250);
        Animation anim3 = new AlphaAnimation(0.0f, 1.0f);
        anim3.setDuration(350);
        anim3.setStartOffset(350);
        Animation anim4 = new AlphaAnimation(0.0f, 1.0f);
        anim4.setDuration(300);
        anim4.setStartOffset(400);
        Animation anim5 = new AlphaAnimation(0.0f, 1.0f);
        anim5.setDuration(250);
        anim5.setStartOffset(500);
        return new Animation[]{anim5, anim4, anim3, anim2, anim1, anim0};
    }

    @Override
    public Animation[] getOnBindAnimation() {
        return null;
    }

    @Override
    public int getPointerCount() {
        return listKeyValue.size();
    }

    @Override
    public String getLabelForPointer(int pointer) {
        final int index = pointer - 1;
        return (index < listKeyValue.size()) ? languageManager.translate(Utils.capitalizer(listKeyValue.get(index).getValue())) : "";
    }

    @Override
    public boolean isPointerEnable(int pointer) {
        final TypeReportItem item = getTypeReportItem();
        final int index = pointer - 1;
        if (index >= listKeyValue.size())
            return false;

        if (tempUserSetKey != null) { // let  use the latest user  key until next update to highlight the recent pressed button
            return listKeyValue.get(index).getKey().equals(tempUserSetKey);
        }
        String value = "";
        try {
            value = getValueForAttr(item.getAttributes()[item.getMasterIndex()].getUuid());
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
        return listKeyValue.get(index).getKey().equals(value);
    }

    @Override
    public void handleViewClick(TextView textView) {
        final int index = getPointFromTextView(textView) - 1;
        if (index < listKeyValue.size()) {
            try {
                final TypeReportItem item = getTypeReportItem();
                sendAttributeValue(item.getAttributes()[item.getMasterIndex()].getUuid(), listKeyValue.get(index).getKey());
                tempUserSetKey = listKeyValue.get(index).getKey();
                manualRefresh();
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        }
    }

    @Override
    public void handleLongClick(TextView textView) {
        //Empty
    }

    @Override
    public void onPreBind(TypeReportItem item) {
        genListEnumValueObject(); // this is not very good! but will do it for now!
        tempUserSetKey = null;
    }

    @Override
    public boolean hasLogic() {
        return false;
    }

    private void genListEnumValueObject() {
        final TypeReportItem item = getTypeReportItem();
        try {
            LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) attributeRepository.get(item.getAttributes()[item.getMasterIndex()].getUuid()).getConfig().getEnumValues();
            listKeyValue.clear();
            if (map != null) {
                listKeyValue.addAll(map.entrySet());
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }
}
