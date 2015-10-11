/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.customview.mjpegview.MjpegView;
import com.zipato.customview.mjpegview.MjpegViewListener;


/**
 * Created by murielK on 2/10/2015.
 */

public class MjpegStreamActivity extends BaseCameraActivity implements MjpegViewListener {

    private static final String TAG = MjpegStreamActivity.class.getSimpleName();
    private MjpegView mjpegView;
    private boolean isFirstError = true;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mjpeg_stream);
        mjpegView = (MjpegView) findViewById(R.id.mjpegView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarMjpeg);
        mjpegView.setListener(this);
    }

    @Override
    protected void onPreInit() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostInit() {
        initPlayer();
    }

    private void initPlayer() {
        if ((camera != null) && (camera.getMjpegUrl() != null)) {
            mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mjpegView.showFps(false);
            mjpegView.setSource(camera.getMjpegUrl());
        } else {
            exitError();
        }
    }

    @Override
    public void onDoubleTap() {
        takeSnapshot();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mjpegView != null) {
            mjpegView.stopPlayback();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((mjpegView != null) && mjpegView.isSuspending()) {
            mjpegView.resumePlayback();
        }
    }

    @Override
    public void success() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((progressBar != null) && (progressBar.getVisibility() == View.VISIBLE))
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void error() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mjpegView != null)
                    mjpegView.stopPlayback();
                if (retry && isFirstError) {
                    retry = false;
                    isFirstError = false;
                    init(false);
                } else
                    exitError();
            }
        });
    }

    @Override
    public void onThreadStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null)
                    progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
}
