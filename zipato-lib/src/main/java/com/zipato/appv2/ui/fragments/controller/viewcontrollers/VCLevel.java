/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 7/17/2015.
 */

@ViewType(layout.view_controller_level)
public class VCLevel extends AbsLevelWithOnOff {


    public VCLevel(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
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
        return ((item == null) || (item.getAttributes() == null)) ? -1 : item.getMasterIndex();
    }

    @Override
    public boolean hasLogic() {
        return false;
    }

}
