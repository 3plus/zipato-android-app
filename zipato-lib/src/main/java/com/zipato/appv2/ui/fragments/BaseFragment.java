/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.zipato.annotation.MainUIPostID;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.activities.BaseActivity;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.Utils;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by murielK on 2.7.2014..
 */
public abstract class BaseFragment extends Fragment {

    public static final String PARCELABLE_KEY = "PARCELABLE_KEY";
    public static final String SERIALIZABLE_KEY = "SERIALIZABLE_KEY";
    public static final int MAIN_UI_VISIBILITY_GONE = 0;
    public static final int MAIN_UI_VISIBILITY_VISIBLE = 1;
    public static final int MAIN_UI_TOAST = 2;
    public static final int MAIN_UI_REFRESH_ADAPTER = 3;
    public static final int MAIN_UI_SHOW_P_DIALOG = 4;
    public static final int MAIN_UI_SHOW_CANCELABLE_P_DIALOG = 5;
    public static final int MAIN_UI_DISMISS_P_DIALOG = 6;
    public static final int MAIN_UI_REFRESH_RECYCLER_ADAPTER = 7;
    public static final int MAIN_UI_DISABLE_VIEW = 8;
    public static final int MAIN_UI_ENABLE_VIEW = 9;

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_SELECT_PICTURE = 2;
    public static final int REQUEST_CODE = 3;


    @Inject
    protected InternetConnectionHelper internetConnectionHelper;
    @Inject
    protected LanguageManager languageManager;
    protected Handler baseFragmentHandler;


    public static <T extends BaseFragment> T newInstance(Class<T> tClass) {

        T fragment = null;

        try {
            fragment = tClass.newInstance();

        } catch (Exception e) {
            Log.d(TagFactoryUtils.getTag(BaseFragment.class), "", e);
        }
        return fragment;

    }

    public static <T extends BaseFragment> T newInstance(Class<T> tClass, Bundle bundle) {

        T fragment = null;

        try {
            fragment = tClass.newInstance();
            if (bundle != null)
                fragment.setArguments(bundle);
        } catch (Exception e) {
            Log.d(TagFactoryUtils.getTag(BaseFragment.class), "", e);
        }
        return fragment;

    }

    public static <T extends BaseFragment> T newInstance(Class<T> tClass, Parcelable parcelable) {

        if (tClass == null) {
            throw new NullPointerException("cannot create a new instance from a null object : <T extends BaseControllerFragment> ");
        }

        T fragment = null;

        try {
            fragment = tClass.newInstance();
            if (parcelable != null) {
                Bundle bundle = new Bundle(1);
                bundle.putParcelable(PARCELABLE_KEY, parcelable);
                fragment.setArguments(bundle);
            }
        } catch (Exception e) {
            Log.d(TagFactoryUtils.getTag(BaseFragment.class), "", e);
        }
        return fragment;
    }

    public static <T extends BaseFragment> T newInstance(Class<T> tClass, Serializable serializable) {

        if (tClass == null) {
            throw new NullPointerException("cannot create a new instance from a null object : <T extends BaseControllerFragment> ");
        }

        T fragment = null;

        try {
            fragment = tClass.newInstance();
            if (serializable != null) {
                Bundle bundle = new Bundle(1);
                bundle.putSerializable(SERIALIZABLE_KEY, serializable);
                fragment.setArguments(bundle);
            }
        } catch (Exception e) {
            Log.d(TagFactoryUtils.getTag(BaseFragment.class), "", e);
        }
        return fragment;
    }


    private static void setViewVisibility(View v, int visibility) {
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    private static void refreshBaseAdapter(BaseAdapter adapter) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static void refreshRecyclerBaseAdapter(Adapter adapter) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static void enableView(View view, boolean enable) {
        if (view != null) {
            view.setEnabled(enable);
        }
    }

    public Handler getBaseFragmentHandler() {
        return baseFragmentHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ZipatoApplication) getActivity().getApplication()).getObjectGraph().inject(this);
        baseFragmentHandler = new BaseFragmentHandler(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    protected void sendMessage(@MainUIPostID int what, Object object) {
        baseFragmentHandler.obtainMessage(what, object).sendToTarget();
    }

    protected abstract int getResourceView();

    protected abstract void onPostViewCreate();

    protected void onViewReady(View v) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getResourceView() == 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        final View v = inflater.inflate(getResourceView(), null);
        ButterKnife.inject(this, v);

        v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                onViewReady(v);
                if (Utils.isPreJellyBean())
                    v.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                else v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        onPostViewCreate();

        return v;
    }

    protected void showProgressDialog(String message, boolean cancelable) {
        final BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity == null)
            return;
        baseActivity.showProgressDialog(message, cancelable);
    }

    protected void dismissProgressDialog() {
        final BaseActivity baseActivity = (BaseActivity) getActivity();
        if (baseActivity == null)
            return;
        baseActivity.dismissProgressDialog();
    }

    protected void toast(final CharSequence message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    protected boolean checkInternet() {
        final BaseActivity baseActivity = (BaseActivity) getActivity();
        return baseActivity.checkInternet();
    }

    protected void handlerException(final Exception e, final String tag) {
        final BaseActivity baseActivity = (BaseActivity) getActivity();
        baseActivity.handlerException(e, tag);
    }

    private static class BaseFragmentHandler extends Handler {

        private final WeakReference<BaseFragment> weakBaseFragment;

        public BaseFragmentHandler(BaseFragment baseFragment) {
            weakBaseFragment = new WeakReference<>(baseFragment);
        }

        @Override
        public void handleMessage(Message msg) {

            BaseFragment baseFragment = weakBaseFragment.get();
            if ((baseFragment == null) || baseFragment.isDetached()) return;

            switch (msg.what) {
                case MAIN_UI_VISIBILITY_GONE:
                    setViewVisibility((View) msg.obj, View.GONE);
                    break;
                case MAIN_UI_VISIBILITY_VISIBLE:
                    setViewVisibility((View) msg.obj, View.VISIBLE);
                    break;
                case MAIN_UI_TOAST:
                    if (msg.obj != null)
                        baseFragment.toast(msg.obj.toString());
                    break;
                case MAIN_UI_REFRESH_ADAPTER:
                    refreshBaseAdapter((BaseAdapter) msg.obj);
                    break;
                case MAIN_UI_DISMISS_P_DIALOG:
                    baseFragment.dismissProgressDialog();
                    break;
                case MAIN_UI_SHOW_P_DIALOG:
                    baseFragment.showProgressDialog((String) msg.obj, false);
                    break;
                case MAIN_UI_SHOW_CANCELABLE_P_DIALOG:
                    baseFragment.showProgressDialog((String) msg.obj, true);
                    break;
                case MAIN_UI_REFRESH_RECYCLER_ADAPTER:
                    refreshRecyclerBaseAdapter((Adapter) msg.obj);
                    break;
                case MAIN_UI_ENABLE_VIEW:
                    enableView((View) msg.obj, true);
                    break;
                case MAIN_UI_DISABLE_VIEW:
                    enableView((View) msg.obj, false);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    }
}
