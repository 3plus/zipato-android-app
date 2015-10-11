/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

import com.viewpagerindicator.CirclePageIndicator;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.customview.CustomViewPager;
import com.zipato.util.Utils;

import butterfork.Bind;

/**
 * Created by murielK on 11/6/2014.
 */
public class DMCMConfigFragment extends BaseFragment {

    private static final String TAG = DMCMConfigFragment.class.getSimpleName();
    @Bind(B.id.viewPager)
    CustomViewPager viewPager;
    @Bind(B.id.titles)
    CirclePageIndicator circlePageIndicator;

    @Override
    protected int getResourceView() {
        return R.layout.fragment_dm_cm;
    }

    @Override
    protected void onPostViewCreate() {
        //
    }

    @Override
    protected void onViewReady(View v) {
        if (Utils.isPreJellyBean())
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        circlePageIndicator.setViewPager(viewPager);
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0:
                    return newInstance(DMConfigFragment.class, getArguments());
                case 1:
                    return newInstance(DMIconConfigColorFragment.class, getArguments());

            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
