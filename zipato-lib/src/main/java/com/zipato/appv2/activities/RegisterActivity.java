/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreabaccega.widget.FormEditText;
import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.annotation.TranslatedHint;
import com.zipato.appv2.B;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.anim;
import com.zipato.appv2.R.bool;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.string;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.client.RestObject;
import com.zipato.model.user.User;
import com.zipato.util.Utils;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import butterfork.OnClick;
import butterfork.OnFocusChange;

public class RegisterActivity extends BaseActivity {

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("first_name")
    @Bind(B.id.editTextFirstName)
    FormEditText firstNameEditText;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("last_name")
    @Bind(B.id.editTextLastName)
    FormEditText lastNameEditText;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("email_address")
    @Bind(B.id.editTextEmail)
    FormEditText emailEditText;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @TranslatedHint("LoginScreenPasswordLabel")
    @Bind(B.id.editTextPassword)
    FormEditText passwordEditText;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonRegister)
    Button registerButton;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("reg_success_text1")
    @Bind(B.id.textViewRegSuccessText1)
    TextView textViewRegSuccessTest1;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("thank_you")
    @Bind(B.id.textViewRegSuccessThank)
    TextView textViewRegThank;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("reg_success_text2")
    @Bind(B.id.textViewRegSuccessText2)
    TextView textViewRegSuccessTest2;

    @Bind(B.id.reg_success_layout)
    LinearLayout regSuccessLayout;

    @Bind(B.id.register_layout)
    LinearLayout layout;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewLogInTextMessage)
    TextView textViewLogInTextMessage;

    @Inject
    InternetConnectionHelper internetConnectionHelper;
    Button button;
    private Animation animationFadeIn;
    private Animation animationFadeOut;
    private Handler handler;

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.register_layout_final;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterFork.bind(this);
        registerButton.setText(Utils.capitalizer(languageManager.translate("register").toLowerCase()));
        handler = new Handler();
    }

    @Override
    protected boolean provideMenu() {
        return getResources().getBoolean(bool.menu_on_reg);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        regSuccessLayout.setVisibility(View.GONE);
        setLogInTextMsg();
    }

    private void setLogInTextMsg() {
        final String text = languageManager.translate("log_int_text_message");
        final String appName = getResources().getString(string.app_nameMain);
        textViewLogInTextMessage.setText(text.replace("{productName}", appName));// TODO how can i actually get that string name in BOLD? tried  <b></b> didn't worked even with Html.fromHtml();
    }

    @Override
    protected void onResume() {
        super.onResume();
        languageManager.translateFields(this);
    }

    @OnClick(B.id.buttonRegister)
    public void onRegisterButton() {
        if (!checkAllValid()) {
            toast(getResources().getString(string.fill_field_properly));
            return;
        }
        if (!internetConnectionHelper.isOnline()) {
            showAlertDialog(languageManager.translate("fill_field_properly"), languageManager.translate("fill_field_properly"), false);
            return;
        }
        showProgressDialog(languageManager.translate("registering"), false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                register();
            }
        });
    }

    @OnFocusChange(B.id.editTextPassword)
    public void onFocusPass(boolean focused) {
        if (!focused)
            passwordEditText.testValidity();
    }

    @OnFocusChange(B.id.editTextFirstName)
    public void onFocusFirst(boolean focused) {
        if (!focused)
            firstNameEditText.testValidity();
    }

    @OnFocusChange(B.id.editTextLastName)
    public void onFocusLast(boolean focused) {
        if (!focused)
            lastNameEditText.testValidity();
    }

    @OnFocusChange(B.id.editTextEmail)
    public void onFocusEmail(boolean focused) {
        if (!focused)
            emailEditText.testValidity();
    }

    public boolean checkAllValid() {
        FormEditText[] allFields = {firstNameEditText, lastNameEditText, emailEditText, passwordEditText};
        boolean allValid = true;
        for (FormEditText field : allFields) {
            allValid &= field.testValidity();
        }
        return allValid;
    }

    public void register() {
        User user = new User();
        user.setName(firstNameEditText.getText().toString());
        user.setSurname(lastNameEditText.getText().toString());
        String email = emailEditText.getText().toString();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(passwordEditText.getText().toString());
        RestObject result = null;
        try {
            result = restTemplate.register(user);
        } catch (Exception e) {
            handlerException(e, "RegisterActivity");
        }
        if (result != null) {
            if (result.isSuccess())
                onSuccess();
            else {
                final String result1 = result.getError();
                final String resultFinal = result1.replace(" ", "_");
                onFailure(((result.getError() == null) ? languageManager.translate("something_when_wrong") : languageManager.translate(resultFinal)));
            }
        } else {
            onFailure(languageManager.translate("fail_registration"));
        }
    }

    private String getFailMessage(String messageIn) {
        return (languageManager.translate("login_fail") + ": " + messageIn);
    }

    private void onSuccess() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                animationFadeOut = AnimationUtils.loadAnimation(RegisterActivity.this, anim.anim_fade_out);
                animationFadeIn = AnimationUtils.loadAnimation(RegisterActivity.this, anim.anim_fade_in);
                animationFadeIn.setStartOffset(300);
                dismissProgressDialog();
                layout.startAnimation(animationFadeOut);
                layout.setVisibility(View.GONE);
                regSuccessLayout.setVisibility(View.VISIBLE);
                regSuccessLayout.setAnimation(animationFadeIn);
            }
        });
    }

    private void onFailure(final String input) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                showAlertDialog(languageManager.translate("failed"), input, false);
            }
        });
    }

}
