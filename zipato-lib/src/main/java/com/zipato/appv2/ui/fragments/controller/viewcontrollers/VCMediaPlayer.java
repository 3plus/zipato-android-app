/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.controller.ViewControllerLogic;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.client.RestObject;
import com.zipato.model.media.Action;
import com.zipato.model.media.Actions;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by murielK on 8/21/2015.
 */
@ViewType(R.layout.view_controller_media)
public class VCMediaPlayer extends AbsLevel implements ViewControllerLogic {

    private static final String VC_CACHE_ENTRY_ACTIONS = "VC_CACHE_ENTRY_ACTIONS";
    private static final String TAG = TagFactoryUtils.getTag(VCMediaPlayer.class);
    private final ArrayMap<String, TextView> actionViewMap = new ArrayMap<>();

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.buttonPlay)
    TextView butPlay;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.buttonPause)
    TextView butPause;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.buttonStop)
    TextView butStop;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.buttonBack)
    TextView butBack;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.buttonNext)
    TextView butNext;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(R.id.buttonMute)
    TextView butMute;

    @Inject
    ExecutorService executor;
    @Inject
    ApiV2RestTemplate restTemplate;


    private int logicQueueID;

    public VCMediaPlayer(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        init();
    }

    private void init() {
        actionViewMap.put(Actions.play.name(), butPlay);
        actionViewMap.put(Actions.stop.name(), butStop);
        actionViewMap.put(Actions.pause.name(), butPause);
        actionViewMap.put(Actions.prev.name(), butBack);
        actionViewMap.put(Actions.next.name(), butNext);
        actionViewMap.put(Actions.mute.name(), butMute);
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        upgradeUIButtons((TypeReportItem) object);
    }

    private void disableAllButtons() {
        butPlay.setEnabled(false);
        butPause.setEnabled(false);
        butStop.setEnabled(false);
        butBack.setEnabled(false);
        butNext.setEnabled(false);
        butMute.setEnabled(false);
    }

    private void upgradeUIButtons(TypeReportItem item) {
        disableAllButtons();
        try {
            final Action[] actions = (Action[]) getValueFromVCCache(item.getKey(), VC_CACHE_ENTRY_ACTIONS);
            if ((actions == null) || (actions.length == 0))
                return;
            for (Action a : actions) {
                actionViewMap.get(a.getName().name()).setEnabled(true);
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    @OnClick(R.id.buttonPlay)
    public void onClickPlay(View v) {
        handleOnClickEvent(Actions.play);
    }

    @OnClick(R.id.buttonPause)
    public void onClickPause(View v) {
        handleOnClickEvent(Actions.pause);
    }

    @OnClick(R.id.buttonStop)
    public void onClickStop(View v) {
        handleOnClickEvent(Actions.stop);
    }

    @OnClick(R.id.buttonBack)
    public void onClickBack(View v) {
        handleOnClickEvent(Actions.prev);
    }

    @OnClick(R.id.buttonNext)
    public void onClickNext(View v) {
        handleOnClickEvent(Actions.next);
    }

    @OnClick(R.id.buttonMute)
    public void onClickMute(View v) {
        handleOnClickEvent(Actions.mute);
    }


    private void handleOnClickEvent(final Actions action) {
        final TypeReportItem item = getTypeReportItem();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Sending action: " + action.name() + " for player: " + item.getName());
                    RestObject restObject = restTemplate.postForObject("v2/clusterEndpoints/{uuid}/actions/{action}", new HashMap<>(), RestObject.class, item.getUuid(), action.name());
                    Log.d(TAG, String.format("Sending action successful? %s", (restObject != null) && restObject.isSuccess()));
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
            }
        });
    }

    @Override
    protected int getTargetAttrID() {
        return BaseTypesFragment.VOLUME;
    }

    @Override
    protected boolean isCustomUnit() {
        return true;
    }

    @Override
    protected String getCustomUnit(Attribute attr) {
        return "%";
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return ((item == null) || (item.getAttributes() == null)) ? -1 : item.getMasterIndex();
    }

    @Override
    public boolean hasLogic() {
        return true;
    }

    @Override
    public void setLogicQueueID(int logicQueueID) {
        this.logicQueueID = logicQueueID;
    }

    @Override
    public void run() {
        final ThreadLocal<Integer> localLogicID = new ThreadLocal<>();
        localLogicID.set(logicQueueID);
        final GenericAdapter genericAdapter = getAdapter();
        boolean isSuccess = true;
        try {
            Log.d(TAG, "fetching actions for");
            final int itemCount = ((Adapter) genericAdapter).getItemCount();
            for (int i = 0; i < itemCount; i++) {
                final TypeReportItem item = genericAdapter.getTypeReportItem(i);
                if ((item == null) || !"IP_MEDIA_PLAYER".equals(item.getTemplateId()) || isEntryInVCCache(item.getKey(), VC_CACHE_ENTRY_ACTIONS)) {
                    Log.d(TAG, String.format("Skipping item: %s ... probably already loaded", (item == null) ? null : ((item.getName() == null) ? item.getUuid() : item.getName())));
                    continue;
                }
                Log.d(TAG, String.format("fetching actions for item: %s ...", (item.getName() == null) ? item.getUuid() : item.getName()));
                Action[] action = restTemplate.getForObject("v2/clusterEndpoints/{uuid}/actions", Action[].class, item.getUuid());
                putToVCCache(item.getKey(), VC_CACHE_ENTRY_ACTIONS, action);
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
            isSuccess = false;
        } finally {
            if (genericAdapter != null) {
                final int viewTye = R.layout.view_controller_media;
                if (isSuccess)
                    genericAdapter.logicExecuted(viewTye, true, localLogicID.get());
                else genericAdapter.logicFailExecution(viewTye, localLogicID.get());
            }
        }
    }

    @Override
    protected boolean handleMultiAttr() {
        return false;
    }

}
