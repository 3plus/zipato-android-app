/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 7/15/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_DEFAULT)
public class VCDefault extends AbsBaseSimpleStatus {

    public VCDefault(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    public boolean hasLogic() {
        return false;
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

}
