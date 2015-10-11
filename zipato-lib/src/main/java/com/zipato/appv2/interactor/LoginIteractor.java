/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.interactor;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.activities.LogInActivity;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.helper.PreferenceHelper;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.user.User;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

/**
 * Created by murielK on 10/6/2015.
 */
public class LoginIteractor {

    private static final String TAG = TagFactoryUtils.getTag(LoginIteractor.class);
    private final WeakReference<LogInActivity> weakReference;
    @Inject
    InternetConnectionHelper internetConnectionHelper;
    @Inject
    AttributeValueRepository attributeValueRepository;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    ExecutorService executor;
    @Inject
    LanguageManager languageManager;
    @Inject
    PreferenceHelper preferenceHelper;
    private boolean isLogging;

    public LoginIteractor(LogInActivity logInActivity) {
        weakReference = new WeakReference<>(logInActivity);
        ((ZipatoApplication) logInActivity.getApplication()).inject(this);
    }

    public boolean isLogging() {
        return isLogging;
    }

    private LogInActivity getActivity() {
        return weakReference.get();
    }

    public void login(final String userNameInput, final String passwordInput) {

        final LogInActivity logInActivity = getActivity();
        if (logInActivity == null)
            return;

        isLogging = true;

        logInActivity.showProgressDialog(languageManager.translate("login"), languageManager.translate("log_in_dialog_message"), false);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                final String message = performLogin(userNameInput, passwordInput);
                logInActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isLogging = false;
                        if (message == null) {
                            logInActivity.startBrowserManager(null);
                            logInActivity.finish();
                        } else {
                            if (!"dummy".equals(message))
                                logInActivity.toast(languageManager.translate(message));
                            else logInActivity.toast(languageManager.translate("login_fail"));
                        }
                        logInActivity.dismissProgressDialog();
                    }

                });
            }
        });
    }

    private String performLogin(final String username, final String password) {
        final LogInActivity logInActivity = getActivity();
        if (logInActivity == null)
            return "dummy";

        String error;

        try {

            error = restTemplate.login(username, password, null);
            if (error == null) {
                if (logInActivity.getResources().getBoolean(R.bool.push_enable)) { // register to push in case it is allowed
                    Log.d(TAG, "trying to register to push Notification services");

                    if (!restTemplate.isUseLocal() && isGcmOK()) {

                        Log.d(TAG, "GCM is Ok sending registration");
                        restTemplate.registerGCM(getRegistrationId());
                        Log.d(TAG, "Push notification reg success");

                    } else if (restTemplate.isUseLocal()) {

                        try { // try to unregister from push notifications

                            restTemplate.unRegisterGCM(getRegistrationId());

                        } catch (Exception e) {
                            Log.d(TAG, "", e);
                        }

                    } else
                        return "GCM ERROR";

                } else {

                    Log.e(TAG, "Push notification not allowed in this BUILD. No registration request sent");
                }

                preferenceHelper.storeCredentials(username, password);

                if (!restTemplate.isUseLocal())
                    persistUserID();
                else preferenceHelper.clearPreference(PreferenceHelper.Preference.USER_ID);
            }

        } catch (Exception e) {
            Log.d(TAG, "", e);
            restTemplate.invalidate();
            preferenceHelper.clearCredentials();
            error = "dummy";
        }
        return error;
    }

    private void persistUserID() {
        User currentUser = restTemplate.getUser();
        preferenceHelper.putInt(PreferenceHelper.Preference.USER_ID, currentUser.getId());
    }

    private String getRegistrationId() {
        String registrationId = preferenceHelper.getStringPref(PreferenceHelper.Preference.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = preferenceHelper.getInt(PreferenceHelper.Preference.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        Log.i(TAG, "Registration  found.");
        return registrationId;
    }

    private void storeRegistrationId(String regId) {
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        preferenceHelper.putStringPref(PreferenceHelper.Preference.PROPERTY_REG_ID, regId);
        preferenceHelper.putInt(PreferenceHelper.Preference.PROPERTY_APP_VERSION, appVersion);
    }

    private int getAppVersion() {
        try {
            final LogInActivity logInActivity = getActivity();
            if (logInActivity == null)
                return 0;

            PackageInfo packageInfo = logInActivity.getPackageManager()
                    .getPackageInfo(logInActivity.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean isGcmOK() {
        final LogInActivity logInActivity = getActivity();
        if (logInActivity == null)
            return false;

        String regId = getRegistrationId();
        Log.d(TAG, "regId: " + regId);
        if (regId.isEmpty()) {
            String msg = "";
            try {
                GoogleCloudMessaging.getInstance(logInActivity);
                final InstanceID instanceID = InstanceID.getInstance(logInActivity);
                regId = instanceID.getToken(logInActivity.getResources().getString(R.string.sender_ID), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                msg = "Device registered, registration ID=" + regId;
                Log.d(TAG, msg);
                storeRegistrationId(regId);
            } catch (Exception e) {
                Log.d(TAG, msg, e);
                return false;
            }
        }
        return true;
    }


}
