/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.icon;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipato.model.icon.IconRest;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IconUpdater {

    private static final String TAG = IconUpdater.class.getSimpleName();
    private static final String ICON_LIST_FILE_NAME = "icon_list.json";
    private static final String ICONS_LIST_URL = "http://icons.zipato.com/res/android/v2/hdpi.json";
    private static final String ICON_BASE_URL = "http://icons.zipato.com/";
    private final ObjectMapper mapper = new ObjectMapper();
    private final AssetManager assetManager;
    private final Context context;
    private RestTemplate restTemplate;

    public IconUpdater(Context context) {
        this.context = context;
        assetManager = context.getAssets();

    }

    public String getJsonString(String path) {
        InputStream is = null;
        boolean isFail = false;

        try {
            is = context.openFileInput(path);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "", e);
            isFail = true;
        } finally {
            if (isFail) {
                try {
                    is = assetManager.open(path);
                } catch (IOException e) {
                    Log.e(TAG, "", e);
                    //  e.printStackTrace();
                }
            }
        }

        try {

            if (is != null) {
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                return new String(buffer, "UTF-8");
            }
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }


        return null;
    }

    private void init() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        }
    }

    public void checkForNewIcon() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                init();
                List<IconRest> downloadQueue = new ArrayList<IconRest>();
                try {
                    String tempJson = getJsonString(ICON_LIST_FILE_NAME);
                    if (tempJson == null)
                        return;

                    IconRest[] currentBase = mapper.readValue(tempJson, IconRest[].class);
                    IconRest[] onlineBase = restTemplate.getForObject(ICONS_LIST_URL, IconRest[].class);
                    if (currentBase.length != onlineBase.length) {
                        reBase();
                        return;
                    }

                    int size = currentBase.length;
                    for (int i = 0; i < size; i++) {

                        if (currentBase[i].getModified().getTime() < onlineBase[i].getModified().getTime()) {
                            downloadQueue.add(onlineBase[i]);
                        }
                    }

                    if (!downloadQueue.isEmpty()) {
                        downloader(downloadQueue);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }).start();
    }

    private void downloader(List<IconRest> downloadQueue) {

        downloadNewStatus();
        downloadQueue(downloadQueue);
    }

    private void downloadNewStatus() {

        byte[] newStatus = restTemplate.getForObject(ICONS_LIST_URL, byte[].class);
        try {

            FileOutputStream fos = context.openFileOutput(ICON_LIST_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(newStatus);
            fos.close();
            Log.d(TAG, "downloaded icon : " + ICON_LIST_FILE_NAME);
        } catch (IOException e) {
            Log.e(TAG, "", e);
        }
    }

    private void downloadQueue(List<IconRest> downloadQueue) {
        if (restTemplate == null)
            init();
        for (IconRest iconRest : downloadQueue) {
            byte[] temp;
            try {
                temp = restTemplate.getForObject(ICON_BASE_URL + iconRest.getIcon(), byte[].class);
            } catch (Exception e) {
                continue;
            }
            try {
                FileOutputStream fos = context.openFileOutput(iconRest.getFilename(), Context.MODE_PRIVATE);
                fos.write(temp);
                fos.close();
                Log.d(TAG, "downloaded icon : " + iconRest.getFilename());
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
        }
    }

    public void reBase() {
        try {
            IconRest[] iconRest = restTemplate.getForObject(ICONS_LIST_URL, IconRest[].class);
            List<IconRest> iconRestList = new ArrayList<IconRest>();
            iconRestList.addAll(Arrays.asList(iconRest));
            downloadQueue(iconRestList);
            downloadNewStatus();
        } catch (Exception e) {

        }

    }
}