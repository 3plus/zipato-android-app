/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.appv2.R;
import com.zipato.model.DynaObject;
import com.zipato.model.box.Box;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import io.vov.vitamio.utils.Log;

/**
 * Created by murielK on 2/25/2015.
 */
public class BoxInfoFragment extends AbsBaseSubMenu {

    public static final String BOX_PARCELABLE_KEY = "BOX_PARCELABLE_KEY";
    private static final String TAG = BoxInfoFragment.class.getSimpleName();
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("controller_name")
    @InjectView(R.id.textViewName)
    TextView textViewName;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("serial_number")
    @InjectView(R.id.textViewSerial)
    TextView textViewSerial;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("local_ip")
    @InjectView(R.id.textViewLocalIp)
    TextView textViewLocalIp;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("remote_ip")
    @InjectView(R.id.textViewRemoteIp)
    TextView textViewRemoteIp;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("firmware_version")
    @InjectView(R.id.textViewFirmware)
    TextView textViewFirmware;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("time_zone")
    @InjectView(R.id.textViewTImeZone)
    TextView textViewTImeZone;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("new_firmware")
    @InjectView(R.id.textViewNewFirmware)
    TextView textViewNewFirmware;
    @SetTypeFace("helvetica_neue_light.otf")
    @Translated("update_fw")
    @InjectView(R.id.buttonUpdate)
    Button buttonUpdateFW;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewNValue)
    TextView textViewNValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewSValue)
    TextView textViewSValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewLIPValue)
    TextView textViewLIPValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewRIPValue)
    TextView textViewRIPValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewFVersionValue)
    TextView textViewFVersionValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewTZValue)
    TextView textViewTZValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.textViewNFValue)
    TextView textViewNFValue;

    @InjectView(R.id.layoutNewFirmware)
    LinearLayout linearLayoutFW;
    @InjectView(R.id.layoutFWUpDivider)
    LinearLayout layoutFWUpDivider;

    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    ExecutorService executor;

    private Box box;


    public static BoxInfoFragment newInstance(Parcelable boxParcel) {
        Bundle bundle = new Bundle(1);
        bundle.putParcelable(BOX_PARCELABLE_KEY, boxParcel);
        BoxInfoFragment boxInfoFragment = new BoxInfoFragment();
        boxInfoFragment.setArguments(bundle);
        return boxInfoFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            try {
                box = getArguments().getParcelable(BOX_PARCELABLE_KEY);
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        }

        setViews();

    }

    @OnClick(R.id.buttonUpdate)
    public void onUpdateClick() {
        if (!checkBox())
            return;

        sendFWUpdateRequest();

    }

    private void showRequiredMessage() {
        if ((box == null) || !box.isFirmwareUpgradeRequired())
            return;

        final View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_fw_up_message, null);
        final TextView message = (TextView) v.findViewById(R.id.textViewMsg);
        final String messageText = languageManager.translate("firmware_up_required");
        message.setText(messageText.replace("{productName}", getContext().getResources().getString(R.string.reg_box)));
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(v);
        builder.setPositiveButton(languageManager.translate("ok"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean checkBox() {
        if (box == null) {
            backToMain();
            return false;
        }

        return true;
    }

    private void setViews() {
        if (!checkBox())
            return;

        if (!checkNewFW()) {
            linearLayoutFW.setVisibility(View.GONE);
            buttonUpdateFW.setVisibility(View.GONE);
            layoutFWUpDivider.setVisibility(View.GONE);
        }

        showRequiredMessage();

        textViewNValue.setText((box.getName() == null) ? "" : box.getName());
        textViewSValue.setText((box.getSerial() == null) ? "" : box.getSerial());
        textViewLIPValue.setText((box.getLocalIp() == null) ? "" : box.getLocalIp());
        textViewRIPValue.setText((box.getRemoteIp() == null) ? "" : box.getRemoteIp());
        textViewFVersionValue.setText((box.getFirmwareVersion() == null) ? "" : box.getFirmwareVersion());
        textViewTZValue.setText((box.getTimezone() == null) ? "" : box.getTimezone());
        textViewNFValue.setText((box.getLatestFirmwareVersion() == null) ? "" : box.getLatestFirmwareVersion());

    }

    private boolean checkNewFW() {
        return (box != null) && box.isOnline() && box.isFirmwareUpgradeAvailable();
    }

    private void sendFWUpdateRequest() {
        if (!checkBox())
            return;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    restTemplate.getForObject("v2/firmware/upgrade/release", DynaObject.class);
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            final String temp = languageManager.translate("box_update_req_sent");
                            final String finalTemp = temp.replace("{productName}", getContext().getResources().getString(R.string.reg_box));
                            toast(finalTemp);
                            buttonUpdateFW.setEnabled(false);
                        }
                    });

                } catch (Exception e) {
                    handlerException(e, TAG);
                }
            }
        });

    }


    @Override
    protected int getResourceView() {
        return R.layout.fragment_box_info;
    }

    @Override
    protected void onPostViewCreate() {

    }

    @Override
    protected String provideTitle() {
        final String temp = languageManager.translate("box_info");
        return temp.replace("{productName}", getContext().getResources().getString(R.string.reg_box));
    }
}

