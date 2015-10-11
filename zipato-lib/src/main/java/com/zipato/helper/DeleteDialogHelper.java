/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by murielK on 9/4/2014.
 */
public class DeleteDialogHelper {

    private final OnPositiveClicked listner;
    private final Context context;
    private final List<String> items;
    private final String textMessage1;
    private final int dialogIconRes;
    private final String dialogTitle;
    private final String negativeButtonText;
    private final String positiveButtonText;

    public DeleteDialogHelper(Context context, List<String> items, String textMessage1, int dialogIconRes,
                              String dialogTitle, String negativeButtonText,
                              String positiveButtonText, OnPositiveClicked listner) {
        this.listner = listner;
        this.context = context;
        this.items = items;
        this.textMessage1 = textMessage1;
        this.dialogIconRes = dialogIconRes;
        this.dialogTitle = dialogTitle;
        this.negativeButtonText = negativeButtonText;
        this.positiveButtonText = positiveButtonText;
    }

    public void show() {
        final int dpAsPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpAsPixels;
        params.topMargin = dpAsPixels;
        TextView tempTextView = new TextView(context);
        ListView tempListView = new ListView(context);
        if (textMessage1 == null || textMessage1.isEmpty()) {
            tempTextView.setVisibility(View.GONE);
        } else {
            tempTextView.setText(textMessage1);
        }
        if (Build.VERSION.SDK_INT < 11)
            tempTextView.setTextColor(Color.WHITE);
        tempTextView.setLayoutParams(params);
        tempTextView.setGravity(Gravity.CENTER);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        ListAdapter arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, items);
        tempListView.setAdapter(arrayAdapter);
        tempListView.setLayoutParams(params);
//                    tempListView.setBackgroundColor(Color.WHITE);
//                    tempListView.setCacheColorHint(Color.WHITE);
        linearLayout.addView(tempTextView);
        linearLayout.addView(tempListView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setIcon(dialogIconRes);
        builder.setTitle(dialogTitle);
        builder.setView(linearLayout);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listner != null) {
                    listner.onPositiveClicked();
                }

            }
        });

        builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog removeDialog = builder.create();
        removeDialog.show();
    }

    public interface OnPositiveClicked {
        void onPositiveClicked();
    }
}
