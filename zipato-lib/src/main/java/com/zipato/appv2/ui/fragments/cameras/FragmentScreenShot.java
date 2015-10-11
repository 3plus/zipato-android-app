/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.cameras;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zipato.appv2.R;
import com.zipato.customview.TouchImageView;
import com.zipato.model.camera.Camera;
import com.zipato.model.camera.CameraRepository;
import com.zipato.model.camera.SVFileRest;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;

/**
 * Created by murielK on 2/2/2015.
 */
public class FragmentScreenShot extends BaseCameraFragment {

    public static final String INDEX_KEY = "";
    private static final String TAG = FragmentScreenShot.class.getSimpleName();
    @Inject
    protected CameraRepository cameraRepository;
    @Inject
    protected ApiV2RestTemplate restTemplate;
    @Inject
    protected Picasso picasso;
    @InjectView(R.id.imageViewSS)
    TouchImageView image;
    @InjectView(R.id.progressBarSS)
    ProgressBar progressBar;
    @Inject
    List<SVFileRest> files;
    private int currentIndex;

    public static FragmentScreenShot getInstance(int index) {

        final Bundle bundle = new Bundle(1);
        bundle.putInt(INDEX_KEY, index);
        FragmentScreenShot fragmentScreenShot = new FragmentScreenShot();
        fragmentScreenShot.setArguments(bundle);
        return fragmentScreenShot;
    }

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ((getArguments() != null) && !files.isEmpty()) {
            currentIndex = getArguments().getInt(INDEX_KEY);
            final SVFileRest svFileRest = files.get(currentIndex);
            try {
                picasso.load(svFileRest.getUrl()).into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        try {
                            image.setZoom(1);
                            progressBar.setVisibility(View.GONE);
                        } catch (Exception e) {
                            //
                        }
                    }

                    @Override
                    public void onError() {
                        try {
                            progressBar.setVisibility(View.GONE);
                            image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_img_fail));
                        } catch (Exception e) {
                            //
                        }
                    }
                });

            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        }
//        } else {
//            if (image != null)
//                image.setImageDrawable(getSherlockActivity().getResources().getDrawable(R.drawable.ic_img_fail));
//        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventBus.post(v);
            }
        });


    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_screen_shot;
    }

    @Override
    protected void onPostViewCreate() {

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

}
