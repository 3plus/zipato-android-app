/*
 * Copyright (c) 2015 Tri plus d.o.o. All right reserved.
 */

package com.zipato.appv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;

import java.io.Serializable;

import butterknife.ButterKnife;

/**
 * Created by Triplus3 on 10/9/2015.
 */
public abstract class AbsMenuActivity extends BaseActivity {

    public static final String SHOW_ID_BUNDLE_KEY = "SHOW_ID_BUNDLE_KEY";


    private int showID;

    protected abstract BaseFragment getFragmentFor(int showID);

    @Override
    protected boolean provideMenu() {
        return false;
    }

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterKnife.inject(this);
        typeFaceUtils.applyTypefaceFor(this);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            showID = intent.getExtras().getInt(SHOW_ID_BUNDLE_KEY);
        }

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.fragmentHolder, getFragmentFor(showID)).commit();
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected Parcelable getParcelable() {
        final Intent intent = getIntent();
        if ((intent != null) && intent.hasExtra(BaseFragment.PARCELABLE_KEY))
            return intent.getExtras().getParcelable(BaseFragment.PARCELABLE_KEY);
        return null;
    }

    protected Serializable getSerializable() {
        final Intent intent = getIntent();
        if ((intent != null) && intent.hasExtra(BaseFragment.SERIALIZABLE_KEY))
            return intent.getExtras().getSerializable(BaseFragment.SERIALIZABLE_KEY);
        return null;
    }
}
