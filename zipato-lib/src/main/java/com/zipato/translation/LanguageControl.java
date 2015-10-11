/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.translation;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Created by dbudor on 18/06/2014.
 */

public class LanguageControl extends Control {

    private static final TypeReference<Map<String, String>> MAP_TR = new TypeReference<Map<String, String>>() {
    };
    private static final String ASSET_PATH = "translation";
    private final ObjectMapper mapper;
    private final Context context;


    public LanguageControl(ObjectMapper mapper, Context context) {
        this.mapper = mapper;
        this.context = context;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
                                    boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        // The below is a copy of the default implementation.
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "json");
        ResourceBundle bundle;

        // First try with downloaded files
        //File file = new File(base, resourceName);
        Log.e("LanguageControl", "resource name: " + resourceName);

        FileInputStream is = null;
        try {
            is = context.openFileInput(resourceName);
            Log.d("LanguageControl", "loading context languages");
            Map<String, String> map = mapper.readValue(is, MAP_TR);
            bundle = new MapResourceBundle(map);
            return bundle;
        } catch (Exception e) {
            Log.d("LanguageControl", "", e);
        } finally {
            if (is != null)
                is.close();
        }

        // Second try with assets
        String asset = ASSET_PATH + '/' + resourceName;

        InputStream stream = null;
        try {
            stream = context.getAssets().open(asset);
            Log.d("LanguageControl", "loading locale languages");
            Map<String, String> map = mapper.readValue(stream, MAP_TR);
            bundle = new MapResourceBundle(map);
            return bundle;
        } catch (JsonParseException e) {
            Log.e("LanguageControl", "some dumbass left broken json: " + asset, e);
        } catch (Exception e) {
            Log.d("LanguageControl", e.getMessage(), e);
        } finally {
            if (stream != null)
                stream.close();
        }


        return null;
    }
}
