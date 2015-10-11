/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.bm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;

import java.util.List;

/**
 * Created by murielK on 10/6/2015.
 */
public class MainRoomFragment extends BaseFragment {
    @Override
    protected int getResourceView() {
        return R.layout.bm_frames_place_holder;
    }

    @Override
    protected void onPostViewCreate() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentManager fm = getChildFragmentManager();
            fm.beginTransaction().replace(R.id.leftFrame, new RoomFragment())
                    .commit();
            fm.beginTransaction().replace(R.id.rightFrame, new TypesRoomFragment())
                    .commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
