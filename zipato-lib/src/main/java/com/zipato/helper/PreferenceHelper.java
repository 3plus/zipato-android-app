/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.zipato.translation.LanguageManager;
import com.zipato.v2.client.ApiV2RestTemplate;

import dagger.Lazy;

/**
 * Created by murielK on 30.5.2014..
 */
public class PreferenceHelper {

    public static final String PREF_NAME = PreferenceHelper.class.getPackage().getName();
    private final SharedPreferences sharedPreferences;

    private final Lazy<LanguageManager> languageManagerLazy;
    private final Lazy<ApiV2RestTemplate> restTemplateLazy;

    public PreferenceHelper(Context context, Lazy<LanguageManager> languageManagerLazy, Lazy<ApiV2RestTemplate> restTemplateLazy) {
        this.languageManagerLazy = languageManagerLazy;
        this.restTemplateLazy = restTemplateLazy;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getStringPref(Preference key, String defaultValue) {
        return sharedPreferences.getString(key.name(), defaultValue);
    }

    public Boolean getBooleanPref(Preference key) {
        return sharedPreferences.getBoolean(key.name(), false);
    }

    public void putStringPref(String key, String value) {
        final Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putStringPref(Preference key, String value) {
        final Editor editor = sharedPreferences.edit();
        editor.putString(key.name(), value);
        editor.apply();
        switch (key) {
            case USERNAME:
            case PASSWORD:
                break;
            case SERVER_URL:
                restTemplateLazy.get().setRemoteUrl(value);
                //restTemplateLazy.get().invalidate();
                break;
            case LANGUAGE:
                restTemplateLazy.get().setLocal(value);
                languageManagerLazy.get().setLanguageCode(value);
                break;
        }
    }

    public void putBooleanPref(Preference key, boolean value) {
        final Editor editor = sharedPreferences.edit();
        editor.putBoolean(key.name(), value);
        editor.apply();
    }

    public void resetRepoSync() {
        putBooleanPref(Preference.REPO_SYNC, false);
    }

    public void setRepoSync() {
        putBooleanPref(Preference.REPO_SYNC, true);
        putLongPref(Preference.SAVE_DATA, System.currentTimeMillis());
    }

    public void clearCredentials() {
        Log.d("PreferenceManager", "Clearing credentials");
        clearPreference(Preference.USERNAME, Preference.PASSWORD, Preference.USER_ID);
    }

    public void storeCredentials(String userName, String password) {
        putStringPref(Preference.USERNAME, userName);
        putStringPref(Preference.PASSWORD, password);
    }

    public String getBaseUrl() {
        return restTemplateLazy.get().getRemoteUrl();
    }

    public int getInt(Preference key, int defaultValue) {
        return sharedPreferences.getInt(key.name(), defaultValue);
    }

    public void putInt(Preference key, int value) {
        final Editor editor = sharedPreferences.edit();
        editor.putInt(key.name(), value);
        editor.apply();
    }

    public void putLongPref(Preference key, long value) {
        final Editor editor = sharedPreferences.edit();
        editor.putLong(key.name(), value);
        editor.apply();
    }

    public Long getLongPref(Preference key) {
        return sharedPreferences.getLong(key.name(), 0);
    }

    public boolean isRepoLoaded() {
        return getBooleanPref(Preference.REPO_SYNC);
    }

    public void clearPreference(Preference... preference) {
        final Editor editor = sharedPreferences.edit();
        for (Preference pref : preference) {
            editor.remove(pref.name());
        }

        editor.apply();
    }


    public enum Preference {
        USERNAME, PASSWORD, SERVER_URL,
        LANGUAGE, LOCAL_CONNECTION, REPO_SYNC,
        SAVE_DATA, PROPERTY_REG_ID, PROPERTY_APP_VERSION,
        SHAKE_FORCE, SHAKE_TIME_OUT, SHAKE_ENABLE, IS_LOG_IN,
        REFRESH_ON_RESUME, BOX_SERIAL, URI, USER_ID, GCM_REGISTERED
    }

}
