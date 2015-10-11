/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnCloseListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.anim;
import com.zipato.appv2.R.bool;
import com.zipato.appv2.R.color;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.string;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.settings.BoxInfoFragment;
import com.zipato.appv2.ui.fragments.settings.SettingMenuFragment;
import com.zipato.appv2.ui.fragments.settings.SubSettingsFragment;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.helper.PopUpMessageHelper;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectConnectivity;
import com.zipato.model.event.ObjectLauncher;
import com.zipato.model.event.ObjectMenu;
import com.zipato.translation.LanguageManager;
import com.zipato.util.ShakeUtils;
import com.zipato.util.ShakeUtils.OnShakeListener;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.APIV2RestCallback;
import com.zipato.v2.client.ApiV2RestTemplate;
import com.zipato.v2.client.RestObjectClientException;
import com.zipato.v2.client.RestObjectException;

import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

//import com.google.android.gms.common.AccountPicker;

/**
 * Created by murielK on 4.6.2014..
 */
public abstract class BaseActivity extends AppCompatActivity implements OnShakeListener, OnClosedListener, OnCloseListener,
        OnOpenedListener, SlidingMenu.OnOpenListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Inject
    protected ShakeUtils shakeUtils;
    @Inject
    protected ApiV2RestTemplate restTemplate;
    @Inject
    protected PreferenceHelper preferenceHelper;
    protected Vibrator vib;
    protected SlidingMenu slidingMenu;
    @Inject
    LanguageManager languageManager;
    @Inject
    AttributeValueRepository attributeValueRepository;
    @Inject
    DeviceStateRepository deviceStateRepository;
    @Inject
    InternetConnectionHelper internetConnectionHelper;
    @Inject
    EventBus eventBus;
    @Inject
    ExecutorService executor;
    @Inject
    TypeFaceUtils typeFaceUtils;
    private PopUpMessageHelper popUpMessageHelper;
    private ProgressDialog progressDialog;
    private boolean isDestroyed;
    private Context context;
    private ProgressBar progressBar;

    /**
     * @return Application's version code from the {@code PackageManager}.
     */

    protected abstract boolean provideMenu();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ZipatoApplication) getApplication()).inject(this);

        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        context = this;

        onPreContentView(savedInstanceState);
        setStatusBarTint();
        setContentView(getContentViewID());
        setToolbar();
        setBehindView(savedInstanceState);
        onPostContentView(savedInstanceState);

        typeFaceUtils.applyTypefaceFor(this);
        languageManager.translateFields(this);

        vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        popUpMessageHelper = new PopUpMessageHelper(this);
        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);

    }

    protected abstract void onPreContentView(Bundle savedInstanceState);

    protected abstract int getContentViewID();

    protected abstract void onPostContentView(Bundle savedInstanceState);

    @Override
    public void onClosed() {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onOpened() {

    }

    @Override
    public void onShake() {

    }

    private void setUpShakeUtils() {
        shakeUtils.setOnShakeListener(this);
        shakeUtils.registerToSensor();
        shakeUtils.setForceTimeOut(preferenceHelper.getInt(Preference.SHAKE_FORCE, 500),
                preferenceHelper.getInt(Preference.SHAKE_TIME_OUT, 500));
        shakeUtils.setEnableShake(preferenceHelper.getBooleanPref(Preference.SHAKE_ENABLE));
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void setSlidingEnabled(boolean sliding) {
        slidingMenu.setSlidingEnabled(sliding);
    }

//    public void setSlidingMenuBehindViewoffset(int offset){
//        slidingMenu.setBehindOffset(offset);
//    }

    protected void setBehindView(Bundle savedInstanceState) {
        if (!provideMenu()) {
            return;
        }
        slidingMenu = new SlidingMenu(this);
        slidingMenu.setMenu(R.layout.menu_right);
        slidingMenu.setMode(SlidingMenu.RIGHT);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow_slider_activity);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        slidingMenu.setOnClosedListener(this);
        slidingMenu.setOnCloseListener(this);
        slidingMenu.setOnOpenedListener(this);
        slidingMenu.setOnOpenListener(this);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        if (savedInstanceState == null) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            Fragment fragment = new SettingMenuFragment();
            t.replace(id.menu_frame, fragment);
            t.commit();
        } else {
            // fragment = (BoxInfoFragment) getSupportFragmentManager().findFragmentById(R.id.menu_frame);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch (Exception e) {
            Log.e(TAG, "onDestroy crashed",e);
        }
        isDestroyed = true;
    }

    public void showProgressDialog(String title, String message, boolean cancelable) {
        progressDialog.setTitle(title);
        showProgressDialog(message, cancelable);
    }

    public void showProgressDialog(String message, boolean cancelable) {
        progressDialog.setCancelable(cancelable);
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (((progressDialog != null) & !isDestroyed) && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void toast(CharSequence message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    protected void logD(String message, Throwable throwable) {
        Log.d(getClassName(), message, throwable);
    }

    protected void logE(String message, Throwable throwable) {

        Log.e(getClassName(), message, throwable);
    }

    @Deprecated
    protected void logD(String message) {
        Log.d(getClassName(), message);
    }

    @Deprecated
    protected void logE(String message) {
        Log.e(getClassName(), message);
    }

    private String getClassName() {
        return context.getClass().getSimpleName();
    }

    public void showAlertDialog(String title, String message, Boolean status) {
        Builder builder = new Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);

        if (status != null) {
            builder.setIcon((status) ? drawable.success : drawable.fail);
        }

        builder.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog callDialogFinal = builder.create();
        callDialogFinal.show();
        // Showing Alert Message
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
//        if ((slidingMenu != null) && !slidingMenu.isMenuShowing()) {
//            slidingMenu.setSlidingEnabled(false);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.menu_menu) {
            if (slidingMenu != null) {
                slidingMenu.toggle();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        eventBus.register(this);
        setUpShakeUtils();
        if (!(this instanceof LogInActivity))
            restTemplate.setCallback(new MyAPIV2RestCallback(this));
        if ((slidingMenu != null) && slidingMenu.isMenuShowing()) slidingMenu.toggle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
        shakeUtils.unRegisterToSensor();
        restTemplate.setCallback(new MyAPIV2RestCallback(this));
    }


    protected void logout(final String action) {
        if (action.equals(LogInActivity.LOGOUT_ACTION)) {

            preferenceHelper.clearCredentials();

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        restTemplate.logout();
                    } catch (Exception e) {
                        Log.d(TAG, "", e);
                    }
                }

            });
        }

        attributeValueRepository.clearETag();
        deviceStateRepository.clearETag();

        preferenceHelper.resetRepoSync();

        Intent intent = new Intent(this, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK)
                .setAction(action);
        startActivity(intent);
    }

    protected void setStatusBarTint() {
        final int color = getResources().getColor(R.color.color_view_controller_item_background);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(color);
            getWindow().setNavigationBarColor(color);
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(color);
            tintManager.setNavigationBarTintEnabled(true);
            tintManager.setNavigationBarTintColor(color);
        }
    }

    private void setToolbar() {
        progressBar = (ProgressBar) findViewById(id.progress_spinner);
        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
        if (toolbar == null)
            return;
        toolbar.setBackgroundColor(getResources().getColor(color.toolbar_color));
        //toolbar.setTitle(getResources().getString(string.app_nameMain));
        toolbar.setTitle("");
        toolbar.setTitleTextColor(getResources().getColor(color.color_white));
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
    }

    public void showIndeterminateProgress(boolean show) {
        if (progressBar == null)
            return;
        if (show) progressBar.setVisibility(View.VISIBLE);
        else progressBar.setVisibility(View.INVISIBLE);
    }

    protected void setInsets(View view) {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        SystemBarConfig config = tintManager.getConfig();
        view.setPadding(0, config.getPixelInsetTop(true), config.getPixelInsetRight(), config.getPixelInsetBottom());
    }

    private boolean checkPlayServices() {

        if (!getResources().getBoolean(bool.push_enable)) {
            Log.d("BaseActivity", "Push notification is disable in this Build. ");
            return false;
        }

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(TAG, "This device is not supported.");
                toast("This device is not supported : Google Play Service missing!");
                finish();
            }

            return false;
        }
        return true;
    }

    public void handlerException(final Exception e, final String tag) {

        if (!internetConnectionHelper.isOnline() && !restTemplate.isUseLocal())
            try {
                popUpMessageHelper.show(drawable.ic_err_connection, languageManager.translate("no_internet_connection"));
            } catch (Exception g) {
                //Empty
            }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (((e instanceof RestObjectClientException) || (e instanceof ResourceAccessException))) {

                    if ((e instanceof RestObjectClientException) && (((RestObjectException) e).getResponseBody() != null) && (((RestObjectException) e).getResponseBody().getError() != null)) {
                        if (restTemplate.isUseLocal()) {
                            toast(languageManager.translate("local_error"));
                        } else {
                            toast(languageManager.translate("connection_error"));
                        }
                        Log.e(tag, ((RestObjectException) e).getResponseBody().getError(), e);

                    } else {
                        if (restTemplate.isUseLocal())
                            toast(languageManager.translate("connection_error_local").replace("{productName}", getResources().getString(string.reg_box)));
                        else
                            toast(languageManager.translate("connection_error"));
                        Log.e(tag, "", e);
                    }

                } else {
                    toast(languageManager.translate("something_when_wrong"));
                    Log.e(tag, "", e);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void handlerConEvent(ObjectConnectivity event) {
        if (!event.isOnline && !restTemplate.isUseLocal()) {
            popUpMessageHelper.show(drawable.ic_err_connection, languageManager.translate("no_internet_connection"));
        } else if ((event.object != null)) {
            popUpMessageHelper.show(drawable.ic_err_connection, event.object.toString());
        } else {
            popUpMessageHelper.hide();
        }

    }

    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_CONNECTIVITY_EVENT:
                final ObjectConnectivity objectConnectivity = (ObjectConnectivity) event.eventObject;
                handlerConEvent(objectConnectivity);
                break;
            case Event.EVENT_TYPE_TYPE_MENU:
                final ObjectMenu objectMenu = (ObjectMenu) event.eventObject;
                handleMenuEvent(objectMenu);
                break;
        }
    }

    protected void handleLauncherEvent(ObjectLauncher objectLauncher) {
        switch (objectLauncher.launchType) {
            case ObjectLauncher.LAUNCH_LOG_OUT:
                logout(LogInActivity.LOGOUT_ACTION);
                break;
            case ObjectLauncher.LAUNCH_SCENES:
                triggerScenes();
        }
    }

    private void handleMenuEvent(ObjectMenu objectMenu) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (objectMenu.menuType) {
            case ObjectMenu.MENU_MAIN:
                SettingMenuFragment settingMenuFragment = new SettingMenuFragment();
                fragmentManager.beginTransaction().setCustomAnimations(anim.enter_main, anim.exit_main, anim.pop_enter, anim.pop_exit).replace(id.menu_frame, settingMenuFragment).commit();
                break;
            case ObjectMenu.MENU_BOX_INFO:
                try {
                    fragmentManager.beginTransaction().setCustomAnimations(anim.enter, anim.exit, anim.pop_enter, anim.pop_exit).replace(id.menu_frame, BoxInfoFragment.newInstance((Parcelable) objectMenu.object)).commit();
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
                break;
            case ObjectMenu.MENU_SETTINGS:
                fragmentManager.beginTransaction().setCustomAnimations(anim.enter, anim.exit, anim.pop_enter, anim.pop_exit).replace(id.menu_frame, new SubSettingsFragment()).commit();
                break;
        }

    }

    public SlidingMenu getSlidingMenu() {
        return slidingMenu;
    }

    protected void triggerScenes() {
        Intent intent = new Intent(this, BrowserManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(BrowserManagerActivity.ACTION_SCENES);
        startActivity(intent);
    }

    public boolean checkInternet() {
        if (!internetConnectionHelper.isOnline()) {
            toast(languageManager.translate("no_internet_connection"));

            return false;
        }
        return true;
    }

    public void vibrate(long duration) {
        vib.vibrate(duration);
    }

    private static class MyAPIV2RestCallback implements APIV2RestCallback {
        BaseActivity activity;

        private MyAPIV2RestCallback(BaseActivity activity) {
            this.activity = activity;
        }

        @Override
        public void loginSuccessful() {
        }

        @Override
        public void loginFailed(final String error) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error != null) {
                        activity.toast(error);
                    }
                    activity.logout(LogInActivity.KICK_LOGIN_ACTION);
                }
            });

        }

        @Override
        public void onGCMRegistered() {
            Log.d(TAG, "onGCMRegistered setting GCM_REGISTERED pref to true");

            activity.preferenceHelper.putBooleanPref(Preference.GCM_REGISTERED, true);
        }

        @Override
        public void onGCMUnregistered() {
            Log.d(TAG, "onGCMUnregistered setting GCM_REGISTERED pref to false");

            activity.preferenceHelper.putBooleanPref(Preference.GCM_REGISTERED, false);
        }
    }


}
