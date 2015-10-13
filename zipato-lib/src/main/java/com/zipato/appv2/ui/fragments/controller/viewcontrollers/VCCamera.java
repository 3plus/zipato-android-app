/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.B;
import com.zipato.appv2.R;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.activities.BaseCameraActivity;
import com.zipato.appv2.activities.CameraActivity;
import com.zipato.appv2.activities.MjpegStreamActivity;
import com.zipato.appv2.activities.ShowVCMenu;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.appv2.ui.fragments.controller.ViewControllerLogic;
import com.zipato.model.camera.Camera;
import com.zipato.model.camera.CameraRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.Utils;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.Bind;
import butterfork.OnClick;

/**
 * Created by murielK on 8/18/2015.
 */

@ViewType(TypeViewControllerFactory.VC_ID_CAMERA)
public class VCCamera extends AbsHeader implements ViewControllerLogic {

    private static final String TAG = TagFactoryUtils.getTag(VCCamera.class);
    private static final long CAMERA_RE_ENABLE_UPDATE_DELAY = 120000L;

    @Inject
    CameraRepository cameraRepository;
    @Inject
    ExecutorService executor;
    @Inject
    Picasso picasso;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butSD)
    TextView textViewSD;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butHD)
    TextView textViewHD;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butMjpeg)
    TextView textViewMjpeg;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("take_snap_shot")
    @Bind(B.id.butTakSnapshot)
    TextView textViewTakeSnapShot;
    @Bind(B.id.imageViewCam)
    ImageView imageView;

    private int logicID;
    private volatile boolean logicExecuted;

    public VCCamera(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return 0;
    }

    @Override
    public boolean hasLogic() {
        return true;
    }

    private void loadDefaultImg() {
        if (Utils.isPreJellyBean())
            imageView.setBackgroundDrawable(getAdapter().getContext().getResources().getDrawable(drawable.ic_img_fail));
        else
            imageView.setBackground(getAdapter().getContext().getResources().getDrawable(drawable.ic_img_fail));
    }

    private void loadImg(final String url) {
        Log.d(TAG, String.format("url to load: %s", url));
        try {
            picasso.load(url)
                    .fit()
                    .placeholder(drawable.ic_img_empty)
                    .error(drawable.ic_img_fail)
                    .into(imageView);
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        TypeReportItem item = (TypeReportItem) object;
        final Camera camera = cameraRepository.get(item.getUuid());

        if ((camera != null) && (camera.getLastFile() != null) && (camera.getLastFile().getUrl() != null)) {
            loadImg(camera.getLastFile().getUrl());

        } else if ((camera != null) && (camera.getSnapshot() != null)) {
            loadImg(camera.getSnapshot());

        } else {
            loadDefaultImg();
            Log.e(TAG, "null url at dispatchOnBind");
        }

//        if (logicExecuted) {
//            Log.d(TAG, String.format("logic was executed disabling update for the next %d seconds", (CAMERA_RE_ENABLE_UPDATE_DELAY / 1000)));
//            disableAdapterUpdate();
//            resetAdapterUpdate(CAMERA_RE_ENABLE_UPDATE_DELAY);
//        }
    }

    private void startStream(int quality) {
        final GenericAdapter genericAdapter = getAdapter();
        final TypeReportItem item = getTypeReportItem();
        String className = getContext().getResources().getString(R.string.camera_activity_class);
        Class<? extends Activity> clazz;
        try {
            clazz = (Class<? extends Activity>) Class.forName(className);
        } catch (Exception e) {
            Log.e(TAG, "no class " + className);
            clazz = CameraActivity.class;
        }
        Intent intent = new Intent(genericAdapter.getContext(), clazz);
        intent.putExtra(BaseFragment.PARCELABLE_KEY, item.getKey());
        intent.putExtra(BaseCameraActivity.STREAMING_TYPE_KEY, quality);
        genericAdapter.getContext().startActivity(intent);
    }

    @OnClick(B.id.butTakSnapshot)
    public void onSnapShot(final View v) {
        final TypeReportItem item = getTypeReportItem();
        takeSnapshot(item.getUuid());
    }

    @OnClick(B.id.butHD)
    public void onClickHI(final View v) {
        startStream(CameraActivity.HIGH_STREAM);
    }

    @OnClick(B.id.butSD)
    public void onClickLow(final View v) {
        startStream(CameraActivity.LOW_STREAM);
    }

    @OnClick(B.id.butMjpeg)
    public void onClickMjpeg(final View v) {
        final GenericAdapter genericAdapter = getAdapter();
        final TypeReportItem item = getTypeReportItem();
        Intent intent = new Intent(genericAdapter.getContext(), MjpegStreamActivity.class);
        intent.putExtra(BaseFragment.PARCELABLE_KEY, item.getKey());
        intent.putExtra(BaseCameraActivity.STREAMING_TYPE_KEY, BaseCameraActivity.MJPEG);
        genericAdapter.getContext().startActivity(intent);
    }

    private void takeSnapshot(final UUID uuid) {
        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on save method");
            return;
        }
        final Handler handler = getHandler();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraRepository.takeSnapShot(uuid);
                    if (handler != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, languageManager.translate("command_snapshot"), Toast.LENGTH_SHORT).show();
                            }
                        });
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

        });
    }

    @Override
    public void setLogicQueueID(int logicQueueID) {
        logicID = logicQueueID;
    }

    @Override
    public void run() {
        final ThreadLocal<Integer> localLogicID = new ThreadLocal<>();
        localLogicID.set(logicID);
        int viewType = TypeViewControllerFactory.VC_ID_CAMERA;
        final GenericAdapter genericAdapter = getAdapter();
        try {
            cameraRepository.fetchAll();
            cameraRepository.write();
            logicExecuted = true;
            Log.d(TAG, "=== camera logic updater DONE!!! ===");
        } catch (Exception e) {
            logicExecuted = false;
            Log.d(TAG, "", e);
        } finally {
            if (genericAdapter != null)
                genericAdapter.logicExecuted(viewType, true, localLogicID.get());
        }
    }

    @Override
    protected String[] getListMenu() {
        return new String[]{languageManager.translate("archives"), languageManager.translate("configuration"), languageManager.translate("change_icon")};
    }

    @Override
    protected void handleWhichMenu(Context context, TypeReportKey key, int which) {
        switch (which) {
            case 0:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_ARCHIVES);
                break;
            case 1:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_CONFIG);
                break;
            case 2:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_CHANGE_ICON);
                break;
        }
    }

}
