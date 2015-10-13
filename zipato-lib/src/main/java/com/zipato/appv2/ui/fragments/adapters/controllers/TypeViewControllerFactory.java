/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.controllers;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zipato.annotation.ViewType;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCBlindRoller;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCCamera;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCDefault;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCEnumButtons;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCITach;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCLevel;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCMediaPlayer;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOnOff;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOsRamRGB;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOsRamRGBW;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOsRamTemp;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCPhilipsHue;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCRemotec;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCScenes;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCSecurity;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCThermostat;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCWeather;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCZipaRGBW;
import com.zipato.model.Constant;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by murielK on 7/14/2015.
 */
public final class TypeViewControllerFactory {

    public static final int VC_ID_DEFAULT = 0;
    public static final int VC_ID_LEVEL = 1;
    public static final int VC_ID_BLINDER = 2;
    public static final int VC_ID_CAMERA = 3;
    public static final int VC_ID_ON_OFF = 4;
    public static final int VC_ID_VC_ENUM_BUTTONS = 5;
    public static final int VC_ID_I_TACH = 6;
    public static final int VC_ID_MEDIA_PLAYER = 7;
    public static final int VC_ID_OS_RAM_RGB = 8;
    public static final int VC_ID_OS_RAM_RGBW = 9;
    public static final int VC_ID_OS_RAM_TEMP = 10;
    public static final int VC_ID_PHILIPS_HUE = 11;
    public static final int VC_ID_REMOTEC = 12;
    public static final int VC_ID_SCENES = 13;
    public static final int VC_ID_SECURITY = 14;
    public static final int VC_ID_THERMOSTAT = 15;
    public static final int VC_ID_WEATHER = 16;
    public static final int VC_ID_ZIPATO_RGBW = 17;

    private static final String TAG = TagFactoryUtils.getTag(TypeViewControllerFactory.class);
    private static final Map<String, Class<? extends ViewController>> VIEW_CONTROLLER_MAP = new HashMap<>();
    private static final Map<Integer, Class<? extends ViewController>> VIEW_TYPE_VIEW_HOLDER_MAP = new HashMap<>();
    private static final SparseIntArray VIEW_ID_RES_MAP = new SparseIntArray();

    private static HashMap<UUID, Integer> cache;

    static {
        //TODO cluster class like sensor can be added in the map to speedup things???
        VIEW_CONTROLLER_MAP.put("com.zipato.cluster.OnOff", VCOnOff.class);//
        VIEW_CONTROLLER_MAP.put("com.zipato.cluster.LevelControl", VCLevel.class);//ZIPA_RGBW
        VIEW_CONTROLLER_MAP.put("ZIPA_RGBW", VCZipaRGBW.class);
        VIEW_CONTROLLER_MAP.put("RGBW", VCZipaRGBW.class);
        VIEW_CONTROLLER_MAP.put("HUE_CONTROL", VCPhilipsHue.class);
        VIEW_CONTROLLER_MAP.put("ZIGBEE_RGBW", VCOsRamRGBW.class);
        VIEW_CONTROLLER_MAP.put("ZIPA_ALARM", VCSecurity.class);
        VIEW_CONTROLLER_MAP.put("CAMERA_PTZ", VCCamera.class);
        VIEW_CONTROLLER_MAP.put("CAMERA", VCCamera.class);
        VIEW_CONTROLLER_MAP.put("REMOTE", VCRemotec.class);
        VIEW_CONTROLLER_MAP.put("ITACH", VCITach.class);
        VIEW_CONTROLLER_MAP.put("ENUM_BUTTONS", VCEnumButtons.class);
        VIEW_CONTROLLER_MAP.put("ENUM_BUTTONS_STATELESS", VCEnumButtons.class);
        VIEW_CONTROLLER_MAP.put("IP_MEDIA_PLAYER", VCMediaPlayer.class);
        VIEW_CONTROLLER_MAP.put("LEVEL_ROLLERSHUTTER", VCBlindRoller.class);
        VIEW_CONTROLLER_MAP.put("LEVEL_BLINDS", VCBlindRoller.class);
        VIEW_CONTROLLER_MAP.put("VIRTUAL_WEATHER", VCWeather.class);
        VIEW_CONTROLLER_MAP.put("THERMOSTAT", VCThermostat.class);
        VIEW_CONTROLLER_MAP.put("BULB_DIM_TEMP", VCOsRamTemp.class);
        VIEW_CONTROLLER_MAP.put("BULB_DIM_RGB", VCOsRamRGB.class);
        VIEW_CONTROLLER_MAP.put(Constant.SCENE_FAKE_TEMPLATE_ID, VCScenes.class); //
    }

    static {
        // this is to avoid the expensive getIdentifier
        VIEW_ID_RES_MAP.put(VC_ID_ON_OFF, R.layout.view_controller_state);
        VIEW_ID_RES_MAP.put(VC_ID_LEVEL, R.layout.view_controller_level);
        VIEW_ID_RES_MAP.put(VC_ID_BLINDER, R.layout.view_controller_level);
        VIEW_ID_RES_MAP.put(VC_ID_CAMERA, R.layout.view_controller_camera);
        VIEW_ID_RES_MAP.put(VC_ID_VC_ENUM_BUTTONS, R.layout.view_controller_enum_buttons);
        VIEW_ID_RES_MAP.put(VC_ID_I_TACH, R.layout.view_controller_ir);
        VIEW_ID_RES_MAP.put(VC_ID_MEDIA_PLAYER, R.layout.view_controller_media);
        VIEW_ID_RES_MAP.put(VC_ID_OS_RAM_RGB, R.layout.view_controller_rgb_zigbee);
        VIEW_ID_RES_MAP.put(VC_ID_OS_RAM_RGBW, R.layout.view_controller_rgbw_zigbee);
        VIEW_ID_RES_MAP.put(VC_ID_OS_RAM_TEMP, R.layout.view_controller_temp_zigbee);
        VIEW_ID_RES_MAP.put(VC_ID_PHILIPS_HUE, R.layout.view_controller_rgb_hue);
        VIEW_ID_RES_MAP.put(VC_ID_REMOTEC, R.layout.view_controller_ir);
        VIEW_ID_RES_MAP.put(VC_ID_SCENES, R.layout.view_controller_scenes);
        VIEW_ID_RES_MAP.put(VC_ID_SECURITY, R.layout.view_controller_security);
        VIEW_ID_RES_MAP.put(VC_ID_THERMOSTAT, R.layout.view_controller_thermostat);
        VIEW_ID_RES_MAP.put(VC_ID_WEATHER, R.layout.view_controller_weather_station);
        VIEW_ID_RES_MAP.put(VC_ID_ZIPATO_RGBW, R.layout.view_controller_rgbw_zipato);
        VIEW_ID_RES_MAP.put(VC_ID_DEFAULT, R.layout.view_controller_default); //
    }

    static {
        VIEW_TYPE_VIEW_HOLDER_MAP.put(VC_ID_DEFAULT, VCDefault.class);
    }

    private TypeViewControllerFactory() {
    }

    public static Class<? extends ViewController> getCachedVCCls(int viewType) {
        return VIEW_TYPE_VIEW_HOLDER_MAP.get(viewType);
    }

    public static <T extends ViewController> T getViewHolder(ViewGroup viewGroup, int viewType, RecyclerView recyclerView) {
        Log.d(TAG, String.format("getViewHolder for viewType: %d", viewType));
            try {
                Class<? extends ViewController> cls = VIEW_TYPE_VIEW_HOLDER_MAP.get(viewType);
                Log.d(TAG, String.format("found viewHolder for viewType: %d, viewHolder name = %s", viewType, cls.getSimpleName()));
                Constructor<T> constructor = (Constructor<T>) cls.getConstructor(View.class, RecyclerView.class);
                final int resID = VIEW_ID_RES_MAP.get(viewType);
                return constructor.newInstance(LayoutInflater.from(viewGroup.getContext()).inflate(resID, viewGroup, false), recyclerView);
            } catch (Exception e) {
                Log.d(TAG, "Moderfucker!", e);
                return (T) new VCDefault(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_controller_default, viewGroup, false), recyclerView);
            }
    }

    private static int findViewType(String key) {
        Class<? extends ViewController> cls = VIEW_CONTROLLER_MAP.get(key);
        final int viewType = cls.getAnnotation(ViewType.class).value();
        if (!VIEW_TYPE_VIEW_HOLDER_MAP.containsKey(viewType))
        VIEW_TYPE_VIEW_HOLDER_MAP.put(viewType, cls);
        return viewType;
    }

    public static <T extends ViewController> void onViewRecycled(T t) {
        Log.d(TAG, String.format("onViewRecycled: Recycling %s from VIEW_TYPE_MAP", t.getClass().getSimpleName()));
//        if (VIEW_TYPE_MAP.containsValue(t.getClass())) {+
//            final int viewType = t.getClass().getAnnotation(ViewType.class).value();
//            if ((cache != null) && cache.containsValue(viewType))
//                cache.entrySet().remove(viewType);
//            VIEW_TYPE_MAP.entrySet().remove(t.getClass());
//        }
    }

    public static int getViewType(TypeReportItem item, Map attributeRepository) {
        if (item == null)
            return VC_ID_DEFAULT;

        Log.d(TAG, String.format("item name = %s entityType %s", item.getName(), item.getEntityType()));

        if (item.getEntityType() == EntityType.ATTRIBUTE) {// in case the controller it is an attribute itself
            try {
                final Attribute attribute = (Attribute) attributeRepository.get(item.getUuid());
                final int viewType = findViewType(attribute.getDefinition().getCluster());
                Log.d(TAG, String.format("item  viewType found = %d", viewType));
                return viewType;
            } catch (Exception e) {
                //
                return VC_ID_DEFAULT;
            }
        }

        if ((item.getTemplateId() != null) && !item.getTemplateId().isEmpty()) {
            Log.d(TAG, String.format("item  has templateID = %s", item.getTemplateId()));

            try {
                final int viewType = findViewType(item.getTemplateId());
                Log.d(TAG, String.format("item  viewType found = %d", viewType));
                return viewType;
            } catch (Exception e) {
                return VC_ID_DEFAULT;

            }
        }

        if (item.getAttributes() == null)
            return VC_ID_DEFAULT;

        //look on cache to speedup attributes look up!
        if ((cache != null) && cache.containsKey(item.getUuid())) {
            Log.d(TAG, String.format("item  cached viewType found = %d", cache.get(item.getUuid())));
            return cache.get(item.getUuid());
        }

        for (Attribute attr : item.getAttributes()) {
            try {
                final Attribute attribute = (Attribute) attributeRepository.get(attr.getUuid());
                final int viewType = findViewType(attribute.getDefinition().getCluster());
                Log.d(TAG, String.format("item  viewType found = %d", viewType));
                if (viewType != 0) {
                    if (cache == null)
                        cache = new HashMap<>(); // it is threadsafe as it will and should always be access via mainThread
                    cache.put(item.getUuid(), viewType);
                    return viewType;
                }
            } catch (Exception e) {
                // empty
            }
        }

        return VC_ID_DEFAULT;
    }


}
