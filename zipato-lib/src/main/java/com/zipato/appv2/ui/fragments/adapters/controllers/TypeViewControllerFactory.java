/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.controllers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zipato.annotation.ViewType;
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

    private static final String TAG = TagFactoryUtils.getTag(TypeViewControllerFactory.class);

    private static final Map<String, Class<? extends ViewController>> VIEW_CONTROLLER_MAP = new HashMap<>();
    private static final Map<Integer, Class<? extends ViewController>> VIEW_TYPE_MAP = new HashMap<>();
    private static HashMap<UUID, Integer> cache;

    public static int defaultViewType;

    static {

        /* this is not going to be very good idea with a huge map (size = 20-30+)but there is much more chances that a user do not have that much types of devices or that much
         types of devices in the same list than having the ViewPool (RecyclerViewPool) swapping in and out ViewHolders
         (which will lead to something like constantly creating the ViewHolder every time a viewHolder is require)
          */

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
        VIEW_TYPE_MAP.put(defaultViewType, VCDefault.class);
    }


    private TypeViewControllerFactory() {
    }

    public static void setContext(Context context) {
        if (defaultViewType == 0) {
            defaultViewType = idOf(context, "view_controller_default");
        }
    }

    public static Class<? extends ViewController> getCachedVCCls(int viewType) {
        return VIEW_TYPE_MAP.get(viewType);
    }

    public static <T extends ViewController> T getViewHolder(ViewGroup viewGroup, int viewType, RecyclerView recyclerView) {
        Log.d(TAG, String.format("getViewHolder for viewType: %d", viewType));
        if (VIEW_TYPE_MAP.containsKey(viewType)) {
            Class<? extends ViewController> cls = VIEW_TYPE_MAP.get(viewType);
            Log.d(TAG, String.format("found viewHolder for viewType: %d, viewHolder name = %s", viewType, cls.getSimpleName()));
            try {
                Constructor<T> constructor = (Constructor<T>) cls.getConstructor(View.class, RecyclerView.class);
                return constructor.newInstance(LayoutInflater.from(viewGroup.getContext()).inflate(viewType, viewGroup, false), recyclerView);
            } catch (Exception e) {
                Log.d(TAG, "Moderfucker!", e);
            }
        }
        return (T) new VCDefault(LayoutInflater.from(viewGroup.getContext()).inflate(defaultViewType, viewGroup, false), recyclerView);
    }

    private static int findViewType(Context context, String key) {
        Class<? extends ViewController> cls = VIEW_CONTROLLER_MAP.get(key);
        final int viewType = idOf(context, cls.getAnnotation(ViewType.class).value());
        if (viewType == 0)
            throw new IllegalStateException("Invalid resource ID for a layout (ViewType) please make sure you properly use @ViewType(R.layout.example) in your class type to pass the res ID of the desire layout");
        VIEW_TYPE_MAP.put(viewType, cls);
        return viewType;
    }

    public static int idOf(Context context, String viewType) {
        int id = context.getResources().getIdentifier(viewType, "layout", context.getPackageName());
        if (id == 0) {
            Log.e(TAG, "could not find resource id." + viewType);
            // this will crash sooner or later
        }
        return id;
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

    public static int getViewType(Context context, TypeReportItem item, Map attributeRepository) {
        if (item == null)
            return defaultViewType;

        Log.d(TAG, String.format("item name = %s entityType %s", item.getName(), item.getEntityType()));

        if (item.getEntityType() == EntityType.ATTRIBUTE) {// in case the controller it is an attribute itself
            try {
                final Attribute attribute = (Attribute) attributeRepository.get(item.getUuid());
                final int viewType = findViewType(context, attribute.getDefinition().getCluster());
                Log.d(TAG, String.format("item  viewType found = %d", viewType));
                return viewType;
            } catch (Exception e) {
                //
                return defaultViewType;
            }
        }

        if ((item.getTemplateId() != null) && !item.getTemplateId().isEmpty()) {
            Log.d(TAG, String.format("item  has templateID = %s", item.getTemplateId()));

            try {
                final int viewType = findViewType(context, item.getTemplateId());
                Log.d(TAG, String.format("item  viewType found = %d", viewType));
                return viewType;
            } catch (Exception e) {
                return defaultViewType;

            }
        }

        if (item.getAttributes() == null)
            return defaultViewType;

        //look on cache to speedup attributes look up!
        if ((cache != null) && cache.containsKey(item.getUuid())) {
            Log.d(TAG, String.format("item  cached viewType found = %d", cache.get(item.getUuid())));
            return cache.get(item.getUuid());
        }

        for (Attribute attr : item.getAttributes()) {
            try {
                final Attribute attribute = (Attribute) attributeRepository.get(attr.getUuid());
                final int viewType = findViewType(context, attribute.getDefinition().getCluster());
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

        return defaultViewType;
    }

}
