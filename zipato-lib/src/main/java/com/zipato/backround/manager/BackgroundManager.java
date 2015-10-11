/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.backround.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.zipato.model.files.FilesObject;
import com.zipato.model.files.ImagesObject;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by murielK on 11/3/2014.
 */
public class BackgroundManager {

    private static final String FILES_LINK = "http://files.zipato.com/backgrounds/android/files.json";
    private static final String FILES = "files.json";
    private static final int THREAD_POOL = 2;
    private RestTemplate restTemplate;
    private Context context;
    private Handler responseHandler;
    private ExecutorService executorService;
    private android.support.v4.util.LruCache<String, Bitmap> mBitmapCache;
    private boolean isUpdating;

    public BackgroundManager(Context context, Handler handler) {
        this.context = context;
        responseHandler = handler;
        executorService = Executors.newFixedThreadPool(THREAD_POOL);
        final int cacheSize = 8000;
        mBitmapCache = new android.support.v4.util.LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                //Log.d(TAG, "bitmap: " + (drawable.getIntrinsicHeight()*drawable.getIntrinsicHeight()*4) / 1024);
                return (bitmap.getRowBytes() * bitmap.getHeight()) / 1024;
            }
        };
    }

    private void checkUpdate() {

    }

    private void dlAll() {
        try {
            FilesObject filesObjects = restTemplate.getForObject(FILES_LINK, FilesObject.class);
            List<ImagesObject> imgList = new ArrayList<ImagesObject>();
            imgList.addAll(Arrays.asList(filesObjects.getImages()));
            downloadQueue(imgList);
            // downloadNewStatus();
        } catch (Exception e) {

        }
    }

    private void downloadQueue(List<ImagesObject> downloadQueue) {
        if (restTemplate == null)
            init();
        boolean isComplete = true;
        for (ImagesObject imagesObject : downloadQueue) {
            byte[] temp;
            try {
                temp = restTemplate.getForObject(imagesObject.getFileUrl(), byte[].class);
            } catch (Exception e) {
                break;
            }
            try {
                FileOutputStream fos = context.openFileOutput(imagesObject.getName(), Context.MODE_PRIVATE);

                fos.write(temp);
                fos.close();
                Log.d("BackGroundManager", "downloaded icon : " + imagesObject.getName());
            } catch (IOException e) {
                Log.e("BackGroundManager", "", e);

            }
        }
    }

    private void init() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        }
    }
}
