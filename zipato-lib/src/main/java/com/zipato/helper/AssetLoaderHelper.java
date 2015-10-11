/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zipato.appv2.R;
import com.zipato.appv2.R.drawable;
import com.zipato.util.TagFactoryUtils;

import java.io.File;

/**
 * Created by murielK on 8/17/2015.
 */
public class AssetLoaderHelper {

    private static final String TAG = TagFactoryUtils.getTag(AssetLoaderHelper.class);
    private static final String ASSET_PATH = "file:///android_asset/icons/";
    private final Picasso picasso;
    private final Context context;

    public AssetLoaderHelper(Context context, Picasso picasso) {
        this.picasso = picasso;
        this.context = context;
    }

    public void loadAsset(final String fileName, final ImageView target) {
        loadAsset(fileName, null, target);
    }

    private void load(final String path, final ImageView target, final Callback callback) {
        picasso.load(path)
                .error(drawable.empty_drawable)
                .noFade()
                .noPlaceholder()
                .into(target, callback);
    }

    private void load(final File file, final ImageView target, final Callback callback) {
        picasso.load(file)
                .error(drawable.empty_drawable)
                .noFade()
                .noPlaceholder()
                .into(target, callback);
    }

    public void loadAsset(final String fileName, final String defFileName, final ImageView target) {
        Log.d(TAG, String.format("loading image: %s from %s", fileName, context.getFilesDir().getPath() + '/'));
        load(new File(context.getFilesDir().getPath() + '/' + fileName), target, new Callback() { // first try, to load from updated icons
            @Override
            public void onSuccess() {
                Log.d(TAG, String.format("%s loaded from %s", fileName, context.getFilesDir().getPath() + '/'));
            }

            @Override
            public void onError() {// 2nd try to load from assets
                Log.d(TAG, String.format("loading image: %s from assets", fileName));
                load(ASSET_PATH + fileName, target, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, String.format("%s loaded from assets", fileName));
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, String.format("%s could not be load from both path....", fileName));
                        if ((target != null) && (defFileName != null)) {
                            Log.d(TAG, String.format("recursive retry with provided default name: %s", defFileName));
                            loadAsset(defFileName, null, target); //recursive call for the default file

                        } else if (target != null) {
                            Log.d(TAG, "no default icon provided applying empty drawable");
                            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN)
                                target.setBackground(context.getResources().getDrawable(R.drawable.empty_drawable));
                            else
                                target.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.empty_drawable));
                        }
                    }
                });
            }
        });
    }
}
