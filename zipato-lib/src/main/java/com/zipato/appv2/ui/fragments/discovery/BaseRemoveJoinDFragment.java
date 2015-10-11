/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.model.device.Device;
import com.zipato.model.network.DiscoveryRest;
import com.zipato.model.network.DiscoveryStatus;
import com.zipato.model.network.NetworkRepository;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;

/**
 * Created by murielK on 8/21/2014.
 */
public abstract class BaseRemoveJoinDFragment extends BaseDiscoveryFragment {

    protected static final long COUNTER_TIME = 60000L;
    private static final long COUNT_INTERVAL = 1000L;
    private static final String TAG = BaseRemoveJoinDFragment.class.getSimpleName();
    private static final String COUNTER_THREAD_ID = BaseRemoveJoinDFragment.class.getSimpleName() + " COUNTER_THREAD_ID";
    @Inject
    protected ExecutorService executor;
    @Inject
    protected ApiV2RestTemplate restTemplate;
    @Inject
    protected NetworkRepository networkRepository;
    @Bind(B.id.imageViewCir)
    protected ImageView imageCir;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewCounterText)
    protected TextView counterText;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewJoin1)
    protected TextView text1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewJoin2)
    protected TextView text2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonFD)
    protected Button butFD;
    protected ObjectParcel objectParcel;
    protected boolean counterStopped;
    protected String textTitleID = "stating_discovery";
    protected DiscoveryRest discoveryRest;
    protected boolean retry;
    protected String butID = "";
    protected boolean infiniteCount;


    protected void injectView(View v) {
        String counter = "";
        Drawable drawable = null;
        String text1String = "";
        String text2String = "";
        try {
            counter = counterText.getText().toString();
            drawable = imageCir.getDrawable();
            text1String = text1.getText().toString();
            text2String = text2.getText().toString();
        } catch (Exception e) {
        }
        ButterFork.bind(this, v);
        typeFaceUtils.applyTypefaceFor(this);
        setView();
        counterText.setText(counter);
        if (drawable != null)
            imageCir.setBackgroundDrawable(drawable);
        text1.setText(text1String);
        text2.setText(text2String);
    }

    protected abstract Object getObjectBody();

    protected abstract void restart();

    protected abstract void setPostCreateText();

    protected abstract void fail(String text);

    protected abstract void setTextIsRunning();

    protected abstract void setTitleSearching();

    protected abstract boolean isStarted(String status, String type);

    protected abstract void setOnStartedText();

    protected abstract boolean isSuccess(String status, String type);

    protected abstract void setOnSuccessText(List<Device> devices);

    protected abstract boolean isDeviceFound(String status, String type, Device[] devices);

    protected abstract void setOnFoundText();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void setView() {
        butFD.setText(languageManager.translate("force_discovery"));
        Typeface typeface = typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf");
        counterText.setTypeface(typeface);
        imageCir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (retry)
                    restart();
            }
        });
    }

    @Override
    protected void onPostViewCreate() {
        setView();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            objectParcel = getArguments().getParcelable(PARCELABLE_KEY);
        }

        setPostCreateText();
        startCounter(COUNTER_TIME);
        start();

    }

    protected void start() {
        if (!internetConnectionHelper.isOnline()) {
            toast(languageManager.translate("internet_error_refresh"));
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String message = null;
                if (objectParcel != null) {
                    try {
                        discoveryRest = networkRepository.getDiscoveryRes(objectParcel.getUuid(), getObjectBody());
                    } catch (Exception e) {
                        message = languageManager.translate("connection_error");
                    }
                }
                if (discoveryRest != null) {

                    if (!discoveryRest.isRunning()) {
                        if (baseFragmentHandler != null)
                            baseFragmentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isDetached())
                                        setTitleSearching();
                                }
                            });

                        searchingDevice();

                    } else {
                        if (baseFragmentHandler != null)
                            baseFragmentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isDetached())
                                        setTextIsRunning();
                                }
                            });
                    }

                } else {
                    if (!isDetached()) {
                        final String finalMessage = message;
                        baseFragmentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                fail(finalMessage);
                            }
                        });
                    }

                }
            }
        });

    }

    private void searchingDevice() {

        if (objectParcel != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    boolean isFinalFound = false;
                    boolean found = false;
                    boolean started = false;
                    String status = "";
                    String type = "";
                    List<Device> deviceName = new ArrayList<Device>();
                    while (!counterStopped) {
                        DiscoveryStatus discoveryStatus = null;
                        try {
                            discoveryStatus = networkRepository.getDiscoveryStatus(discoveryRest.getNetwork().getUuid(), discoveryRest.getUuid());
                        } catch (Exception e) {
                            Log.d(TAG, "", e);
                        }

                        if ((discoveryStatus == null) || (discoveryStatus.getMessages() == null)) {
                            continue;
                        }
                        int messageSize = discoveryStatus.getMessages().length;
                        for (int i = 0; i < messageSize; i++) {
                            status = discoveryStatus.getMessages()[i].getMessage().getStatus();
                            type = discoveryStatus.getMessages()[i].getMessage().getType();
                            Log.d(getFragmentTag(), " message " + i + " status: " + status + " Type: " + type);
                            if (isStarted(status, type) && !started) {
                                if (baseFragmentHandler != null)
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isDetached())
                                                setOnStartedText();
                                        }
                                    });
                                started = true;
                            }
                            if (isDeviceFound(status, type, discoveryStatus.getDevices())) {
                                if (baseFragmentHandler != null)
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isDetached())
                                                setOnFoundText();
                                        }
                                    });
                            }
                            if ((discoveryStatus.getMessages()[i].getMessage().getData() != null) && (discoveryStatus.getMessages()[i].getMessage().getData().get("final") != null)) {

                                if (Boolean.valueOf(discoveryStatus.getMessages()[i].getMessage().getData().get("final").toString())) {
                                    isFinalFound = true;
                                    Log.d(getFragmentTag(), "final message found " + i + " status: " + status + " Type: " + type);
                                    if (isSuccess(status, type)) {
                                        if ((discoveryStatus.getDevices() != null) && (discoveryStatus.getDevices().length > 0)) {
                                            Log.d(getFragmentTag(), " DeviceFound: " + discoveryStatus.getDevices().length);
                                            for (Device device : discoveryStatus.getDevices()) {
                                                deviceName.add(device);
                                                try {
                                                    Log.d(getFragmentTag(), "Device : " + device.getName());
                                                } catch (Exception e) {

                                                    Log.d(getFragmentTag(), "", e);
                                                }
                                            }

                                        }
                                        found = true;

                                    } else {
                                        found = false;

                                        Log.d(getFragmentTag(), "final message found " + i + " status: " + status + " type: " + type + " DeviceNotFound: ");
                                    }
                                    break;
                                }
                            }

                            try {
                                Thread.sleep(500L);
                            } catch (Exception e) {
                                Log.d(getFragmentTag(), "", e);
                            }
                        }
                        if (isFinalFound)
                            break;
                    }
                    if (found) {
                        if (baseFragmentHandler != null) {
                            final List<Device> finalDeviceNameList = deviceName;
                            baseFragmentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isDetached()) {

                                        setOnSuccessText(finalDeviceNameList);
                                    }
                                }
                            });
                        }

                        postFound();

                    } else {
                        if (baseFragmentHandler != null) {
                            final String finalStatus = status;
                            baseFragmentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    fail(finalStatus);
                                }
                            });
                        }
                        postFail();
                    }
                }
            });
        }
    }


    protected abstract void postFound();

    protected abstract void postFail();


    protected void startCounter(final long durationMilli) {
        retry = false;
        imageCir.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_gradient));
        imageCir.setAnimation(null);
        counterStopped = false;
        Thread counterThread = new Thread(new Runnable() {
            int counter = (int) (durationMilli / COUNT_INTERVAL);

            @Override
            public void run() {
                while (!counterStopped && !isDetached()) {
                    if ((counter < 0) && !infiniteCount) {
                        if (baseFragmentHandler != null)
                            baseFragmentHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isDetached())
                                            setTextOnCounterStopped();
                                    }
                                });

                        cancelDiscovery();
                        counterStopped = true;
                        return;
                    }
                    try {
                        if (baseFragmentHandler != null)
                            baseFragmentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (imageCir != null)
                                        imageCir.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_circle_rotation));
                                    if (!infiniteCount) {
                                        if (counterText != null)
                                            counterText.setText(String.valueOf(counter));
                                        counter--;
                                    } else if (counterText != null) {
                                        counterText.setText("");
                                    }

                                }
                            });
                        try {
                            Thread.sleep(COUNT_INTERVAL); //TODO fix this  (to handler.postDelay)
                        } catch (InterruptedException e) {
                            Log.e(TAG, "", e);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        }, COUNTER_THREAD_ID);

        counterThread.start();
    }

    protected void stopCounter(int restCir, int restBackgroundText, boolean retry) {
        if (!counterStopped)
            counterStopped = true;
        try {
            imageCir.setAnimation(null);
            imageCir.setBackgroundDrawable(getResources().getDrawable(restCir));
            imageCir.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_circle_rotation_2));
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }

        try {
            counterText.setText("");
            if (restBackgroundText > 0)
                counterText.setBackgroundDrawable(getResources().getDrawable(restBackgroundText));

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        this.retry = retry;

    }

    protected abstract void setTextOnCounterStopped();


    @Override
    public void onDestroy() {
        cancelDiscovery();
        super.onDestroy();
    }

    private void cancelDiscovery() {
        if (!(discoveryRest == null) && !counterStopped) {
            counterStopped = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        networkRepository.deleteDiscovery(discoveryRest.getNetwork().getUuid(), discoveryRest.getUuid());
                    } catch (Exception e) {
                        Log.d(getFragmentTag(), "", e);
                    }
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        frameLayout.removeAllViews();
        View view;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_joining_land, null);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_joining, null);
        }

        frameLayout.addView(view);
        injectView(frameLayout);
    }

    @Override
    protected int getResourceView() {
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            return R.layout.fragment_joining_land;
        return R.layout.fragment_joining;
    }

}
