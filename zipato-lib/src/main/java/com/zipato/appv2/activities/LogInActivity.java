/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.annotation.TranslatedHint;
import com.zipato.appv2.B;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.bool;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.R.string;
import com.zipato.appv2.interactor.LoginIteractor;
import com.zipato.appv2.services.ZipaGcmListenerService;
import com.zipato.discovery.DiscoveryManager;
import com.zipato.discovery.ServiceInfoAdapter;
import com.zipato.discovery.ZipatoServiceInfo;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectBonjour;
import com.zipato.util.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import butterfork.OnClick;
import butterfork.OnItemClick;

public class LogInActivity extends BaseActivity {
    public static final String LOGOUT_ACTION = "LOG_OUT_ACTION";
    public static final String KICK_LOGIN_ACTION = "KICK_LOGIN_ACTION";
    private static final String TAG = LogInActivity.class.getSimpleName();
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("register")
    @Bind(B.id.textViewRegister)
    TextView register;
    @Bind(B.id.listViewLogInPane)
    ListView serviceInfoListView;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonConnect)
    Button connectButton;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("email_username")
    @Bind(B.id.editTextUserName)
    EditText userName;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("LoginScreenPasswordLabel")
    @Bind(B.id.editTextPassword)
    EditText password;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewSNValue)
    TextView textViewSNValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewMacValue)
    TextView textViewMacValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewIPValue)
    TextView textViewIpValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewZipaBox)
    TextView textViewZipaBox;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("remote_connection")
    @Bind(B.id.textViewConnectionType)
    TextView textViewConnectionType;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("login_pane_title")
    @Bind(B.id.textViewPaneTitle)
    TextView textViewRemoteConnection;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("forgot_password")
    @Bind(B.id.textViewForgotPassword)
    TextView textViewForgetPassword;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewLogInTextMessage)
    TextView textViewLogInTextMessage;

    @Bind(B.id.imageButtonLogToSlideUp)
    ImageView imageViewButtonForPane; //

    @Bind(B.id.login_sliding_panel)
    SlidingUpPanelLayout slidingUpPanelLayout;
    @Inject
    DiscoveryManager discoveryManager;

    @Bind(B.id.layout_bottom)
    LinearLayout linearLayout;

    private List<ZipatoServiceInfo> zipatoServiceInfoList;
    private ServiceInfoAdapter serviceInfoAdapter;
    private String userNameString;
    private String passwordString;
    private String connectionType;
    private String macValue;
    private String ipValue;
    private String zipaBox;
    private String sNValue;

    private LoginIteractor loginInteractor;

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {
        loginInteractor = new LoginIteractor(this);
    }

    @Override
    protected int getContentViewID() {
        final int i = getResources().getConfiguration().orientation;
        if (i == Configuration.ORIENTATION_LANDSCAPE) {
            return R.layout.activity_start_land;
        } else {
            return R.layout.activity_start;
        }
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterFork.bind(this);

        connectButton.setText(Utils.capitalizer(languageManager.translate("LoginScreenButtonConnect").toLowerCase()));

        final Intent intent = getIntent();
        userNameString = preferenceHelper.getStringPref(Preference.USERNAME, null);
        passwordString = preferenceHelper.getStringPref(Preference.PASSWORD, null);
        if ((intent != null) && (intent.getAction() != null)) {
            if (intent.getAction().equals(ZipaGcmListenerService.GCM_ACTION)) {
                if ((userNameString != null) && (passwordString != null)) {
                    startBrowserManager(BrowserManagerActivity.RESUMING_ACTION);
                }
            }
        } else {
            if ((userNameString != null) && (passwordString != null)) {
                startBrowserManager(null);
            }
        }

        password.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    handleOnclickLogin();
                    return true;
                }
                return false;
            }
        });

        restTemplate.setUseLocal(false);

        if (!getResources().getBoolean(bool.display_layout_bottom)) {
            linearLayout.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setLogInTextMsg() {
        final String text = languageManager.translate("log_int_text_message");
        final String appName = getResources().getString(string.app_nameMain);
        textViewLogInTextMessage.setText(text.replace("{productName}", appName));// TODO how can i actually get that string name in BOLD? tried  <b></b> didn't worked even with Html.fromHtml();
    }

    @Override
    protected boolean provideMenu() {
        return getResources().getBoolean(bool.menu_on_log_in);
    }

    public void startBrowserManager(String action) {
        Intent i = new Intent(this, BrowserManagerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (action != null)
            i.setAction(action);
        startActivity(i);
        finish();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        zipatoServiceInfoList = new ArrayList<ZipatoServiceInfo>();
        serviceInfoAdapter = new ServiceInfoAdapter(this, layout.row_login_pane, zipatoServiceInfoList);
        serviceInfoListView.setAdapter(serviceInfoAdapter);
        userNameString = preferenceHelper.getStringPref(Preference.USERNAME, null);
        passwordString = preferenceHelper.getStringPref(Preference.PASSWORD, null);
        if ((userNameString != null) && (passwordString != null)) {
            userName.setText(userNameString);
            password.setText(passwordString);
        }

        setLogInTextMsg();
        slidingUpPanelLayout.setTouchEnabled(true);
        slidingUpPanelLayout.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelCollapsed(View view) {
//                setContentView(R.layout.activity_start);
//                ButterFork.bind(LogInActivity.this);
            }

            @Override
            public void onPanelExpanded(View view) {
                discoveryManager.stop();
                discoveryManager.start(LogInActivity.this);
            }

            @Override
            public void onPanelAnchored(View view) {

            }

            @Override
            public void onPanelHidden(View view) {

            }
        });

    }


    private void setServiceInfoValues(ZipatoServiceInfo zipatoServiceInfo) {
        String temp;
        try {
            temp = zipatoServiceInfo.getName();
            if ("UNKNOWN".equalsIgnoreCase(temp))
                textViewZipaBox.setText(getResources().getString(string.reg_box));
            else
                textViewZipaBox.setText(temp);
        } catch (NullPointerException e) {
            textViewZipaBox.setText(getResources().getString(string.reg_box));
            logE("ZipaboxName " + "null");
        }
        try {
            temp = zipatoServiceInfo.getSerial();
            textViewSNValue.setText("SN " + temp);
        } catch (NullPointerException e) {
            logE("SNValue " + "null");
            textViewSNValue.setText("-");
        }
        try {
            temp = zipatoServiceInfo.getIp();
            textViewIpValue.setText("IP " + temp);
        } catch (NullPointerException e) {
            logE("IPValue " + "null");
            textViewIpValue.setText("-");
        }
        try {
            temp = zipatoServiceInfo.getMac();
            textViewMacValue.setText("MAC " + temp);
        } catch (NullPointerException e) {
            logE("MacValue " + "null");
            textViewMacValue.setText("-");
        }
        textViewConnectionType.setText(languageManager.translate("local_connection"));
        connectionType = textViewConnectionType.getText().toString();
        macValue = textViewMacValue.getText().toString();
        ipValue = textViewIpValue.getText().toString();
        zipaBox = textViewZipaBox.getText().toString();
        sNValue = textViewSNValue.getText().toString();
    }

    public void resetTextView() {
        textViewConnectionType.setText(languageManager.translate("remote_connection"));
        textViewMacValue.setText("");
        textViewIpValue.setText("");
        textViewZipaBox.setText("");
        textViewSNValue.setText("");
    }

    @Override
    public void onPause() {
        discoveryManager.stop();
        super.onPause();


    }

    @Override
    public void onResume() {
        if (!discoveryManager.isListening()) {
            discoveryManager.start(this);
        }
        super.onResume();
    }

    @OnClick(B.id.buttonConnect)
    public void connectClicked() {
        handleOnclickLogin();
    }

    private void handleOnclickLogin() {
        if (!internetConnectionHelper.isOnline()) {
            showAlertDialog(languageManager.translate("no_internet_connection"), languageManager.translate("no_internet_connection_message"), false);
            return;
        }

        final String userNameInput = userName.getText().toString();
        final String passwordInput = password.getText().toString();

        if (!loginInteractor.isLogging())
            loginInteractor.login(userNameInput, passwordInput);
    }

    @OnClick(B.id.imageButtonLogToSlideUp)
    public void imageButtonClicked() {
        slidingUpPanelLayout.setPanelState(PanelState.EXPANDED);
    }

    @OnClick(B.id.textViewRegister)
    public void registerClicked() {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    @OnClick(B.id.textViewForgotPassword)
    public void forgotPasswordClicked() {
        startActivity(new Intent(this, PasswordRecoveryActivity.class));
    }

    @OnClick(B.id.textViewPaneTitle)
    public void textViewPaneClicked() {
        preferenceHelper.putBooleanPref(Preference.LOCAL_CONNECTION, false);
        resetTextView();
        restTemplate.setUseLocal(false);
        slidingUpPanelLayout.setPanelState(PanelState.COLLAPSED);

    }

    @OnItemClick(B.id.listViewLogInPane)
    public void onItemClick(int position) {
        setServiceInfoValues(zipatoServiceInfoList.get(position));
        restTemplate.setUseLocal(true);
        restTemplate.setLocalUrl(zipatoServiceInfoList.get(position).address());
        slidingUpPanelLayout.setPanelState(PanelState.COLLAPSED);
    }


    public void onEventMainThread(Event event) {

        final ObjectBonjour bonjourEvent = (ObjectBonjour) event.eventObject;
        final ZipatoServiceInfo info = (ZipatoServiceInfo) bonjourEvent.info;
        switch (bonjourEvent.eventType) {
            case ObjectBonjour.ADD:
                if (zipatoServiceInfoList.contains(info) && (info.getName() != null)) {
                    zipatoServiceInfoList.remove(info);
                    zipatoServiceInfoList.add(info);
                } else if (!zipatoServiceInfoList.contains(info))
                    zipatoServiceInfoList.add(info);
                serviceInfoAdapter.notifyDataSetChanged();
                break;
            case ObjectBonjour.REMOVE:
                if (zipatoServiceInfoList.contains(info)) {
                    zipatoServiceInfoList.remove(info);
                    serviceInfoAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void restoreText() {
        languageManager.translateFields(this);
        setLogInTextMsg();
        if (restTemplate.isUseLocal()) {
            try {
                textViewConnectionType.setText(connectionType);
                textViewMacValue.setText(macValue);
                textViewIpValue.setText(ipValue);
                textViewZipaBox.setText(zipaBox);
                textViewSNValue.setText(sNValue);
            } catch (Exception e) {

            }
        }
        serviceInfoListView.setAdapter(serviceInfoAdapter);
        if (!getResources().getBoolean(bool.display_layout_bottom)) {
            linearLayout.setVisibility(View.GONE);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(layout.activity_start);

        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            setContentView(layout.activity_start_land);
        }

        ButterFork.bind(this);
        restoreText();
    }

}
