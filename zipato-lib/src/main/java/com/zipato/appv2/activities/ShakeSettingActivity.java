/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

/**
 * Created by murielK on 10/30/2014.
 */

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.appv2.R;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.R.id;
import com.zipato.helper.PreferenceHelper.Preference;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class ShakeSettingActivity extends BaseActivity implements View.OnClickListener {

    @InjectView(id.checkBox)
    CheckBox checkBox;
    @InjectView(id.EnableShakeLaout)
    LinearLayout enableShake;
    @InjectView(id.seekBarShakeForce)
    SeekBar seekBarForce;
    @InjectView(id.seekBarShakeTimeOut)
    SeekBar seekBarTimeOut;
    @InjectView(id.tryoutLayout)
    LinearLayout tryIt;
    @InjectView(id.imageViewTryIt)
    ImageView myImageView;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_shake_speed")
    @InjectView(id.textViewShakeForceTitle)
    TextView textViewForceTitle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_shake_speed_des")
    @InjectView(id.textViewShakeForceDes)
    TextView textViewForceDes;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_shake_timeout")
    @InjectView(id.textViewTimeOutTitle)
    TextView textViewTimeOutTitle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_shake_timeout_des")
    @InjectView(id.textViewTimeOutDes)
    TextView textViewTimeOutDes;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_shake_enable")
    @InjectView(id.textView)
    TextView textView;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_shake_des")
    @InjectView(id.textView2)
    TextView textView2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_try_it")
    @InjectView(id.textViewTryItTittle)
    TextView textViewTryItTitle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("pref_try_it_des")
    @InjectView(id.textViewTryItDes)
    TextView textViewTryItDes;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewShakeForce)
    TextView textViewForce;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewTimeOut)
    TextView textViewTimeOut;
    private int force;
    private int timeOut;
    private LinearLayout myLayout;
    private TextView myTextView;
    private AlertDialog myDialog;


    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_shake_setting;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterKnife.inject(this);
        force = preferenceHelper.getInt(Preference.SHAKE_FORCE, 500);
        timeOut = preferenceHelper.getInt(Preference.SHAKE_TIME_OUT, 500);
        enableShake.setOnClickListener(this);
        tryIt.setOnClickListener(this);
        myLayout = new LinearLayout(this);
        myTextView = new TextView(this);
        if (VERSION.SDK_INT < 11) {
            myTextView.setTextColor(Color.WHITE);
        }
        seekBarForce.setProgress(force);
        seekBarTimeOut.setProgress(timeOut);
        textViewForce.setText(String.valueOf(force));
        textViewTimeOut.setText(timeOut + " ms");
        seekBarForce.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                force = progress;
                textViewForce.setText(String.valueOf(force));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBarTimeOut.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeOut = progress;
                textViewTimeOut.setText(timeOut + " ms");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        boolean checked = preferenceHelper.getBooleanPref(Preference.SHAKE_ENABLE);
        if (checked) {
            checkBox.setChecked(true);
            //enabler();
        } else {
            checkBox.setChecked(false);
            setDisable();
        }
    }

    @Override
    protected boolean provideMenu() {
        return false;
    }


    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case id.EnableShakeLaout:
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                    preferenceHelper.putBooleanPref(Preference.SHAKE_ENABLE, false);
                    shakeUtils.setEnableShake(false);
                    setDisable();

                } else {
                    checkBox.setChecked(true);
                    preferenceHelper.putBooleanPref(Preference.SHAKE_ENABLE, true);
                    shakeUtils.setEnableShake(true);
                    enabler();
                }
                break;

            case id.tryoutLayout:
                setter();
                shakeUtils.registerToSensor();
                Builder builder = new Builder(this);
                builder.setCancelable(false);
                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.layout_shake_try_out, null);
                TextView textView = (TextView) view.findViewById(id.textView);
                textView.setText(languageManager.translate("try_message"));
                builder.setView(view);

                builder.setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shakeUtils.unRegisterToSensor();
                        dialog.cancel();
                    }
                });
                myDialog = builder.create();
                myDialog.show();
                break;
        }
    }

    @Override
    public void onShake() {
        vib.vibrate(50);
        toast(languageManager.translate("shake_register"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setter();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("ShakeSetting", "onPause was called");
        if (myDialog != null) {
            myDialog.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        shakeUtils.unRegisterToSensor();
    }

    private void setDisable() {
        textViewForceTitle.setEnabled(false);
        textViewForceDes.setEnabled(false);
        textViewTimeOutTitle.setEnabled(false);
        textViewTimeOutDes.setEnabled(false);
        textViewTryItTitle.setEnabled(false);
        textViewTryItDes.setEnabled(false);
        textViewForce.setEnabled(false);
        textViewTimeOut.setEnabled(false);
        seekBarTimeOut.setEnabled(false);
        seekBarForce.setEnabled(false);
        tryIt.setEnabled(false);
        myImageView.setImageResource(drawable.ic_shake_try_disable);
    }

    private void enabler() {
        textViewForceTitle.setEnabled(true);
        textViewForceDes.setEnabled(true);
        textViewTimeOutTitle.setEnabled(true);
        textViewTimeOutDes.setEnabled(true);
        textViewTryItTitle.setEnabled(true);
        textViewTryItDes.setEnabled(true);
        textViewForce.setEnabled(true);
        textViewTimeOut.setEnabled(true);
        seekBarTimeOut.setEnabled(true);
        seekBarForce.setEnabled(true);
        tryIt.setEnabled(true);
        myImageView.setImageResource(drawable.ic_shake_try);

    }

    private void setter() {
        preferenceHelper.putInt(Preference.SHAKE_FORCE, force);
        preferenceHelper.putInt(Preference.SHAKE_TIME_OUT, timeOut);
        shakeUtils.setForceTimeOut(force, timeOut);
    }


}