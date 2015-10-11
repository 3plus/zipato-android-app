/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.cameras;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.zipato.appv2.B;import com.zipato.appv2.R;

import java.lang.reflect.Method;

/**
 * Created by murielK on 2/3/2015.
 */
public class PhotoController implements View.OnClickListener {

    private static final int SHOW_POPUP = 0;
    private static final int HIDE_POPUP = 1;
    private static final String TAG = PhotoController.class.getSimpleName();
    private long delay = 5000L; // time out in millis of course
    private Activity activity;
    private Handler handler;
    private View photoController;
    private PopupWindow window;
    private ImageButton delete;
    private Button download;
    private OnPhotoControlListner listner;


    private PhotoController(Activity activity, OnPhotoControlListner listner, long delay) {
        init(activity);
        this.listner = listner;
        this.delay = delay;
    }

//    public static PhotoController getDefault(Activity activityIn) {// in case;
//
//        if ((singleton == null)) {
//            synchronized (PhotoController.class) {
//                if (singleton == null) {
//                    singleton = new PhotoController(activityIn);
//                }
//            }
//        }
//        return singleton;
//
//    }

    private void init(Activity activityIn) {
        activity = activityIn;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SHOW_POPUP:
                        show(delay);
                        break;
                    case HIDE_POPUP:
                        hide(0L);
                        break;
                    default:
                        super.handleMessage(msg);
                        break;
                }
            }
        };
    }

    private void initPopUp() {
        initPhotoView();
        window = new PopupWindow(photoController, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable(false);
        window.setBackgroundDrawable(null);
        window.setOutsideTouchable(true);
    }

    protected void initPhotoView() { //can extend this class and override this method to provide your own view, just override the onclick and register to this fo onclick even

        photoController = LayoutInflater.from(activity).inflate(R.layout.photo_controller, null);
        delete = (ImageButton) photoController.findViewById(R.id.imageButtonDel);
        delete.setOnClickListener(this);


    }

    private boolean checkInit() {
        return (window == null) || (photoController == null);
    }


    public void showAt(long atTime) {

        handler.removeMessages(SHOW_POPUP);
        handler.sendEmptyMessageAtTime(SHOW_POPUP, atTime);

    }

    public void show() {
        handler.removeMessages(SHOW_POPUP);
        handler.sendEmptyMessage(SHOW_POPUP);
    }

    public void show(long timeOut) {

        if (isShowing()) {
            handler.removeMessages(HIDE_POPUP);
            handler.sendEmptyMessageDelayed(HIDE_POPUP, timeOut);
            return;
        }

        if (checkInit())
            initPopUp();
        if (activity == null)
            return;
        window.setAnimationStyle(R.style.popupAnimation);
        setWindowLayoutType();
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content));
        int[] location = new int[2];
        viewGroup.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + viewGroup.getWidth(), location[1] + viewGroup.getHeight());
        window.showAtLocation(viewGroup, Gravity.NO_GRAVITY, rect.left, rect.top);
        if (listner != null)
            listner.onShow();
        if (timeOut != 0) {
            handler.removeMessages(HIDE_POPUP);
            handler.sendEmptyMessageDelayed(HIDE_POPUP, timeOut);
        }
    }

    public void setListner(OnPhotoControlListner listner) {
        this.listner = listner;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setWindowLayoutType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            try {
                photoController.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                Method setWindowLayoutType = PopupWindow.class.getMethod("setWindowLayoutType", new Class[]{int.class});
                setWindowLayoutType.invoke(window, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);

            } catch (Exception e) {
                android.util.Log.e(TAG, "", e);
            }
        }
    }

    public void hide() {
        hide(0L);
    }


    public boolean isShowing() {

        return (window != null) && window.isShowing();
    }

    public void hide(long delay) {
        if (delay != 0) {
            handler.removeMessages(HIDE_POPUP);
            handler.sendEmptyMessageAtTime(HIDE_POPUP, delay);
            return;
        }
        if ((window == null) || (photoController == null) || !window.isShowing())
            return;
        try {
            window.dismiss();
            if (listner != null)
                listner.onHide();
        } catch (Exception e) {

            Log.d(TAG, ",e");
        }
    }

    @Override
    public void onClick(View v) {
        if (listner == null)
            return;
        final int id = v.getId();
        if (id == R.id.imageButtonDel) {
            listner.onDeleteClick();
        }
    }

    public interface OnPhotoControlListner {
        void onDeleteClick();

        void onDownloadClick();

        void onShow();

        void onHide();
    }

    public static final class Builder {

        private final Activity activity;
        private OnPhotoControlListner listner;
        private long delay = 5000L;

        public Builder(Activity activity) {
            if (activity == null)
                throw new IllegalArgumentException("activity could not be null");
            this.activity = activity;
        }

        public Builder setListner(OnPhotoControlListner listner) {
            if (this.listner != null)
                throw new IllegalStateException("already added listner");
            this.listner = listner;
            return this;
        }

        public Builder setDelay(long delay) {
            this.delay = delay;
            return this;
        }

        public PhotoController build() {

            return new PhotoController(activity, listner, delay);
        }

    }
}
