/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_DISABLE_VIEW;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_DISMISS_P_DIALOG;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_ENABLE_VIEW;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_REFRESH_ADAPTER;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_REFRESH_RECYCLER_ADAPTER;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_SHOW_CANCELABLE_P_DIALOG;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_SHOW_P_DIALOG;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_TOAST;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_VISIBILITY_GONE;
import static com.zipato.appv2.ui.fragments.BaseFragment.MAIN_UI_VISIBILITY_VISIBLE;

/**
 * Created by murielK on 9/29/2015.
 */
@Retention(RetentionPolicy.CLASS)
@IntDef({MAIN_UI_DISABLE_VIEW, MAIN_UI_DISMISS_P_DIALOG, MAIN_UI_ENABLE_VIEW, MAIN_UI_REFRESH_ADAPTER, MAIN_UI_REFRESH_RECYCLER_ADAPTER
        , MAIN_UI_SHOW_CANCELABLE_P_DIALOG, MAIN_UI_SHOW_P_DIALOG, MAIN_UI_TOAST, MAIN_UI_VISIBILITY_GONE, MAIN_UI_VISIBILITY_VISIBLE})
public @interface MainUIPostID {
}
