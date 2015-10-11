/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.VCMenuID;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.cameras.ArchiveFragment;
import com.zipato.appv2.ui.fragments.security.SecurityEventFragment;
import com.zipato.appv2.ui.fragments.security.ZonesFragment;
import com.zipato.appv2.ui.fragments.vcmenu.ConfigFragment;
import com.zipato.appv2.ui.fragments.vcmenu.EventFragment;
import com.zipato.appv2.ui.fragments.vcmenu.IconConfigColorFragment;
import com.zipato.helper.AssetLoaderHelper;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectIcon;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportRepository;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.Utils;

import java.io.Serializable;

import javax.inject.Inject;

import butterknife.InjectView;

/**
 * Created by murielK on 9/28/2015.
 */
public class ShowVCMenu extends AbsMenuActivity {

    public static final int SHOW_ID_CONFIG = 0;
    public static final int SHOW_ID_CHANGE_ICON = 1;
    public static final int SHOW_ID_EVENT = 2;
    public static final int SHOW_ID_EVENT_SECURITY = 3;
    public static final int SHOW_ID_ZONES = 4;
    public static final int SHOW_ID_ARCHIVES = 5;
    private static final String TAG = TagFactoryUtils.getTag(ShowVCMenu.class);


    @Inject
    AssetLoaderHelper assetLoaderHelper;
    @Inject
    TypeReportRepository typeReportRepository;

    @InjectView(R.id.imageViewIC)
    ImageView imageIC;
    @SetTypeFace("icomoon.ttf")
    @InjectView(R.id.textViewKK)
    TextView textViewKK;
    @SetTypeFace("helvetica_neue_light.otf")
    @InjectView(R.id.textViewDName)
    TextView textViewDNAme;

    public static void show(Context context, Parcelable parcelable, @VCMenuID int showID) {
        Intent intent = new Intent(context, ShowVCMenu.class);
        intent.putExtra(BaseFragment.PARCELABLE_KEY, parcelable);
        intent.putExtra(SHOW_ID_BUNDLE_KEY, showID);
        context.startActivity(intent);
    }

    public static void show(Context context, Serializable serializable, @VCMenuID int showID) {
        Intent intent = new Intent(context, ShowVCMenu.class);
        intent.putExtra(BaseFragment.SERIALIZABLE_KEY, serializable);
        intent.putExtra(SHOW_ID_BUNDLE_KEY, showID);
        context.startActivity(intent);
    }

    @Override
    protected BaseFragment getFragmentFor(int showID) {
        switch (showID) {
            case SHOW_ID_CHANGE_ICON:
                return BaseFragment.newInstance(IconConfigColorFragment.class, getParcelable());
            case SHOW_ID_CONFIG:
                return BaseFragment.newInstance(ConfigFragment.class, getParcelable());
            case SHOW_ID_EVENT:
                return BaseFragment.newInstance(EventFragment.class, getParcelable());
            case SHOW_ID_EVENT_SECURITY:
                return BaseFragment.newInstance(SecurityEventFragment.class, getParcelable());
            case SHOW_ID_ZONES:
                return BaseFragment.newInstance(ZonesFragment.class, getParcelable());
            case SHOW_ID_ARCHIVES:
                return BaseFragment.newInstance(ArchiveFragment.class, getParcelable());
        }

        return null;
    }

    @Override
    protected boolean provideMenu() {
        return false;
    }

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_show_vc_menu;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        final Parcelable key = getParcelable();

        if ((key == null) || (typeReportRepository.get(key) == null)) {
            finish();
            return;
        }

        super.onPostContentView(savedInstanceState);

        setHeader();
    }

    private void setHeader() {
        final Parcelable key = getParcelable();
        if (key == null)
            return;

        final TypeReportItem item = typeReportRepository.get(key);

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
            assetLoaderHelper.loadAsset(item.getUiType().getEndpointType() + ".png", imageIC);
        } else
            assetLoaderHelper.loadAsset(item.getUiType().getEndpointType() + ".png", imageIC);
    }

    @Override
    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_REFRESH_REQUEST:
                setHeader();
                break;
            case Event.EVENT_TYPE_KITKAT_ICON:
                if (textViewKK.getVisibility() == View.GONE)
                    textViewKK.setVisibility(View.VISIBLE);
                if (imageIC.getVisibility() == View.VISIBLE)
                    imageIC.setVisibility(View.GONE);
                ObjectIcon objectIcon = (ObjectIcon) event.eventObject;
                if (objectIcon.kkEventType == ObjectIcon.TYPE_COLOR)
                    textViewKK.setTextColor(Color.parseColor(String.valueOf(objectIcon.value)));

                else textViewKK.setText(Utils.getHexForKitKat(String.valueOf(objectIcon.value)));
                break;
            default:
                super.onEventMainThread(event);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar Up/Home button
            case android.R.id.home:
                Intent backIntent = new Intent(this, BrowserManagerActivity.class);
                backIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                NavUtils.navigateUpTo(this, backIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
