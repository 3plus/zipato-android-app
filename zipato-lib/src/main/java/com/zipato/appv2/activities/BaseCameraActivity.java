/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.camera.Camera;
import com.zipato.model.camera.CameraRepository;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.model.typereport.TypeReportRepository;
import com.zipato.translation.LanguageManager;
import com.zipato.util.GestureUtils;
import com.zipato.util.GestureUtils.SimpleGestureListener;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * Created by murielK on 2/10/2015.
 */
public abstract class BaseCameraActivity extends Activity implements OnScaleGestureListener, SimpleGestureListener {

    public static final int HIGH_STREAM = 1;
    public static final int LOW_STREAM = 0;
    public static final String STREAMING_TYPE_KEY = "STREAMING_TYPE_KEY";
    public static final int MJPEG = 2;
    protected volatile boolean initializing;
    protected boolean localFlag;
    protected boolean retry;
    protected Camera camera;
    @Inject
    protected CameraRepository camerasRepository;
    @Inject
    protected TypeReportRepository typeReportRepository;
    @Inject
    protected LanguageManager languageManager;
    @Inject
    protected ExecutorService executor;
    @Inject
    protected InternetConnectionHelper internetConnectionHelper;
    protected String mediaURL = "";
    protected ProgressBar progressBar;
    protected int streamingType;
    private float scaleFactor = 1.0f;
    private float savedSF = 1.0f;
    private ScaleGestureDetector mScaleDetector;
    private GestureUtils gestureUtils;

    protected abstract String getTag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //        if (Build.VERSION.SDK_INT >= 19) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
        ((ZipatoApplication) getApplication()).inject(this);
        mScaleDetector = new ScaleGestureDetector(this, this);
        gestureUtils = new GestureUtils(this, this);
        retrieveFromIntent();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (internetConnectionHelper.isConnectedWifi())
            init(true);
        else
            init(false);
    }

    protected abstract void onPreInit();

    protected abstract void onPostInit();

    protected void init(final boolean isLocal) {
        initializing = true;
        Log.d(getTag(), "isLocal? " + isLocal);
        onPreInit();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.d(getTag(), "reloading camera...\"v2/cameras/{uuid}\""); // reloading cameras to always get updated link.
                    camera = camerasRepository.fetchOne(camera.getUuid(), isLocal);
                    if (isLocal) {
                        boolean pinNoOK = true;
                        try {
                            InetAddress in = InetAddress.getByName(camera.getIpAddress());
                            Log.d(getTag(), "Local Camera info: " + in);
                            Log.d(getTag(), "Trying to ping the camera locally ");
                            if (in.isReachable(1000)) {
                                //camera = cameraTemp;
                                //retry = true;
                                pinNoOK = false;
                                localFlag = true;
                                Log.d(getTag(), "Local Camera PING OK!");
                            } else {
                                Log.d(getTag(), "Cannot ping the local camera");
                            }
                        } catch (Exception e) {
                            Log.d(getTag(), "", e);
                        }
                        if (pinNoOK) {
                            Log.d(getTag(), "switching to remote streaming");
                            camera = camerasRepository.fetchOne(camera.getUuid(), false);
                            retry = false;
                            localFlag = false;
                        }
                    } else {
                        //  camera = cameraTemp;
                        Log.d(getTag(), "force streaming camera remotely! ");
                        retry = false;
                        localFlag = false;
                    }

                } catch (Exception e) {
                    Log.e(getTag(), "", e);
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (camera == null) {
                                Log.d(getTag(), "Camera == null finishing activity");
                                exitError();
                            } else {
                                onPostInit();
                            }
                            initializing = false;
                        }
                    });
                }

            }
        });
    }

    private void retrieveFromIntent() {
        if (getIntent() != null) {
            streamingType = getIntent().getIntExtra(STREAMING_TYPE_KEY, 0);
            TypeReportKey key = getIntent().getParcelableExtra(BaseFragment.PARCELABLE_KEY);
            try {
                camera = camerasRepository.get(typeReportRepository.get(key).getUuid());
            } catch (Exception e) {
                Log.d(getTag(), "", e);
            }
        }
        if (camera == null) {
            exitError();
        }
    }

    protected void exitError() {
        Toast.makeText(this, languageManager.translate("could_not_load_cam"), Toast.LENGTH_LONG).show();
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
        finish();
    }

    protected void takeSnapshot() {
        if (camera == null)
            return;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    camerasRepository.takeSnapShot(camera.getUuid());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BaseCameraActivity.this, languageManager.translate("command_snapshot"), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.d(getTag(), "", e);
                }
            }
        });
    }

    private void panTilt(final GestureUtils.GestureCommand direction) {
        if (camera == null) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    camerasRepository.performPan(camera.getUuid(), direction.name().toLowerCase());
                } catch (Exception e) {
                    Log.d(getTag(), "err", e);
                }
            }
        });

        // Toast.makeText(TestCameraActivity.this, direction.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSwipe(GestureUtils.GestureCommand direction) {

        switch (direction) {
            case UP:
                panTilt(GestureUtils.GestureCommand.UP);
                break;
            case DOWN:
                panTilt(GestureUtils.GestureCommand.DOWN);
                break;
            case LEFT:
                panTilt(GestureUtils.GestureCommand.LEFT);
                break;
            case RIGHT:
                panTilt(GestureUtils.GestureCommand.RIGHT);
                break;
        }
        Toast.makeText(this, direction.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoubleTap() {


    }

    @Override
    public void onSingleTap() {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        try {
            mScaleDetector.onTouchEvent(me);
            if (!mScaleDetector.isInProgress()) {
                gestureUtils.onTouchEvent(me);
            }
        } catch (Exception e) {

        }
        return super.dispatchTouchEvent(me);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        // Don't let the object get too small or too large.
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
        //Toast.makeText(TestCameraActivity.this,String.valueOf(scaleFactor),Toast.LENGTH_SHORT).show();
//        videoView.setScaleX(scaleFactor);
//        videoView.setScaleY(scaleFactor);
        Log.d(getTag(), "Scale value: " + String.valueOf(scaleFactor));
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d("TestCamera", "Scale value: " + String.valueOf(detector.getScaleFactor()));
        // Toast.makeText(TestCameraActivity.this, "SCALE BEGIN", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (scaleFactor < savedSF) {
            panTilt(GestureUtils.GestureCommand.ZOOMIN);
            Toast.makeText(this, "ZOOM IN", Toast.LENGTH_SHORT).show();

        } else {
            panTilt(GestureUtils.GestureCommand.ZOOMOUT);
            Toast.makeText(this, "ZOOM OUT", Toast.LENGTH_SHORT).show();
        }
        savedSF = scaleFactor;


    }

}
