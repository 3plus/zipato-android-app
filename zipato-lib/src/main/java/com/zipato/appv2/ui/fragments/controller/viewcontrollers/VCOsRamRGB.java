/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 10/1/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_OS_RAM_RGB)
public class VCOsRamRGB extends VCOsRamRGBW {

    public VCOsRamRGB(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    protected int getControllerType() {
        return CONTROLLER_TYPE_RGB_ONLY;
    }

    @Override
    protected boolean isColorOn(TypeReportItem item) {
        return current > 0;
    }

}
