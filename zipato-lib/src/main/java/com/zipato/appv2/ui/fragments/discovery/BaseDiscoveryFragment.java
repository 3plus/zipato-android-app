/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.util.TypeFaceUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 8/13/2014.
 */
public abstract class BaseDiscoveryFragment extends BaseFragment {


    protected static final String PARCELABLE_KEY = "PARCELABLE_KEY";
    private static final String TAG = BaseDiscoveryFragment.class.getSimpleName();
    @Inject
    protected EventBus eventBus;

    protected FrameLayout frameLayout;

    @Inject
    TypeFaceUtils typeFaceUtils;

    abstract String stringTitle();

    abstract int stepID();

    abstract String fragmentTag();

    abstract String stringButTitle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frameLayout = new FrameLayout(getContext());
        if (getResourceView() == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        View v = inflater.inflate(getResourceView(), null);
        ButterKnife.inject(this, v);
        typeFaceUtils.applyTypefaceFor(this);
        frameLayout.addView(v);
        onPostViewCreate();
        return frameLayout;

    }

    public int getStepId() {
        return stepID();
    }

    public String getFragmentTag() {
        return fragmentTag();
    }

    public String getTitle() {
        return stringTitle();
    }

    public String getButtTitle() {
        return stringButTitle();
    }

    @Override
    public void onResume() {
        eventBus.register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    public void onEventMainThread(Integer onUpdate) {

    }
}
