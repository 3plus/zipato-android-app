/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.cameras;

import android.os.Bundle;

import com.squareup.picasso.Picasso;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.camera.Camera;
import com.zipato.model.camera.CameraRepository;
import com.zipato.v2.client.ApiV2RestTemplate;

import javax.inject.Inject;

/**
 * Created by murielK on 9/11/2014.
 */

public abstract class BaseCameraFragment extends BaseTypesFragment {

    @Inject
    protected CameraRepository cameraRepository;

    @Inject
    protected ApiV2RestTemplate restTemplate;

    @Inject
    protected Picasso picasso;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public Camera getCamera() {
        if (key != null)
            try {
                return cameraRepository.get(typeReportRepository.get(key).getUuid());
            } catch (Exception e) {
                return null;
            }
        return null;
    }

    public ApiV2RestTemplate getRestTemplate(){
        return restTemplate;
    }

}
