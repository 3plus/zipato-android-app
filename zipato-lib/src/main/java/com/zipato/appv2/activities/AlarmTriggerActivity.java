/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zipato.annotation.Translated;
import com.zipato.appv2.B;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.string;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.alarm.ArmMode;
import com.zipato.model.alarm.ArmRequestRest;
import com.zipato.model.alarm.ZonesRepository;
import com.zipato.model.client.RestObject;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import butterfork.OnClick;

/**
 * Created by murielK on 8/12/2015.
 */
public class AlarmTriggerActivity extends Activity {

    public static final String KEY_BUNDLE_UUID = "KEY_BUNDLE_UUID";
    public static final String KEY_BUNDLE_ARM_MODE = "KEY_BUNDLE_ARM_MODE";
    private static final String TAG = TagFactoryUtils.getTag(AlarmTriggerActivity.class);
    private final List<Character> pinList = new ArrayList<>();
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    ExecutorService executor;
    @Inject
    LanguageManager languageManager;
    @Inject
    TypeFaceUtils typeFaceUtils;
    @Inject
    InternetConnectionHelper internetConnectionHelper;
    @Inject
    ZonesRepository zonesRepository;

    @Bind(B.id.keypadLayout)
    LinearLayout keypad;
    @Bind(B.id.textViewPad0)
    TextView keyPad0;
    @Bind(B.id.textViewPad1)
    TextView keyPad1;
    @Bind(B.id.textViewPad2)
    TextView keyPad2;
    @Bind(B.id.textViewPad3)
    TextView keyPad3;
    @Bind(B.id.textViewPad4)
    TextView keyPad4;
    @Bind(B.id.textViewPad5)
    TextView keyPad5;
    @Bind(B.id.textViewPad6)
    TextView keyPad6;
    @Bind(B.id.textViewPad7)
    TextView keyPad7;
    @Bind(B.id.textViewPad8)
    TextView keyPad8;
    @Bind(B.id.textViewPad9)
    TextView keyPad9;
    @Bind(B.id.imageViewPin)
    TextView indicator;
    @Bind(B.id.textViewEnterPin)
    TextView textViewEnterPin;
    @Translated("clear")
    @Bind(B.id.buttonPinClear)
    TextView butPinClear;
    private UUID partitionUUID;
    private ArmRequestRest armRequestRest;
    private ProgressDialog progressDialog;


    private static List<Integer> generateKeyPad(boolean random) {
        List<Integer> numbers = new ArrayList<Integer>();

        for (int i = 0; i < 10; i++) {

            if (i == (9)) numbers.add(0);
            else numbers.add(i + 1);
        }

        if (random)
            Collections.shuffle(numbers);

        return numbers;
    }

    private void setKeyPad(List<Integer> padNumber) {
        Typeface typeface = typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf");
        keyPad0.setTypeface(typeface);
        keyPad1.setTypeface(typeface);
        keyPad2.setTypeface(typeface);
        keyPad3.setTypeface(typeface);
        keyPad4.setTypeface(typeface);
        keyPad5.setTypeface(typeface);
        keyPad6.setTypeface(typeface);
        keyPad7.setTypeface(typeface);
        keyPad8.setTypeface(typeface);
        keyPad9.setTypeface(typeface);
        textViewEnterPin.setTypeface(typeface);
        butPinClear.setTypeface(typeface);

        keyPad0.setText(String.valueOf(padNumber.get(0)));
        keyPad1.setText(String.valueOf(padNumber.get(1)));
        keyPad2.setText(String.valueOf(padNumber.get(2)));
        keyPad3.setText(String.valueOf(padNumber.get(3)));
        keyPad4.setText(String.valueOf(padNumber.get(4)));
        keyPad5.setText(String.valueOf(padNumber.get(5)));
        keyPad6.setText(String.valueOf(padNumber.get(6)));
        keyPad7.setText(String.valueOf(padNumber.get(7)));
        keyPad8.setText(String.valueOf(padNumber.get(8)));
        keyPad9.setText(String.valueOf(padNumber.get(9)));


    }

    @OnClick(B.id.textViewPad0)
    public void onClickKeyPad0(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad1)
    public void onClickKeyPad1(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad2)
    public void onClickKeyPad2(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad3)
    public void onClickKeyPad3(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad4)
    public void onClickKeyPad4(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad5)
    public void onClickKeyPad5(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad6)
    public void onClickKeyPad6(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad7)
    public void onClickKeyPad7(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad8)
    public void onClickKeyPad8(View v) {
        keyPadHandler(v);
    }

    @OnClick(B.id.textViewPad9)
    public void onClickKeyPad9(View v) {
        keyPadHandler(v);

    }

    @OnClick(B.id.buttonPinClear)
    public void onClickButPinClear() {
        clearPin();
    }

    private void keyPadHandler(View v) {
        int size = pinList.size();
        if (size < 4) {
            TextView textView = (TextView) v;
            pinList.add(textView.getText().charAt(0));
            setIndicator();
            if (pinList.size() == 4) {
                StringBuilder stringBuilder = new StringBuilder(4);
                for (int i = 0; i < 4; i++) {
                    stringBuilder.append(pinList.get(i));
                }
                String pin = stringBuilder.toString();
                Log.d(TAG, " PIN: " + pin);
                checkInternet();
                performLoginTrigger(pin);
            }
        }
    }


    private void setIndicator() {
        switch (pinList.size()) {
            case 0:
                indicator.setText("");
                break;
            case 1:
                indicator.setText("*");
                break;
            case 2:
                indicator.setText("*  *");
                break;
            case 3:
                indicator.setText("*  *  *");
                break;
            case 4:
                indicator.setText("*  *  *  *");
                break;
            default:
                indicator.setText("");
                break;

        }
    }

    private void clearPin() {
        pinList.clear();
        setIndicator();
    }

    private void performLoginTrigger(final String pin) {
        showProgressDialog(languageManager.translate("establishing_secure_connection"));
        clearPin();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                final String error = login(pin);
                if (error == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDialogMode();
                        }
                    });
                    final boolean success = trigger();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog();
                            if (success)
                                finish();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissDialog();
                            if (restTemplate.isUseLocal()) {
                                final String localMsg = languageManager.translate("connection_error_local").replace("{productName}", getResources().getString(string.reg_box));
                                Toast.makeText(AlarmTriggerActivity.this, localMsg, Toast.LENGTH_SHORT).show();
                            } else if (!"dummy".equalsIgnoreCase(error)) {
                                Toast.makeText(AlarmTriggerActivity.this, error, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AlarmTriggerActivity.this, languageManager.translate("something_when_wrong"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

        });
    }

    private void checkInternet() {
        if (!internetConnectionHelper.isOnline() && !restTemplate.isUseLocal()) {
            Toast.makeText(this, languageManager.translate("internet_error_refresh"), Toast.LENGTH_SHORT).show();
            clearPin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkInternet();
        performStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private String login(final String pin) {
        String error = "dummy";
        try {
            Log.d(TAG, "Starting secure connection process...");
            error = restTemplate.pinLogin(pin);
        } catch (Exception e) {
            Log.d(TAG, "", e);
            restTemplate.clearSecureSessionId();
        }
        return error;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_trigger);
        ((ZipatoApplication) getApplication()).inject(this);
        ButterFork.bind(this);
        progressDialog = new ProgressDialog(this); //TODO add check for darkTheme or Light theme
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        setKeyPad(generateKeyPad(true));
        init();
    }

    private void showProgressDialog(CharSequence message) {
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void dismissDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void init() {
        final Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        partitionUUID = (UUID) bundle.getSerializable(KEY_BUNDLE_UUID);
        armRequestRest = new ArmRequestRest();
        ArmMode armMode = (ArmMode) bundle.getSerializable(KEY_BUNDLE_ARM_MODE);
        armRequestRest.setArmMode(armMode);
    }

    private void showDialogMode() {
        switch (armRequestRest.getArmMode()) {
            case HOME:
            case AWAY:
                showProgressDialog(languageManager.translate("arming"));
                break;
            case DISARMED:
                showProgressDialog(languageManager.translate("disarming"));
                break;
        }
    }

    private void enableKeyPad() {
        dismissDialog();
        keypad.setVisibility(View.VISIBLE);
    }

    private void performStart() {
        showDialogMode();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    private void start() {
        if (restTemplate.getSecuritySessionId() != null) {
            boolean isAlive = false;
            try {
                isAlive = restTemplate.keepAlive();
            } catch (Exception e) {
                Log.e(TAG, "", e);
                restTemplate.clearSecureSessionId();
            }

            if (isAlive) {
                final boolean success = trigger();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        if (success)
                            finish();
                        else
                            enableKeyPad();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enableKeyPad();
                    }
                });
            }

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enableKeyPad();
                }
            });
        }

    }

    private void handleTriggerError(final RestObject restObject) {
        if (restObject != null) {
            if (!restObject.isSuccess()) {
                if (restObject.getError() != null)
                    Toast.makeText(this, restObject.getError(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, languageManager.translate("something_when_wrong"), Toast.LENGTH_SHORT).show();
        }

    }


    private boolean trigger() {
        final String ssClientID = restTemplate.getSecuritySessionId();
        armRequestRest.setSecureSessionId(ssClientID);
        if (armRequestRest.getBypassZones() == null)
            armRequestRest.setBypassZones(new ArrayList<String>());

        final List<UUID> bypassedZones = zonesRepository.getBypassedZones(partitionUUID);
        if (bypassedZones != null) {
            for (UUID uuid : bypassedZones) {
                armRequestRest.addBypassZones(uuid.toString());
            }
        }

        RestObject restObject = null;
        try {
            restObject = restTemplate.postForObject("v2/alarm/partitions/{uuidPartition}/setMode", armRequestRest, RestObject.class, partitionUUID.toString());
            return (restObject != null) && restObject.isSuccess();
        } catch (Exception e) {
            Log.d(TAG, "", e);
            restTemplate.clearSecureSessionId();
        } finally {
            final RestObject finalRestObject = restObject;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    handleTriggerError(finalRestObject);
                }
            });
        }

        return false;

    }
}
