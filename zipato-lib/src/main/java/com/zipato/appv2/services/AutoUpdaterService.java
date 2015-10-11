/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectConnectivity;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;


/**
 * Created by murielK on 7/25/2014.
 */

public class AutoUpdaterService extends Service {

    public static final String TAG = TagFactoryUtils.getTag(AutoUpdaterService.class);

    public static final String SERVICE_COMMAND = "SERVICE_COMMAND";
    public static final long DELAY = 2500L;
    public static final int WHAT_UPDATE = 0;
    public static final int WHAT_SINGLE_UPDATE = 1;
    public static final int WHAT_CHECK_LANGUAGE = 2;

    public static final String THREAD_NAME = "ZipatoService_Thread";
    private static final long DELAY_STATE = 60000L;
    private static final long DELAY_WEATHER = 60000L;

    @Inject
    DeviceStateRepository deviceStateRepository;
    @Inject
    AttributeValueRepository attributeValueRepository;
    @Inject
    EventBus eventBus;
    @Inject
    LanguageManager languageManager;
    @Inject
    InternetConnectionHelper internetConnectionHelper;

    private ServiceHandlerThread serviceHandlerThread;

    private boolean boxSwitched;
    private boolean reset;
    private long previousCall = 0;
    private int errorCount;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((ZipatoApplication) getApplication()).inject(this);
        serviceHandlerThread = new ServiceHandlerThread(this, THREAD_NAME, Process.THREAD_PRIORITY_BACKGROUND, WHAT_CHECK_LANGUAGE, WHAT_UPDATE);
        serviceHandlerThread.start();
        eventBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, " service Started, startId: " + startId);
        if (intent.getExtras() != null) {
            int command = intent.getExtras().getInt(SERVICE_COMMAND);
            serviceHandlerThread.handler.sendMessageAtFrontOfQueue(serviceHandlerThread.handler.obtainMessage(command));
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceHandlerThread != null) {
            serviceHandlerThread.quit();
        }
        eventBus.unregister(this);
        Log.d(TAG, " Service Destroyed ");

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    private void startAutoUpdate() {
        Log.d(TAG, " updating device status and attributes values");
        boolean localReset = false; // maybe  force was set after this started!!
        if (reset)
            localReset = true;

        try {
            try {
                if (localReset) {
                    attributeValueRepository.clearETag();
                    attributeValueRepository.clear();
                    attributeValueRepository.fetchMeteoAttr();
                } else if (canUpdate(DELAY_WEATHER))
                    attributeValueRepository.fetchMeteoAttr();
            } catch (Exception e) {
                Log.d(TAG, "fail to load meteo attributes");
            }

            attributeValueRepository.fetchAll();

            if (localReset) {
                deviceStateRepository.clearETag();
                deviceStateRepository.clear();
                deviceStateRepository.fetchAll();
            } else if (canUpdate(DELAY_STATE))
                deviceStateRepository.fetchAll();

            eventBus.post(new Event(null, Event.EVENT_TYPE_AUTO_UPDATER_SERVICE));
            errorCount = 0;
            eventBus.post(new Event(new ObjectConnectivity(internetConnectionHelper.isOnline(), null), Event.EVENT_TYPE_CONNECTIVITY_EVENT));
            previousCall = System.currentTimeMillis();
            if (localReset) {
                reset = false;
                boxSwitched = false;
            }
            Log.d(TAG, "update done!!...");

        } catch (Exception e) {
            handlerError();
            Log.d(TAG, "", e);
        }
    }

    private void checkLanguage() {
        try {
            languageManager.update();
        } catch (Exception e) {
            Log.d(TAG, "fail updating language", e);
        }

    }

    private boolean canUpdate(long delay) {
        return (System.currentTimeMillis() - previousCall) > delay;
    }

    private void handlerError() {
        if (errorCount >= 2) {
            if (!internetConnectionHelper.isOnline()) {
                eventBus.post(new Event(new ObjectConnectivity(internetConnectionHelper.isOnline(), null), Event.EVENT_TYPE_CONNECTIVITY_EVENT));
            } else {
                final String message = languageManager.translate("connection_time_out_message").replace("{productName}", getResources().getString(R.string.reg_box));
                eventBus.post(new Event(new ObjectConnectivity(internetConnectionHelper.isOnline(), message), Event.EVENT_TYPE_CONNECTIVITY_EVENT));
            }
            errorCount = 0;
        } else {
            errorCount++;
        }
    }

    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_ON_BOX_CHANGE:
                boxSwitched = true; // for new data to be loaded
                break;
            case Event.EVENT_TYPE_REPO_SYNCED:
                if (boxSwitched)
                    reset = true;
                break;
        }
    }

    private static final class ServiceHandler extends Handler {

        private final WeakReference<AutoUpdaterService> weakService;

        public ServiceHandler(WeakReference<AutoUpdaterService> weakService) {
            this.weakService = weakService;
        }

        @Override
        public void handleMessage(Message msg) {
            final AutoUpdaterService service = weakService.get();
            if (service == null)
                return;

            switch (msg.what) {
                case WHAT_SINGLE_UPDATE:
                    service.startAutoUpdate();
                    if (hasMessages(WHAT_UPDATE)) {
                        removeMessages(WHAT_UPDATE);
                        sendEmptyMessageDelayed(WHAT_UPDATE, DELAY);
                    }
                    break;
                case WHAT_UPDATE:
                    service.startAutoUpdate();
                    sendEmptyMessageDelayed(WHAT_UPDATE, DELAY);        //
                    break;
                case WHAT_CHECK_LANGUAGE:
                    service.checkLanguage();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private static final class ServiceHandlerThread extends HandlerThread {
        private final WeakReference<AutoUpdaterService> weakService;
        private ServiceHandler handler;
        private int[] whatToStart;

        public ServiceHandlerThread(AutoUpdaterService service, String name, int priority, int... whatIn) {
            super(name, priority);
            weakService = new WeakReference<>(service);
            if (whatIn == null)
                return;
            final int sizeIn = whatIn.length;
            whatToStart = new int[sizeIn];
            System.arraycopy(whatIn, 0, whatToStart, 0, sizeIn);

        }

        @Override
        protected void onLooperPrepared() {
            handler = new ServiceHandler(weakService);
            onHandlerReady();
        }

        private void onHandlerReady() {
            if (whatToStart == null)
                return;
            for (int w : whatToStart) handler.sendEmptyMessage(w);

        }

    }

}
