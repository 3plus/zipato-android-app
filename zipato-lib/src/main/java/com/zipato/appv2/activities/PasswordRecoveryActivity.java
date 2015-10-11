/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.annotation.TranslatedHint;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.bool;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.string;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.client.RestObject;
import com.zipato.util.Utils;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import butterfork.OnClick;

public class PasswordRecoveryActivity extends BaseActivity {

    private static final String RECOVERY_BOOLEAN_KEY = "RECOVERY_BOOLEAN_KEY";
    private static final String EMAIL_KEY_RECOVERED = "EMAIL_KEY_RECOVERED";
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonRecovery)
    Button buttonRecovery;
    @Bind(B.id.linearLayoutRecoveryText)
    LinearLayout linearLayoutRecoveryText;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("email_address")
    @Bind(B.id.editTextEmail)
    EditText editTextEmail;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("recovery_text1")
    @Bind(B.id.textViewRecovery1)
    TextView textViewRecovery1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("recovery_text2")
    @Bind(B.id.textViewRecovery2)
    TextView textViewRecovery2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("thank_you")
    @Bind(B.id.textViewRecoveryThank_1)
    TextView textViewRecoveryThankYou;
    @Inject
    InternetConnectionHelper internetConnectionHelper;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewLogInTextMessage)
    TextView textViewLogInTextMessage;
    private boolean recovered; //TODO this will be use for onConfiguration change to set properly the View
    private String email;
    private Handler handler;

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.recovery_password_layout;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterFork.bind(this);
        buttonRecovery.setText(Utils.capitalizer(languageManager.translate("recovery_password").toLowerCase()));
        handler = new Handler();
    }

    @Override
    protected boolean provideMenu() {
        return getResources().getBoolean(bool.menu_on_pasword_rec);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (recovered) {
            recovered();
            if (savedInstanceState != null) {
                recovered = savedInstanceState.getBoolean(RECOVERY_BOOLEAN_KEY);
                email = savedInstanceState.getString(EMAIL_KEY_RECOVERED);
            }
        }
        setLogInTextMsg();
    }

    private void setLogInTextMsg() {
        final String text = languageManager.translate("log_int_text_message");
        final String appName = getResources().getString(string.app_nameMain);
        textViewLogInTextMessage.setText(text.replace("{productName}", appName));// TODO how can i actually get that string name in BOLD? tried  <b></b> didn't worked even with Html.fromHtml();
    }

    @Override
    public void onSaveInstanceState(Bundle saveStates) {
        saveStates.putBoolean(RECOVERY_BOOLEAN_KEY, recovered);
        saveStates.putString(EMAIL_KEY_RECOVERED, email);
    }

    @OnClick(B.id.buttonRecovery)
    public void onClick() {
        if (!internetConnectionHelper.isOnline()) {
            showAlertDialog(languageManager.translate("no_internet_connection"), languageManager.translate("no_internet_connection_message"), false);
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgressDialog(languageManager.translate("processing"), false);
                    }
                });
                recovery();
            }
        });
    }

    boolean isEmailValid(CharSequence email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String getFailMessage(String messageIn) {
        return (languageManager.translate("login_fail") + ": " + messageIn);
    }

    private void recovery() {
        try {
            CharSequence emailChar = editTextEmail.getText().toString();
            if (!isEmailValid(emailChar)) {
                onFailure(getFailMessage(languageManager.translate("incorrect_email")));
                return;
            }
            email = emailChar.toString();
            RestObject restObject = null;

            try {
                restObject = restTemplate.recovery(email);
            } catch (Exception e) {
                handlerException(e, "PassActivity");
            }

            if (restObject != null) {

                if (restObject.isSuccess()) {
                    onSuccess();
                    recovered = true;
                    return;
                }
            }
            onFailure(getFailMessage(""));

        } catch (NullPointerException e) {
            onFailure(getFailMessage(languageManager.translate("no_null_input")));
        }
    }

    public void onSuccess() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                recovered();
            }
        });
    }

    public void onFailure(final String input) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                toast(input);
                dismissProgressDialog();
            }
        });
    }

    public void recovered() {
        linearLayoutRecoveryText.setVisibility(View.VISIBLE);
        editTextEmail.setText(email);
        buttonRecovery.setText(languageManager.translate("success"));
        buttonRecovery.setEnabled(false);
    }
}
