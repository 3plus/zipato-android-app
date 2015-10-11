/*
 * Copyright (c) 2015 Tri plus d.o.o. All right reserved.
 */

package com.zipato.appv2.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

import com.zipato.annotation.DialogID;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.vcmenu.ScenesIconColorFragment;
import com.zipato.util.TagFactoryUtils;

import java.io.Serializable;

/**
 * Created by Triplus3 on 10/9/2015.
 */
public class ShowDialogActivity extends AbsMenuActivity {

    public static final int SHOW_ICON_COLOR_ID = 0;

    private static final String TAG = TagFactoryUtils.getTag(ShowDialogActivity.class);

    public static void show(Context context, Parcelable parcelable, @DialogID int showID) {
        Intent intent = new Intent(context, ShowDialogActivity.class);
        intent.putExtra(BaseFragment.PARCELABLE_KEY, parcelable);
        intent.putExtra(SHOW_ID_BUNDLE_KEY, showID);
        context.startActivity(intent);
    }

    public static void show(Context context, Serializable serializable, @DialogID int showID) {
        Intent intent = new Intent(context, ShowDialogActivity.class);
        intent.putExtra(BaseFragment.SERIALIZABLE_KEY, serializable);
        intent.putExtra(SHOW_ID_BUNDLE_KEY, showID);
        context.startActivity(intent);
    }

    public static void showForResult(Fragment fragment, Parcelable parcelable, @DialogID int showID, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), ShowDialogActivity.class);
        intent.putExtra(BaseFragment.PARCELABLE_KEY, parcelable);
        intent.putExtra(SHOW_ID_BUNDLE_KEY, showID);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void showForResult(Fragment fragment, Serializable serializable, @DialogID int showID, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), ShowDialogActivity.class);
        intent.putExtra(BaseFragment.SERIALIZABLE_KEY, serializable);
        intent.putExtra(SHOW_ID_BUNDLE_KEY, showID);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected BaseFragment getFragmentFor(int showID) {
        switch (showID) {
            case SHOW_ICON_COLOR_ID:
                return BaseFragment.newInstance(ScenesIconColorFragment.class, getSerializable());
        }

        return null;
    }

    @Override
    protected boolean provideMenu() {
        return false;
    }

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {
        final Serializable uuid = getSerializable();
        if ((uuid == null)) {
            finish();
            return;
        }

        super.onPostContentView(savedInstanceState);

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_show_dialog;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {

    }

}
