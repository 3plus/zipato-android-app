/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.View;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter.Command;
import com.zipato.customview.CustomRecyclerView;
import com.zipato.helper.AttributesHelper;
import com.zipato.helper.DeviceStateHelper;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.attribute.AttributeValue;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.device.DeviceState;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.model.event.Event;
import com.zipato.model.scene.SceneRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.TypeFaceUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.ButterFork;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 7/14/2015.
 */
public abstract class ViewController extends ViewHolder {

    public static final long DEFAULT_RESET_DELAY = 5000L;

    private static final String TAG = TagFactoryUtils.getTag(ViewController.class);
    private final RecyclerView recyclerView;
    @Inject
    protected LanguageManager languageManager;
    @Inject
    protected AttributeRepository attributeRepository;
    @Inject
    protected EndpointRepository endpointRepository;
    @Inject
    protected ClusterEndpointRepository clusterEndpointRepository;
    @Inject
    protected TypeFaceUtils typeFaceUtils;
    @Inject
    protected DeviceStateHelper deviceStateHelper;
    @Inject
    protected AttributesHelper attributesHelper;
    @Inject
    AttributeValueRepository attributeValueRepository;
    @Inject
    SceneRepository sceneRepository;
    @Inject
    ExecutorService executor;
    @Inject
    EventBus eventBus;
    @Inject
    Map<TypeReportKey, ArrayMap<String, Object>> vcTypeReportsCache;

    public ViewController(View itemView, RecyclerView recyclerView) {
        super(itemView);
        ButterFork.bind(this, itemView);
        ((ZipatoApplication) recyclerView.getContext().getApplicationContext()).getObjectGraph().inject(this);
        languageManager.translateFields(this);
        typeFaceUtils.applyTypefaceFor(this);
        this.recyclerView = recyclerView;
    }

    protected GenericAdapter getAdapter() {
        if (recyclerView == null)
            return null;
        return (GenericAdapter) recyclerView.getAdapter();
    }

    protected RecyclerView getRecyclerView() {
        return recyclerView;
    }

    protected String attrValueUnitResolver(final UUID attrUUID, final String value) {
        return attributesHelper.attrValueResolver(attrUUID, value);
    }

    protected String attrValueUnitResolver(final UUID attrUUID) {
        return attributesHelper.attrValueResolver(attrUUID);
    }

    protected boolean isStateIconTrue(final UUID attrUUID) {
        return attributesHelper.isStateIconTrue(attrUUID);
    }

    protected boolean isDeviceOnline(TypeReportItem item) {
        return deviceStateHelper.isDeviceOnline(item);
    }

    protected boolean isDeviceTrouble(TypeReportItem item) {
        return deviceStateHelper.isDeviceTrouble(item);
    }

    protected boolean isBatteryLow(TypeReportItem item) {
        return deviceStateHelper.isBatteryLow(item);
    }

    protected DeviceState getDeviceState(TypeReportItem item) {
        return deviceStateHelper.getDeviceState(item);
    }

    public abstract boolean hasLogic(); //if true implement the ViewControllerLogic interface


    public void onBind(Object object) {
        final TypeReportItem typeReportItem = (TypeReportItem) object;
        if (!deviceStateHelper.isDeviceOnline(typeReportItem))
            itemView.setBackgroundColor(getContext().getResources().getColor(R.color.color_view_controller_device_offline));
        else if (deviceStateHelper.isBatteryLow(typeReportItem) || deviceStateHelper.isDeviceTrouble(typeReportItem))
            itemView.setBackgroundColor(getContext().getResources().getColor(R.color.color_view_controller_device_battery_low_trouble));
        else
            itemView.setBackgroundColor(getContext().getResources().getColor(R.color.color_view_controller_item_background));

        dispatchOnBind(object);
    }

    public abstract void dispatchOnBind(Object object);

    protected Context getContext() {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter == null)
            return null;
        return genericAdapter.getContext();
    }

    protected Handler getHandler() {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter == null)
            return null;
        return genericAdapter.getHandler();
    }

    protected void disableAdapterUpdate() {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter != null) genericAdapter.enableUpdate(false);
        else Log.e(TAG, "could not disable adapter update because null");
    }

    protected void disableRecyclerScrolling() {
        if (recyclerView == null)
            return;
        final CustomRecyclerView crv = (CustomRecyclerView) recyclerView;
        crv.setEnableScroll(false);
        eventBus.post(new Event(false, Event.EVENT_TYPE_ENBALE_SWIPE_VIEW_PAGER));
    }

    protected void enableRecyclerScrolling() {
        if (recyclerView == null)
            return;
        final CustomRecyclerView crv = (CustomRecyclerView) recyclerView;
        crv.setEnableScroll(true);
        eventBus.post(new Event(true, Event.EVENT_TYPE_ENBALE_SWIPE_VIEW_PAGER));
    }

    protected void resetAdapterUpdate(long delay) {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter != null) genericAdapter.resetUpdate(delay);
        else Log.e(TAG, "could not reset adapter update because null");
    }

    protected TypeReportItem getTypeReportItem() {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter == null) {
            Log.e(TAG, " adapter == null");
            return null;
        }
        final int position = getAdapterPosition();
        if (position >= 0)
            return genericAdapter.getTypeReportItem(position);
        else Log.e(TAG, "WTF index <0 ???");
        return null;
    }

    protected String getValueForAttr(UUID attrUUID) {
        String value;

        final GenericAdapter genericAdapter = getAdapter();
        if ((genericAdapter != null) && genericAdapter.isSceneMode())
            value = sceneRepository.getSettingValueFor(attrUUID);
        else {
            final AttributeValue attributeValue = attributeValueRepository.get(attrUUID);
            value = ((attributeValue != null) && (attributeValue.getValue() != null)) ? attributeValue.getValue().toString() : null;
        }

        return value;
    }

    public void sendAttributeValue(final UUID key, final String value) {
        sendCommand(new Command(key, value));
        final GenericAdapter genericAdapter = getAdapter();
        if ((genericAdapter != null) && genericAdapter.isSceneMode())
            sceneRepository.putSettingValueFor(key, value);
    }

    public void sendCommand(final Command... command) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (Command cmd : command) {
                    sceneRepository.putSettingValueFor(cmd.key, cmd.value);
                    try {
                        attributeValueRepository.putAttributesValue(cmd.key, cmd.value);
                    } catch (Exception e) {
                        Log.e(TAG, "", e);
                    }
                }
            }
        });
    }

    protected void defaultBlockResetUpdate() {
        disableAdapterUpdate();
        resetAdapterUpdate(DEFAULT_RESET_DELAY);
    }

    protected Attribute getTypeAttributeFor(int attrID, TypeReportItem item) {
        return attributesHelper.getTypeReportAttrFor(attrID, item);
    }

    protected void putToVCCache(TypeReportKey key, String entryKey, Object value) {
        ArrayMap<String, Object> map = vcTypeReportsCache.get(key);
        if (map == null) {
            map = new ArrayMap<>();
            vcTypeReportsCache.put(key, map);
        }

        map.put(entryKey, value);
    }

    protected Object getValueFromVCCache(TypeReportKey key, String entryKey) {
        ArrayMap<String, Object> map = vcTypeReportsCache.get(key);
        if (map == null)
            return null;
        return map.get(entryKey);
    }

    protected boolean isEntryInVCCache(TypeReportKey key, String entryKey) {
        ArrayMap<String, Object> map = vcTypeReportsCache.get(key);

        return (map != null) && map.containsKey(entryKey);
    }

    protected void removeEntryToVCCache(TypeReportKey key, String entryKey) {
        ArrayMap<String, Object> map = vcTypeReportsCache.get(key);
        if (map == null) return;

        map.remove(entryKey);
    }

}
