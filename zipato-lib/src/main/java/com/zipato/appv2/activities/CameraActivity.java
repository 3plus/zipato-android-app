/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.zipato.appv2.R;

import java.util.HashMap;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by murielK on 9/8/2014.
 */
public class CameraActivity extends BaseCameraActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();
    VideoView videoView;
    private boolean isPlayerReady;


    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        setContentView(R.layout.test_camera_activity);
        videoView = (VideoView) findViewById(R.id.videoView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarCam);
    }

    @Override
    protected void onPreInit() {
        isPlayerReady = false;
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostInit() {
        initPlayer();
        videoView.start();
        isPlayerReady = true;
    }

    private void initPlayer() {
        try {
            switch (streamingType) {
                case HIGH_STREAM:
                    mediaURL = camera.getHiQualityStream();
                    Log.d(TAG, "high quality stream, URL: " + camera.getHiQualityStream());
                    break;
                case LOW_STREAM:
                    mediaURL = camera.getLowQualityStream();
                    Log.d(TAG, "low quality stream, URL:" + camera.getLowQualityStream());
                    break;
                default:
                    exitError();
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }

        if (mediaURL == null) {
            exitError();
            return;
        }
        if (videoView == null)
            exitError();

        // videoView.setBufferSize(128);
        if (localFlag) {
            videoView.setVideoURI(Uri.parse(mediaURL));
        } else {
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("rtsp_transport", "tcp");
            options.put("reorder_queue_size", "1");
            options.put("max_delay", "2");
            videoView.setVideoURI(Uri.parse(mediaURL), options);
        }

        if (camera.getName() == null) {
            videoView.setNameCamera("");
        } else {
            videoView.setNameCamera(camera.getName());
        }
        videoView.requestFocus();
        final MediaController mediaController = new MediaController(this);
        mediaController.setOnSnapShotListner(new MediaController.OnSnapShotListner() {
            @Override
            public void snapShot() {

                if (camera != null)
                    takeSnapshot();
            }
        });
        mediaController.setOnExitListner(new MediaController.OnExitListner() {
            @Override
            public void onExit() {
                finish();
            }
        });

        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // optional need Vitamio 4.0
                mp.setPlaybackSpeed(1.0f);
                mp.start();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(final MediaPlayer mp, int what, int extra) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressBar != null)
                            progressBar.setVisibility(View.GONE);
                        mp.stop();
                        if (retry) {
                            retry = false;
                            init(false);
                        } else
                            exitError();
                    }
                });

                return true;
            }
        });

        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                        @Override
                                        public boolean onInfo(final MediaPlayer mp, int what, int extra) {
                                            switch (what) {
                                                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (progressBar != null)
                                                                progressBar.setVisibility(View.GONE);
                                                            mp.start();
                                                        }
                                                    });
                                                    break;

                                            }
                                            return true;
                                        }
                                    }
        );

        videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_LOW);

    }

    @Override
    public void onResume() {
        super.onResume();
        if ((videoView != null) && isPlayerReady) {
            videoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onSingleTap() {
        videoView.toggleMediaControlsVisiblity();
    }

//    @TargetApi(19)
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//            if (hasFocus) {
//                getWindow().getDecorView()
//                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            }
//        }
//    }

}
