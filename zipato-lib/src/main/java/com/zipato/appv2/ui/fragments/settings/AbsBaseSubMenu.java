/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectMenu;
import com.zipato.util.TypeFaceUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 2/25/2015.
 */
abstract class AbsBaseSubMenu extends BaseFragment {

    @InjectView(R.id.buttonsBack)
    protected ImageView back;
    @SetTypeFace("helvetica_neue_light.otf")
    @InjectView(R.id.textViewTitleSub)
    protected TextView textViewTitleSub;
    @Inject
    protected EventBus eventBus;
    @Inject
    TypeFaceUtils typeFaceUtils;
    @InjectView(R.id.layoutSubHeader)
    LinearLayout layoutSubHeader;

    protected abstract int getResourceView();

    protected abstract void onPostViewCreate();

    protected abstract String provideTitle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getResourceView() == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        final View v = inflater.inflate(getResourceView(), null);
        ButterKnife.inject(this, v);
        typeFaceUtils.applyTypefaceFor(this);
        setView();
        onPostViewCreate();
        return v;
    }

    @OnClick(R.id.layoutSubHeader)
    public void onLayoutClick(View v) {
        backToMain();
    }

    private void setView() {
        languageManager.translateFields(this);
        textViewTitleSub.setText(provideTitle());
    }

    protected void backToMain() {
        eventBus.post(new Event(new ObjectMenu(ObjectMenu.MENU_MAIN, null), Event.EVENT_TYPE_TYPE_MENU));
    }

}
