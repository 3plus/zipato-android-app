/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.scene;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.event.Event;
import com.zipato.model.scene.SceneRepository;
import com.zipato.model.typereport.TypeReportRepository;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.ButterFork;
import de.greenrobot.event.EventBus;


/**
 * Created by murielK on 8/7/2014.
 */
public abstract class BaseSceneFragment extends BaseFragment {
    public static final String UUID_KEY = "UUID_KEY";
    protected UUID uuid = null;
    @Inject
    TypeReportRepository typeReportRepository;
    @Inject
    SceneRepository sceneRepository;
    @Inject
    AttributeRepository attributeRepository;
    @Inject
    ExecutorService executor;
    @Inject
    EventBus eventBus;


    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        if (getArguments() != null) {
            ParcelUuid parcelUuid = getArguments().getParcelable(UUID_KEY);
            if (parcelUuid != null)
                uuid = parcelUuid.getUuid();
        }
    }

    protected abstract int getResourceView();

    protected abstract void onPostViewCreate();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getResourceView() == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        View v = inflater.inflate(getResourceView(), null);
        ButterFork.bind(this, v);
        onPostViewCreate();
        return v;

    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    public void onEventMainThread(Event event) {

    }

}