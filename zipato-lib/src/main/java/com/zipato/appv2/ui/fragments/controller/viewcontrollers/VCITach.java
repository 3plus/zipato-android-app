/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.R.color;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by murielK on 8/21/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_I_TACH)
public class VCITach extends AbsIR {

    private static final String TAG = TagFactoryUtils.getTag(VCITach.class);


    public VCITach(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return 0;
    }

    @Override
    public void handleLongClick(TextView textView) {
        if (mode == MODE_NORMAL)
            super.handleLongClick(textView);
        else onSetCode(textView);
    }

    private void onSetCode(TextView textView) {
        if (!isClusterValid())
            return;

        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on onSetCode method");
            return;
        }

        final ClusterEndpoint clusterEndpoint = getCluster();
        final int pointer = getPointFromTextView(textView);
        String command = null;
        try {
            command = clusterEndpoint.getConfig().getSlotNames()[pointer - 1];
        } catch (Exception e) {
            //
        }

        final DialogViewHolder dialogViewHolder = new DialogViewHolder(context);
        dialogViewHolder.getEditText().setTextColor(context.getResources().getColor(color.color_white));
        dialogViewHolder.getEditText().setHint(languageManager.translate("set_code"));

        Builder builder = new Builder(context);
        builder.setView(dialogViewHolder.getLinearLayout());
        if (command != null)
            builder.setTitle(languageManager.translate("set_code") + ": " + command);
        else
            builder.setTitle(languageManager.translate("set_code"));
        builder.setPositiveButton(languageManager.translate("save"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(languageManager.translate("cancel"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogViewHolder.getEditText().getText() != null) {
                    setCode(dialogViewHolder.getEditText().getText().toString(), pointer);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, languageManager.translate("please_enter_title"), Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void setCode(final String value, final int pointer) {
        if (!isClusterValid())
            return;

        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on onSetCode method");
            return;
        }

        final ClusterEndpoint clusterEndpoint = getCluster();
        final Handler handler = getHandler();

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(languageManager.translate("saving_configurations"));
        progressDialog.setCancelable(false);
        progressDialog.show();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                Map<String, Object> object = new HashMap<>();
                try {
                    object.put("command", value);
                    object.put("name", pointer);
                    restTemplate.postForObject("v2/clusterEndpoints/{uuid}/actions/saveCode", object, HashMap.class, clusterEndpoint.getUuid());

                } catch (Exception e) {
                    success = false;

                } finally {

                    if ((handler != null)) {
                        final boolean finalSuccess = success;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (!finalSuccess)
                                    Toast.makeText(context, languageManager.translate("saving_config_fail"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public int provideViewTypeID() {
        return layout.view_controller_ir;
    }

    @Override
    public String provideLearnAction() {
        return "learnCode";
    }

    @Override
    public String provideLearnName() {
        return "name";
    }

    @Override
    public String provideReceivedCommand() {
        return languageManager.translate("command_learned");
    }
}
