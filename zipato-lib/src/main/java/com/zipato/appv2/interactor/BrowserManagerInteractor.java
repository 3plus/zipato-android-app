/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.interactor;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.activities.BaseActivity;
import com.zipato.helper.PreferenceHelper;
import com.zipato.model.BaseObject;
import com.zipato.model.alarm.PartitionRepository;
import com.zipato.model.alarm.ZonesRepository;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.box.Box;
import com.zipato.model.camera.CameraRepository;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.network.NetworkRepository;
import com.zipato.model.room.RoomRepository;
import com.zipato.model.room.Rooms;
import com.zipato.model.scene.Scene;
import com.zipato.model.scene.SceneRepository;
import com.zipato.model.scene.SceneSetting;
import com.zipato.model.thermostat.ThermostatRepository;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportRepository;
import com.zipato.model.typereport.UiType;
import com.zipato.translation.LanguageManager;
import com.zipato.util.CollectionUtils;
import com.zipato.util.CollectionUtils.Predicate;
import com.zipato.util.TagFactoryUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 10/6/2015.
 */
public class BrowserManagerInteractor {

    private static final String TAG = TagFactoryUtils.getTag(BrowserManagerInteractor.class);
    private static final String CURRENT_POSITION = "CURRENT_POSITION";
    private static final String CURRENT_ROOM = "CURRENT_ROOM";
    private static final String CURRENT_SCENE_UUID = "CURRENT_SCENE_UUID";
    private static final int FILTER_ID_FAVORITE = -99;
    private final WeakReference<BaseActivity> baseActivityWeakReference;
    @Inject
    List<UiType> uiTypes;
    @Inject
    Map<UiType, List<TypeReportItem>> typeMap;
    @Inject
    @Named("rooms")
    List<TypeReportItem> roomTypes;
    @Inject
    @Named("scenes")
    List<TypeReportItem> scenesTypes;
    @Inject
    List<Rooms> rooms;
    @Inject
    List<Scene> scenes;
    @Inject
    RoomRepository roomsRepository;
    @Inject
    AttributeRepository attributeRepository;
    @Inject
    TypeReportRepository typeReportRepository;
    @Inject
    NetworkRepository networkRepository;
    @Inject
    ThermostatRepository thermostatRepository;
    @Inject
    CameraRepository camerasRepository;
    @Inject
    PartitionRepository partitionRepository;
    @Inject
    SceneRepository sceneRepository;
    @Inject
    ZonesRepository zonesRepository;
    @Inject
    AttributeValueRepository attributeValueRepository;
    @Inject
    DeviceStateRepository deviceStateRepository;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    ExecutorService executor;
    @Inject
    PreferenceHelper preferenceHelper;
    @Inject
    EventBus eventBus;
    @Inject
    LanguageManager languageManager;

    @Nullable
    private UUID currentSceneUUID;

    private volatile boolean loadingRepo;
    private boolean isRefreshing;
    private int currentRoomID;
    private int currentItemPosition = -1;

    public BrowserManagerInteractor(BaseActivity ba) {
        ((ZipatoApplication) ba.getApplication()).inject(this);
        baseActivityWeakReference = new WeakReference<>(ba);
    }

    public void loadRepoRefresh(final boolean reload) {
        isRefreshing = true;
        loadRepo(reload);
    }

    public void loadRepo(final boolean reload) {
        final BaseActivity baseActivity = getActivity();
        if (baseActivity == null)
            return;

        if (reload && !baseActivity.checkInternet()) {
            return;
        }

        loadingRepo = true;

        baseActivity.showIndeterminateProgress(true);

        executor.execute(new Runnable() {
                             @Override
                             public void run() {

                                 boolean success = performLoadRepo(reload);
                                 if (success)
                                     checkAndLoadCurrentSceneSetting(); // in case the activity was killed by the os, we want to reload last selected scenes devices

                                 final Collection<TypeReportItem> roomFilteredResult = performRoomFiltering(currentRoomID);
                                 final Collection<TypeReportItem> sceneSettingResult = getTypesForScene((currentSceneUUID == null) ? null : sceneRepository.get(currentSceneUUID));

                                 baseActivity.runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         setRoomUI(roomFilteredResult);
                                         setUpScenesUI(sceneSettingResult);
                                         eventBus.post(new Event(null, Event.EVENT_TYPE_REPO_SYNCED));
                                         loadingRepo = false;
                                         if (isRefreshing)
                                         isRefreshing = false;
                                         baseActivity.showIndeterminateProgress(false);
                                     }
                                 });


                             }
                         }
        );
    }

    private void checkAndLoadCurrentSceneSetting() {
        if ((currentSceneUUID != null)) {
            final Scene scene = sceneRepository.get(currentSceneUUID);
            if ((scene != null) && (scene.getSettings() == null)) {
                try {
                    performLoadSceneSetting(scene);
                } catch (Exception e) {
                    //Empty
                }
            } else if (scene == null) currentSceneUUID = null;
        }
    }

    private void setRoomUI(Collection<TypeReportItem> filterResults) {
        if (isRefreshing && !filterResults.isEmpty())
            addTypesRoomResults(filterResults);
        else {
            roomTypes.clear();
            eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.ROOM_TYPES), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
        }

        buildRooms();

        notifyRoomView(!isRefreshing);

    }

    private void setUpScenesUI(Collection<TypeReportItem> sceneSettings) {
        if (isRefreshing && !sceneSettings.isEmpty())
            addTypesScenesResults(sceneSettings);
        else {
            scenesTypes.clear();
            eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.SCENES_TYPE), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
        }

        buildScenes();

        notifySceneView(!isRefreshing);

        checkHideAddDeviceOnScenes();
    }

    private void checkHideAddDeviceOnScenes() {
        if (currentSceneUUID == null)
            eventBus.post(new Event(null, Event.EVENT_TYPE_HIDE_ADD_NEW_ON_SCENES));
    }

    private Collection<TypeReportItem> performRoomFiltering(final int filterID) {

        final Collection<TypeReportItem> itemsOut = new ArrayList<>();

        switch (filterID) {
            case RoomRepository.ID_NO_FILTER:
                itemsOut.addAll(typeReportRepository.values());
                break;
            case RoomRepository.ID_UNDEFINED:
                Predicate<TypeReportItem> undefPredicate = new CollectionUtils.Predicate<TypeReportItem>() {
                    @Override
                    public boolean apply(TypeReportItem typeReportItem) {
                        return typeReportItem.getRoom() == 0;

                    }
                };
                CollectionUtils.addMatching(typeReportRepository.values(), itemsOut, undefPredicate);
                break;
            case FILTER_ID_FAVORITE:
                Predicate<TypeReportItem> favPredicate = new CollectionUtils.Predicate<TypeReportItem>() {
                    @Override
                    public boolean apply(TypeReportItem typeReportItem) {

                        return TypeReportRepository.isTypeIsFavorite(typeReportItem, preferenceHelper.getInt(PreferenceHelper.Preference.USER_ID, -1));
                    }
                };

                CollectionUtils.addMatching(typeReportRepository.values(), itemsOut, favPredicate);
                CollectionUtils.addMatching(sceneRepository.valueToTypes(), itemsOut, favPredicate);

                break;
            default:
                Predicate<TypeReportItem> predicate = new CollectionUtils.Predicate<TypeReportItem>() {
                    @Override
                    public boolean apply(TypeReportItem typeReportItem) {
                        return (filterID != 0) && (filterID == typeReportItem.getRoom());

                    }
                };

                CollectionUtils.addMatching(typeReportRepository.values(), itemsOut, predicate);

                break;
        }

        return itemsOut;

    }

    private void onFilter(final int filterID) {
        final BaseActivity baseActivity = getActivity();
        if (baseActivity == null)
            return;

        baseActivity.showIndeterminateProgress(true);
        executor.execute(new Runnable() {
            @Override
            public void run() {  // i will take anything better than this please!!

                final Collection<TypeReportItem> filterResults = performRoomFiltering(filterID);
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    //Empty
                }

                baseActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addTypesRoomResults(filterResults);
                        if (!loadingRepo)
                            baseActivity.showIndeterminateProgress(false);
                    }
                });
            }
        });
    }

    private void addTypesRoomResults(Collection<TypeReportItem> filterResults) {
        roomTypes.clear();
        roomTypes.addAll(filterResults);
        eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.ROOM_TYPES), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
    }

    private void addTypesScenesResults(Collection<TypeReportItem> settingResults) {
        scenesTypes.clear();
        scenesTypes.addAll(settingResults);
        eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.SCENES_TYPE), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
    }

    private boolean performLoadRepo(final boolean reSync) {
        attributeValueRepository.clearETag();
        deviceStateRepository.clearETag();
        boolean sync = preferenceHelper.isRepoLoaded();
        boolean isFail;

        if (!reSync && sync) {
            try {

                restoreRepo();
                updateAttributeTypeLinks(); //Darko idea!? :O

                return true;
            } catch (Exception e) {
                Log.e(TAG, "", e);
                isFail = !syncRepo();
            }
        } else {
            isFail = !syncRepo();
        }

        if (isFail) {
            return false;
        } else {

            updateAttributeTypeLinks();
            storeRepo();

        }

        return true;
    }

    private void buildRooms() {
        rooms.clear();
        rooms.addAll(roomsRepository.values());

        Collections.sort(rooms, BaseObject.ORDER_NAME_COMPARATOR);

        if (!roomsRepository.isEmpty()) {
            Rooms undefined = roomsRepository.get(RoomRepository.ID_UNDEFINED);

            rooms.remove(undefined);
            rooms.add(0, undefined);
        }
    }

    private void buildScenes() {
        scenes.clear();
        for (Scene scene : sceneRepository.values()) {
            if (!(scene.getFlag() == Scene.FLAG_DELETED))
                scenes.add(scene);
        }
    }

    private boolean restoreRepo() {
        try {
            long startTime = System.currentTimeMillis();
            typeReportRepository.restore();
            camerasRepository.restore();
            roomsRepository.restore();
            networkRepository.restoreTree();
            sceneRepository.restore();
            Log.d(TAG, String.format("restored all repo in...%s", ((System.currentTimeMillis() - startTime)) + "ms"));
            return true;
        } catch (Exception e) {
            Log.d(TAG, "", e);
            return false;
        }
    }

    private boolean storeRepo() {
        try {
            Log.d(TAG, "--------- caching repositories -----------");
            long startTime = System.currentTimeMillis();
            networkRepository.write();
            camerasRepository.write();
            roomsRepository.write();
            typeReportRepository.write();
            sceneRepository.write();
            Log.d(TAG, String.format("stored all repo...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
            Log.d(TAG, "--------- caching repositories done ~! -----------");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "", e);
            return false;
        }

    }

    private boolean fetchNetworks() {
        try {
            long startTime = System.currentTimeMillis();
            networkRepository.loadTree();
            Log.d(TAG, String.format("networkRepository loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
        } catch (Exception e) {
            Log.d(TAG, "Fetching Networks fails");
            throw e;
        }
        return true;
    }

    private boolean fetchCameras() {
        try {
            long startTime = System.currentTimeMillis();
            camerasRepository.clear();
            camerasRepository.fetchAll();
            Log.d(TAG, String.format("camerasRepository loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
        } catch (Exception e) {
            Log.d(TAG, "Fetching camerasRepository fails");
            throw e;
        }
        return true;
    }

    private boolean fetchTypes() {
        try {
            long startTime = System.currentTimeMillis();
            typeReportRepository.searchAll();
            Log.d(TAG, String.format("typeReportRepository loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
        } catch (Exception e) {
            Log.d(TAG, "Fetching typeReportRepository fails");
            throw e;
        }
        return true;
    }

    private boolean fetchRooms() {
        try {
            long startTime = System.currentTimeMillis();
            roomsRepository.fetchAll();
            Log.d(TAG, String.format("roomsRepository loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
        } catch (Exception e) {
            Log.d(TAG, "Fetching roomsRepository fails");
            throw e;
        }
        return true;
    }

    private boolean fetchScenes() {
        try {
            long startTime = System.currentTimeMillis();
            sceneRepository.fetchAll();
            Log.d(TAG, String.format("sceneRepository loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
        } catch (Exception e) {
            Log.d(TAG, "Fetching sceneRepository fails");
            throw e;
        }
        return true;
    }

    private void clearWantedRepo() {
        thermostatRepository.clear();
        partitionRepository.clear();
        zonesRepository.clear();
    }

    private boolean saveCurrentBoxSerial() {

        try {
            long startTime = System.currentTimeMillis();
            Box box = restTemplate.getForObject("v2/box", Box.class);
            if ((box != null) && (box.getSerial() != null))
                preferenceHelper.putStringPref(PreferenceHelper.Preference.BOX_SERIAL, box.getSerial());
            Log.d(TAG, String.format("current box loaded...in %s", ((System.currentTimeMillis() - startTime)) + "ms"));
        } catch (Exception e) {
            Log.d(TAG, "Fetching roomsRepository fails");
            throw e;
        }

        return true;
    }

    private boolean syncRepo() {
        final BaseActivity baseActivity = getActivity();
        if (baseActivity == null)
            return false;

        try {
            preferenceHelper.resetRepoSync();
            clearWantedRepo();
            if (fetchNetworks() && fetchTypes()
                    && fetchRooms() && fetchCameras()
                    && fetchScenes()) {
                saveCurrentBoxSerial();
                preferenceHelper.setRepoSync();
                return true;
            }

        } catch (Exception e) {
            baseActivity.handlerException(e, TAG);
        }
        return false;

    }

    private void updateAttributeTypeLinks() {

        for (final TypeReportItem tri : typeReportRepository.values()) {

            if (tri.getEntityType() == EntityType.ATTRIBUTE) {
                final Attribute attribute = attributeRepository.get(tri.getUuid());
                if (attribute != null)
                    attribute.setTypeReportItem(tri);
                continue;
            }

            final Attribute[] attributes = tri.getAttributes();
            if ((attributes == null) || (attributes.length == 0))
                continue;

            for (Attribute attr : attributes) {
                final Attribute attribute = attributeRepository.get(attr.getUuid());
                if (attribute != null) {
                    attribute.setTypeReportItem(tri);
                }
            }
        }
    }

    private void notifyRoomView(boolean flagReset) {
        if (flagReset)
            currentRoomID = 0;

        eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.ROOMS, 0, flagReset), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
    }

    private void notifySceneView(boolean flagReset) {
        if (flagReset)
            currentSceneUUID = null;

        eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.SCENES, 0, flagReset), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
    }

    private void handleOnUiTypesClick(final ObjectItemsClick event) {
        try {
            roomTypes.clear();
            eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.ROOM_TYPES), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
            roomTypes.addAll(typeMap.get(uiTypes.get(event.position)));
            Collections.sort(roomTypes, TypeReportItem.ORDER_NAME_COMPARATOR);
            currentItemPosition = event.position;

            eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.ROOM_TYPES), Event.EVENT_TYPE_LIST_VIEW_REFRESH));

        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    private void handleOnRoomTypesClick(final ObjectItemsClick event) {

    }

    public void handleOnFavClick(ObjectItemsClick objectItemsClick) {
        notifyRoomView(true);
        onFilter(FILTER_ID_FAVORITE);
    }


    private void handleOnRoomClick(final ObjectItemsClick event) {
        currentRoomID = rooms.get(event.position).getId();
        onFilter(rooms.get(event.position).getId());
    }

    private void handleOnSceneClick(final ObjectItemsClick event) {
        final Scene scene = scenes.get(event.position);
        if ((scene.getFlag() == Scene.FLAG_DELETED) && scene.getUuid().equals(currentSceneUUID)) {
            addTypesScenesResults(new ArrayList<TypeReportItem>());
            checkHideAddDeviceOnScenes();

        } else if (!(scene.getFlag() == Scene.FLAG_DELETED)) {
            currentSceneUUID = scene.getUuid();
            if (scene.getSettings() == null)
                loadSceneSetting(scene);
            else
                addTypesScenesResults(getTypesForScene(scene));
        }
    }

    private List<TypeReportItem> getTypesForScene(final Scene scene) {
        List<TypeReportItem> list = new ArrayList<>();
        if ((scene == null) || (scene.getSettings() == null))
            return list;

        for (SceneSetting ss : scene.getSettings()) {
            final Attribute attribute = attributeRepository.get(ss.getAttributeUuid());
            if (attribute == null)
                continue;
            final TypeReportItem typeReportItem = attribute.getTypeReportItem();
            if (typeReportItem == null)
                continue;

            list.add(typeReportItem);
        }
        return list;
    }

    private boolean performLoadSceneSetting(final Scene scene) {
        sceneRepository.fetchOne(scene.getUuid());
        final Scene newScene = sceneRepository.get(scene.getUuid());
        return ((newScene != null) && (newScene.getSettings() != null));

    }

    private void loadSceneSetting(final Scene scene) {
        final BaseActivity baseActivity = baseActivityWeakReference.get();
        if (baseActivity == null)
            return;

        if (!restTemplate.isUseLocal() && !baseActivity.checkInternet())
            return;

        baseActivity.showProgressDialog(languageManager.translate("loading_box"), false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    success = performLoadSceneSetting(scene);
                } catch (Exception e) {
                    success = false;
                } finally {
                    final boolean finalSuccess = success;
                    baseActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finalSuccess) {
                                buildScenes(); // rebuild sceneType with the new loaded scene
                                addTypesScenesResults(getTypesForScene(sceneRepository.get(scene.getUuid())));
                            } else
                                baseActivity.toast(languageManager.translate("unable_to_load_scenes_devices"));
                            baseActivity.dismissProgressDialog();
                        }
                    });
                }
            }
        });
    }

    public void handleOnItemsClickEvent(ObjectItemsClick objectItemsClick) {
        switch (objectItemsClick.fromTo) {
            case ObjectItemsClick.UI_TYPE:
                handleOnUiTypesClick(objectItemsClick);
                break;
            case ObjectItemsClick.ROOM_TYPES:
                handleOnRoomTypesClick(objectItemsClick);
                break;
            case ObjectItemsClick.ROOMS:
                handleOnRoomClick(objectItemsClick);
                break;
            case ObjectItemsClick.SCENES:
                handleOnSceneClick(objectItemsClick);
                break;
            case ObjectItemsClick.FAVORITE_BUTTON:
                handleOnFavClick(objectItemsClick);
                break;
        }
    }

    public void handleOnSceneEvent() {
        roomTypes.clear();
        roomTypes.addAll(sceneRepository.valueToTypes());
        eventBus.post(new Event(new ObjectListRefresh(ObjectItemsClick.ROOM_TYPES), Event.EVENT_TYPE_LIST_VIEW_REFRESH));
        notifyRoomView(true);
    }

    public void changeBox() {
        final BaseActivity baseActivity = getActivity();
        if (baseActivity == null)
            return;

        if (baseActivity.getSlidingMenu().isMenuShowing())
            baseActivity.getSlidingMenu().toggle();

        loadRepo(true);
    }

    public void onShake() {
        final BaseActivity baseActivity = baseActivityWeakReference.get();
        if (baseActivity == null)
            return;

        baseActivity.vibrate(50);
        handleOnSceneEvent();
    }

    public void clearListItems() {
        roomTypes.clear();
        uiTypes.clear();
        rooms.clear();
    }

    public void saveState(Bundle outState) {
        if (!roomTypes.isEmpty()) {
            outState.putInt(CURRENT_ROOM, currentRoomID);
            outState.putInt(CURRENT_POSITION, currentItemPosition);
        }

        if (!scenesTypes.isEmpty()) {
            outState.putSerializable(CURRENT_SCENE_UUID, currentSceneUUID);
        }
    }

    public void onRefreshEvent() {
        final BaseActivity activity = baseActivityWeakReference.get();
        if (activity == null)
            return;

        if (activity.getSlidingMenu().isMenuShowing())
            activity.getSlidingMenu().toggle();
        if (!loadingRepo) {
            loadRepoRefresh(true);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        currentItemPosition = savedInstanceState.getInt(CURRENT_POSITION);
        currentRoomID = savedInstanceState.getInt(CURRENT_ROOM);
        try {
            currentSceneUUID = (UUID) savedInstanceState.getSerializable(CURRENT_SCENE_UUID);
        } catch (Exception e) {
            // Empty
        }
    }

    private BaseActivity getActivity() {
        return baseActivityWeakReference.get();
    }
}
