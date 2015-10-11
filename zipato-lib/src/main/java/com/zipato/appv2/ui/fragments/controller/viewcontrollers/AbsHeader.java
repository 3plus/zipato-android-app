/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R.id;
import com.zipato.appv2.activities.ShowVCMenu;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.helper.AssetLoaderHelper;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.Utils;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by murielK on 7/15/2015.
 */
public abstract class AbsHeader extends ViewController {

    @Inject
    AssetLoaderHelper assetLoaderHelper;

    @InjectView(id.imageViewIC)
    ImageView imageIC;
    @SetTypeFace("icomoon.ttf")
    @InjectView(id.textViewKK)
    TextView textViewKK;
    @InjectView(id.progressBarVC)
    ProgressBar progressBar;
    @SetTypeFace("helvetica_neue_light.otf")
    @InjectView(id.textViewDName)
    TextView textViewDNAme;
    @InjectView(id.imgSubMenu)
    ImageButton buttonSubMenu;

    public AbsHeader(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    protected abstract int getMainIndexAttrToDisplay(TypeReportItem item);

    @Override
    public void dispatchOnBind(Object object) { // set the fucking header... kikat icon , title etc ...
        final TypeReportItem item = (TypeReportItem) object;

        textViewDNAme.setText((item.getName() == null) ? "" : item.getName());
        if (imageIC.getVisibility() == View.GONE)
            imageIC.setVisibility(View.VISIBLE);
        if (textViewKK.getVisibility() == View.VISIBLE)
            textViewKK.setVisibility(View.GONE);

        if ((item.getUserIcon() != null) && (item.getUserIcon().getRelativeUrl() != null) && item.getUserIcon().getRelativeUrl().contains("font")) {
            imageIC.setVisibility(View.GONE);
            textViewKK.setVisibility(View.VISIBLE);
            String[] tempString = item.getUserIcon().getRelativeUrl().split(":");
            final String font = tempString[tempString.length - 1];
            textViewKK.setText(Utils.getHexForKitKat(font));
        } else if (validateIndexFor(getMainIndexAttrToDisplay(item), item) && attributesHelper.isStateIconTrue(item.getAttributes()[getMainIndexAttrToDisplay(item)].getUuid())) {
            if ((item.getUiType() != null) && (item.getUiType().getEndpointType() != null))
                assetLoaderHelper.loadAsset(item.getUiType().getEndpointType() + ".1.png", item.getUiType().getEndpointType() + ".png", imageIC);
        } else if ((item.getUiType() != null) && (item.getUiType().getEndpointType() != null))
            assetLoaderHelper.loadAsset(item.getUiType().getEndpointType() + ".png", imageIC);

    }

    public boolean validateIndexFor(int index, TypeReportItem item) {
        if ((item.getAttributes() == null) || (index < 0) || (index >= item.getAttributes().length)) {
            Log.e(TagFactoryUtils.getTag(this), String.format("ArrayIndexOutOfBoundsException length=%d, index=%d", (item.getAttributes() == null) ? 0 : item.getAttributes().length, index));
            return false;
        }
        return true;
    }

    @OnClick(id.imgSubMenu)
    public void onMenuClick(View v) {
        showMenuDialog();
    }

    protected String[] getListMenu() {
        return new String[]{languageManager.translate("configuration"), languageManager.translate("change_icon"), languageManager.translate("event")};
    }

    private void showMenuDialog() {
        final Context context = getContext();
        final TypeReportItem item = getTypeReportItem();
        if ((context == null) || (item == null))
            return;

        Builder builder = new Builder(context);
        builder.setItems(getListMenu(), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleWhichMenu(context, item.getKey(), which);
            }
        });

        builder.show();
    }

    protected void handleWhichMenu(Context context, TypeReportKey key, int which) {
        switch (which) {
            case 0:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_CONFIG);
                break;
            case 1:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_CHANGE_ICON);
                break;
            case 2:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_EVENT);
                break;
        }
    }

}
