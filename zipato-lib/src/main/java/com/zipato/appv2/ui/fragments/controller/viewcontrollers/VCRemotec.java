/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.model.typereport.TypeReportItem;

/**
 * Created by murielK on 8/20/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_REMOTEC)
public class VCRemotec extends AbsIR {

    public VCRemotec(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return 0;
    }

    @Override
    public int provideViewTypeID() {
        return layout.view_controller_ir;
    }

    @Override
    public String provideLearnAction() {
        return "learn";
    }

    @Override
    public String provideLearnName() {
        return "value";
    }

    @Override
    public String provideReceivedCommand() {
        return null;
    }

}
