/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.zipato.appv2.R;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by murielK on 8/24/2015.
 */
public abstract class AbsLevelWithOnOff extends AbsLevel {

    @InjectView(R.id.buttonOnOff)
    ImageView imageView;

    protected AbsLevelWithOnOff(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @OnClick(R.id.buttonOnOff)
    public void onOnOffClick(View v) {
        handleOnOffClick(v);
    }

    protected void handleOnOffClick(View v) {
        defaultBlockResetUpdate();
        if (current == 0) {
            current = 100;
            update("100", DELAY_TO_SEND_COMMAND);
        } else {
            current = 0;
            update("0", DELAY_TO_SEND_COMMAND);
        }
    }
}
