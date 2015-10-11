/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.vcmenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;
import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.BaseObject;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.Endpoint;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectConnectivity;
import com.zipato.model.room.RoomRepository;
import com.zipato.model.room.Rooms;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.UiType;
import com.zipato.model.types.SystemTypes;
import com.zipato.model.types.UserIcons;
import com.zipato.util.CollectionUtils;
import com.zipato.util.CollectionUtils.Predicate;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.inject.Inject;

import butterfork.Bind;
import butterfork.OnClick;

/**
 * Created by murielK on 7/29/2014.
 */
public class ConfigFragment extends BaseTypesFragment {


    private static final String TAG = ConfigFragment.class.getSimpleName();

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("room")
    @Bind(B.id.textViewRoom)
    protected TextView textViewRoom;

    @Bind(B.id.spinnerRoom)
    protected Spinner spinnerRoom;
    @Bind(B.id.spinnerType)
    protected Spinner spinnerType;

    @SetTypeFace("helvetica_neue_light.otf")
    @Bind(B.id.editTextTypeName)
    protected EditText editTextTypeName;
    @SetTypeFace("helvetica_neue_light.otf")
    @Bind(B.id.editTextTypeDesc)
    protected EditText editTextTypeDesc;

    @Bind(B.id.switchHidden)
    protected Switch switchHidden;
    @Bind(B.id.switchMaster)
    protected Switch switchMaster;
    @Bind(B.id.switchShow)
    protected Switch switchShow;
    @SetTypeFace("helvetica_neue_light.otf")
    @Bind(B.id.textViewDeviceValue)
    protected TextView textViewDeviceValue;
    @SetTypeFace("helvetica_neue_light.otf")
    @Bind(B.id.textViewEntityTypeVal)
    protected TextView textViewEntityTypeVal;
    @SetTypeFace("helvetica_neue_light.otf")
    @Bind(B.id.textViewUUIDValue)
    protected TextView textViewUUIDValue;

    @Bind(B.id.progressBar)
    protected ProgressBar progressBar;
    @Bind(B.id.scrollView)
    protected ObservableScrollView scrollView;
    protected boolean isStarted;
    @Inject
    protected RoomRepository roomRepository;
    @Inject
    protected EndpointRepository endpointRepository;
    @Inject
    protected ClusterEndpointRepository clusterEndpointRepository;
    @Inject
    PreferenceHelper preferenceHelper;
    @Bind(B.id.frameDevice)
    FrameLayout frameDevice;
    @Bind(B.id.frameRoom)
    FrameLayout frameRoom;
    @Bind(B.id.frameType)
    FrameLayout frameDType;
    @Bind(B.id.frameMaster)
    FrameLayout frameMaster;
    @Bind(B.id.frameDescription)
    FrameLayout frameDescription;
    @Inject
    ApiV2RestTemplate restTemplate;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    //@Translated("save")
    @Bind(B.id.button2)
    FloatingActionButton buttonSave;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("type")
    @Bind(B.id.textViewType)
    TextView textViewType;
    @Translated("name")
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewTypeName)
    TextView textViewTypeName;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("description")
    @Bind(B.id.textViewTypeDesc)
    TextView textViewTypeDesc;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("hidden")
    @Bind(B.id.textViewHidden)
    TextView textViewHidden;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("show")
    @Bind(B.id.textViewShow)
    TextView textViewShow;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("device")
    @Bind(B.id.textViewTypeDevName)
    TextView textViewTypeDevName;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("entity_type")
    @Bind(B.id.textViewEntityType)
    TextView textViewEntityType;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("uuid")
    @Bind(B.id.textViewUUID)
    TextView textViewUUID;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("master")
    @Bind(B.id.textViewMaster)
    TextView textViewMaster;
    RoomListAdapter roomAdapter;
    // IconAdapter iconAdapter;
    TypeListAdapter typeAdapter;
    @Inject
    TypeFaceUtils typeFaceUtils;
    private String typeValue = "";
    private List<Rooms> roomList;
    private List<SystemTypes> systemTypesList;
    private List<UserIcons> userIconsList;
    private int typeIndex;

    @Override
    protected int getResourceView() {
        return R.layout.fragment_config;
    }

    @Override
    protected void onPostViewCreate() {
        typeFaceUtils.applyTypefaceFor(this);
        languageManager.translateFields(this);
        roomAdapter = new RoomListAdapter();
        typeAdapter = new TypeListAdapter();
        //iconAdapter = new IconAdapter();
        roomList = new ArrayList<>();
        systemTypesList = new ArrayList<>();
        userIconsList = new ArrayList<>();
        spinnerRoom.setAdapter(roomAdapter);
        spinnerType.setAdapter(typeAdapter);
        //spinnerIcon.setAdapter(iconAdapter);
        spinnerRoom.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1)
                    createRoomInit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonSave.attachToScrollView(scrollView);

    }

    protected String endPointType() {
        String temp;
        if ((getItem().getUiType() != null) && (getItem().getUiType().getEndpointType() != null)) {
            temp = getItem().getUiType().getEndpointType();
        } else {
            temp = getItem().getEndpointType();
        }
        return temp;
    }

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle saveInstaceState) {
        super.onActivityCreated(saveInstaceState);
        removeViewCE();
        init();
    }


    @OnClick(B.id.button2)
    public void onSaveClick(View v) {
        onSave();
    }


    protected void init() {
        if (isDetached() || !restTemplate.isAuthenticated() || !checkInternet()) {
            scrollView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        isStarted = true;
        executor.execute(new Runnable() {
            @Override
            public void run() {

                SystemTypes[] stArray = null;
                UserIcons[] uiArray = null;
                try {
                    stArray = restTemplate.getForObject("v2/types/system/?x=endpointType", SystemTypes[].class);
                    uiArray = restTemplate.getForObject("v2/types/user/?x=relativeUrl", UserIcons[].class);
                    if (roomRepository.isEmpty())
                        roomRepository.fetchAll();
                } catch (Exception e) {
                    handlerException(e, TAG);

                } finally {
                    isStarted = false;
                    if (!notOk(stArray, uiArray))
                        collectData(stArray, uiArray);
                    else
                        sendMessage(MAIN_UI_VISIBILITY_GONE, progressBar);
                }
            }

        });


    }

    protected void collectData(SystemTypes[] starray, UserIcons[] uiarray) {
        // synchronized (LOCK) {
        systemTypesList.clear();
        userIconsList.clear();
        roomDataListGen();
        final Predicate<SystemTypes> predicate = new Predicate<SystemTypes>() {
            @Override
            public boolean apply(SystemTypes systemTypes) {
                if (systemTypes.getEndpointType() == null)
                    return false;
                String[] tempPredicate;
                try {
                    tempPredicate = systemTypes.getEndpointType().split("\\.");
                } catch (NullPointerException e) {
                    return false;
                }
                return (typeValue != null) && ((tempPredicate.length > 2) ? typeValue.equals(tempPredicate[typeIndex]) :
                        ((tempPredicate.length > 0) && typeValue.equals(tempPredicate[0])));
            }

        };
        String endPointType = endPointType();

        try {
            String[] temp = endPointType.split("\\.");

            if (temp.length > 2) {
                typeIndex = temp.length - 2;
                typeValue = temp[typeIndex];
            } else if (temp.length > 1) {
                typeValue = temp[0];
            } else {
                typeValue = endPointType;
            }
        } catch (Exception e) {
            typeValue = endPointType;
        }

        CollectionUtils.addMatching(Arrays.asList(starray), systemTypesList, predicate);
        userIconsList.addAll(Arrays.asList(uiarray));
        UserIcons emptyUserIcon = new UserIcons();
        emptyUserIcon.setName("-");
        userIconsList.add(0, emptyUserIcon);
        SystemTypes emptySystem = new SystemTypes();
        emptySystem.setEndpointType("-");
        emptySystem.setName("-");
        systemTypesList.add(0, emptySystem);
        sendMessage(MAIN_UI_VISIBILITY_VISIBLE, scrollView);
        sendMessage(MAIN_UI_VISIBILITY_GONE, progressBar);
        baseFragmentHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    setViews();
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
            }
        });
        // }
    }

    protected boolean notOk(SystemTypes[] stArray,
                            UserIcons[] uiArray) {
        return ((stArray == null) || (uiArray == null) || (getItem() == null));
    }

    protected EntityType getEntityType() {
        return getItem().getEntityType();
    }

    private void removeViewCE() {
        if ((getEntityType() == EntityType.DEVICE)) {
            frameDevice.setVisibility(View.GONE);
            return;
        }
        if (getEntityType() == EntityType.ATTRIBUTE) {
            frameDescription.setVisibility(View.GONE);
            frameDType.setVisibility(View.GONE);
            frameMaster.setVisibility(View.VISIBLE);
            frameRoom.setVisibility(View.GONE);
        }
    }

    protected boolean setBoolToRefresh() {
        return true;
    }

    private void onSave() {
        if (!checkInternet())
            return;
        showProgressDialog(languageManager.translate("saving_configurations"), false);
        executor.execute(new Runnable() {
            @Override
            public void run() {

                final boolean success = sendSavedSettings();

                if (success) {

                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateItemConfig();
                            eventBus.post(new Event(null, Event.EVENT_TYPE_REFRESH_REQUEST));
                            if (setBoolToRefresh())
                                preferenceHelper.putBooleanPref(Preference.REFRESH_ON_RESUME, true);
                        }
                    });
                }
                baseFragmentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!success && !isDetached()) {
                            setViews();
                            toast(languageManager.translate("saving_config_fail"));
                        }
                        dismissProgressDialog();
                    }
                });

            }


        });

    }


    protected void updateItemConfig() {
        if (editTextTypeName != null)
            getItem().setName(editTextTypeName.getText().toString());
        if (editTextTypeDesc != null)
            getItem().setDescription(editTextTypeDesc.getText().toString());
        getItem().setShow(switchShow.isSelected());
        if (spinnerType.getSelectedItemPosition() != 0) {
            if (getItem().getUiType() != null)
                getItem().getUiType().setEndpointType(systemTypesList.get(spinnerType.getSelectedItemPosition()).getEndpointType());
            getItem().setEndpointType(systemTypesList.get(spinnerType.getSelectedItemPosition()).getEndpointType());
        }
//        if (spinnerIcon.getSelectedItemPosition() != 0) {
//            getItem().setUserIcon(userIconsList.get(spinnerIcon.getSelectedItemPosition()));
//        }
        if (spinnerRoom.getSelectedItemPosition() > 1) {
            getItem().setRoom((roomList.get(spinnerRoom.getSelectedItemPosition()).getId()));
        }
    }

    protected UUID getUUID() {
        return getItem().getUuid();
    }

    private boolean sendSavedSettings() {
        Map<String, Object> confiData = new HashMap<String, Object>();
        if (editTextTypeName.getText() != null)
            confiData.put("name", editTextTypeName.getText().toString());

        if ((editTextTypeDesc.getText() != null) && ((getEntityType() != EntityType.CLUSTER_ENDPOINT) && (getEntityType() != EntityType.ATTRIBUTE)))
            confiData.put("description", editTextTypeDesc.getText().toString());

        confiData.put("hidden", switchHidden.isChecked());

        if ((spinnerRoom.getSelectedItemPosition() > 1) && (getEntityType() != EntityType.ATTRIBUTE))
            confiData.put("room", String.valueOf(roomList.get(spinnerRoom.getSelectedItemPosition()).getId()));

        else if (getEntityType() != EntityType.ATTRIBUTE)
            confiData.put("room", null);

        if (getEntityType() == EntityType.ATTRIBUTE)
            confiData.put("master", switchMaster.isChecked());

        switch (getEntityType()) {
            case ENDPOINT:
                try {
                    String endpointsQuery = queryBuilder("endpoints", getUUID().toString());
                    if (endpointsQuery != null) {
                        ResponseEntity resp = restTemplate.postForEntity(endpointsQuery, null, ResponseEntity.class);
                        if (!((resp.getStatusCode().value() >= 200) && (resp.getStatusCode().value() < 300)))//todo check that shit
                            return false;
                    }
                    restTemplate.put("v2/endpoints/{uuid}/config", confiData, getUUID());

                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }
                return true;
            case CLUSTER_ENDPOINT:
                try {
                    String clusterEnpQuery = queryBuilder("clusterEndpoints", getUUID().toString());
                    if (clusterEnpQuery != null) {
                        ResponseEntity resp = restTemplate.postForEntity(clusterEnpQuery, null, ResponseEntity.class);
                        if (!((resp.getStatusCode().value() >= 200) && (resp.getStatusCode().value() < 300)))
                            return false;
                    }
                    restTemplate.put("v2/clusterEndpoints/{uuid}/config", confiData, getUUID());

                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }
                return true;
            case DEVICE:
                try {
                    String devicesQuery = queryBuilder("devices", getUUID().toString());
                    if (devicesQuery != null) {
                        ResponseEntity resp = restTemplate.postForEntity(devicesQuery, null, ResponseEntity.class);
                        if (!((resp.getStatusCode().value() >= 200) && (resp.getStatusCode().value() < 300)))
                            return false;
                    }
                    restTemplate.put("v2/devices/{uuid}/config", confiData, getUUID());

                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }
                return true;
            case ATTRIBUTE:
                try {
                    String attributesQuery = queryBuilder("attributes", getUUID().toString());
                    if (attributesQuery != null) {
                        ResponseEntity resp = restTemplate.postForEntity(attributesQuery, null, ResponseEntity.class);
                        if (!((resp.getStatusCode().value() >= 200) && (resp.getStatusCode().value() < 300)))
                            return false;
                    }

                    try {
                        Attribute attribute = attributeRepository.get(getUUID());
                        confiData.put("unit", attribute.getConfig().getUnit());
                    } catch (Exception e) {
                        Log.d(TAG, "", e);
                    }
                    restTemplate.put("v2/attributes/{uuid}/config", confiData, getUUID());

                } catch (Exception e) {
                    handlerException(e, TAG);
                    return false;
                }
                return true;
        }
        return false;
    }

    protected boolean getShow() {
        return getItem().isShow();
    }

    protected UiType getUiTYpe() {
        return getItem().getUiType();
    }

    protected UserIcons getUserIcons() {

        return getItem().getUserIcon();
    }

    private String queryBuilder(String entityType, String uuid) {

        StringBuilder stringBuilder = null;
        boolean isDataChange = false;
        if (getShow() != switchShow.isChecked()) {
            stringBuilder = new StringBuilder("v2/" + entityType + "/" + uuid + "/icon?");
            stringBuilder.append("show=" + switchShow.isChecked());
            isDataChange = true;
            Log.d(TAG, "isShow change to " + switchShow.isChecked());
        }
        if (!"attributes".equals(entityType) && ((spinnerType.getSelectedItemPosition() > 0) && ((getUiTYpe() == null) || (getUiTYpe().getEndpointType() == null) || !getUiTYpe().getEndpointType().equals(systemTypesList.get(spinnerType.getSelectedItemPosition()).getEndpointType())))) {
            isDataChange = true;
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder("v2/" + entityType + "/" + uuid + "/icon?");
            } else {
                stringBuilder.append("&");
            }
            stringBuilder.append("icon=" + systemTypesList.get(spinnerType.getSelectedItemPosition()).getName());
            Log.d(TAG, "icon change to " + systemTypesList.get(spinnerType.getSelectedItemPosition()).getName());
        }
//        if ((spinnerIcon.getSelectedItemPosition() > 0) && ((getUserIcons() == null) || !getUserIcons().getName().equals(userIconsList.get(spinnerIcon.getSelectedItemPosition()).getName()))) {
//            isDataChange = true;
//            if (stringBuilder == null) {
//                stringBuilder = new StringBuilder("v2/" + entityType + "/" + uuid + "/icon?");
//            } else {
//                stringBuilder.append("&");
//            }
//            stringBuilder.append("userIcon=" + userIconsList.get(spinnerIcon.getSelectedItemPosition()).getName());
//            Log.d(TAG, "userIcon change to " + userIconsList.get(spinnerIcon.getSelectedItemPosition()).getName());
//        }

        if (isDataChange) {
            Log.d(TAG, "icon Link: " + stringBuilder.toString());
            return stringBuilder.toString();
        } else {
            return null;
        }

    }

    private void createRoomInit() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) ((15 * scale) + 0.5f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpAsPixels;
        params.topMargin = dpAsPixels;
        final EditText editText = new EditText(getContext());
        editText.setLayoutParams(params);
        editText.setGravity(Gravity.CENTER);
        editText.setHint(languageManager.translate("room_name"));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(getContext().getResources().getDrawable(R.drawable.add_new_device_icon));
        builder.setTitle(languageManager.translate("create_new_room"));
        builder.setView(editText);
        builder.setPositiveButton(languageManager.translate("create"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((editText.getText() == null) || "".equals(editText.getText().toString())) {
                    setViews();
                    toast(languageManager.translate("invalid_room_name"));
                    dialog.dismiss();
                    return;
                }
                createRoom(editText.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(languageManager.translate("cancel"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setViews();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void createRoom(final String roomName) {
        executor.execute(new Runnable() {
            @Override
            public void run() {

                if (!checkInternet())
                    return;

                sendMessage(MAIN_UI_SHOW_P_DIALOG, languageManager.translate("creating_room"));

                Map<String, String> roomBody = new WeakHashMap<String, String>();
                roomBody.put("name", roomName);
                final Rooms room;
                try {
                    room = restTemplate.postForObject("v2/rooms/", roomBody, Rooms.class);
                } catch (Exception e) {
                    handlerException(e, TAG);
                    roomCreationFail();
                    return;
                }
                if (room != null) {
                    try {
                        roomRepository.fetchAll();
                    } catch (Exception e) {
                        Log.d(TAG, "Fail reloading all rooms", e);
                        baseFragmentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                roomList.remove(0);
                                roomList.remove(0);
                                roomList.add(room);
                                Collections.sort(roomList, BaseObject.ORDER_NAME_COMPARATOR);
                                Rooms roomAddNew = new Rooms();
                                roomAddNew.setName(languageManager.translate("add_new"));
                                Rooms empty = new Rooms();
                                empty.setName("-");
                                roomList.add(0, roomAddNew);
                                roomList.add(0, empty);
                                spinnerRoom.setSelection(roomAdapter.setSelectedItemPosition(room.getId()));
                                dismissProgressDialog();
                            }
                        });

                        return;

                    }
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            roomDataListGen();
                            spinnerRoom.setSelection(roomAdapter.setSelectedItemPosition(room.getId()));
                            dismissProgressDialog();
                        }
                    });

                } else {
                    roomCreationFail();
                }
            }
        });
    }

    private void roomCreationFail() {
        baseFragmentHandler.post(new Runnable() {
            @Override
            public void run() {
                setViews();
                toast(languageManager.translate("fail_to_create_room"));
                dismissProgressDialog();
            }
        });
    }

    protected void roomDataListGen() {
        roomList.clear();
        roomList.addAll(roomRepository.values());
        Collections.sort(roomList, BaseObject.ORDER_NAME_COMPARATOR);
        Rooms roomAddNew = new Rooms();
        roomAddNew.setName(languageManager.translate("add_new"));
        Rooms empty = new Rooms();
        empty.setName("-");
        roomList.add(0, roomAddNew);
        roomList.add(0, empty);
    }

    protected String getName() {
        return getItem().getName();
    }

    protected String getDescription() {

        return getItem().getDescription();
    }

    protected int getRoom() {
        return getItem().getRoom();
    }

    protected boolean getHidden() {
        return false;
    }

    protected boolean getMaster() {
        return false;
    }

    protected void setViews() {

        if (getEntityType() != null)
            textViewEntityTypeVal.setText(languageManager.translate(getEntityType().name().toLowerCase()));
        textViewEntityTypeVal.setEnabled(false);
        editTextTypeName.setText(getName());
        if (getDescription() != null)
            editTextTypeDesc.setText(getDescription());
        textViewDeviceValue.setEnabled(false);
        String devName = getDeviceName();
        textViewDeviceValue.setText(devName);
        textViewUUIDValue.setEnabled(false);
        textViewUUIDValue.setText(getUUID().toString());
        switchHidden.setChecked(getHidden());
        switchMaster.setChecked(getMaster());
        switchShow.setChecked(getShow());
        roomAdapter.notifyDataSetChanged();
        //iconAdapter.notifyDataSetChanged();
        typeAdapter.notifyDataSetChanged();
        try {
            spinnerRoom.setSelection(roomAdapter.setSelectedItemPosition(Integer.valueOf(getRoom())));
        } catch (Exception e) {

            spinnerRoom.setSelection(0);
        }
        if (((getUiTYpe() != null) && (getUiTYpe().getEndpointType() != null))) {
            spinnerType.setSelection(typeAdapter.setSelectedItemPosition(getUiTYpe().getEndpointType()));
        } else if (endPointType() != null) {
            spinnerType.setSelection(typeAdapter.setSelectedItemPosition(endPointType()));
        }
//        try {
//            spinnerIcon.setSelection(iconAdapter.setSelectedItemPosition(getUserIcons().getName()));
//        } catch (Exception e) {
//            spinnerIcon.setSelection(0);
//        }
    }

    private String getDeviceName() {
        String deviceName = "-";
        switch (getEntityType()) {
            case ENDPOINT:
                Endpoint endpoint = endpointRepository.get(getUUID());
                if (endpoint != null)
                    deviceName = endpoint.getParent().getName();
                break;
            case CLUSTER_ENDPOINT:
                ClusterEndpoint clusterEndpoint = clusterEndpointRepository.get(getUUID());
                if (clusterEndpoint != null) {
                    Endpoint endpoint1 = endpointRepository.get(clusterEndpoint.getParent().getUuid());
                    if (endpoint1 != null)
                        deviceName = endpoint1.getParent().getName();
                }
                break;
            case ATTRIBUTE:
                Attribute attribute = attributeRepository.get(getUUID());
                if (attribute != null) {
                    ClusterEndpoint clusterEndpoint2 = clusterEndpointRepository.get(attribute.getParent().getUuid());
                    if (clusterEndpoint2 != null) {
                        Endpoint endpoint2 = endpointRepository.get(clusterEndpoint2.getParent().getUuid());
                        if (endpoint2 != null)
                            deviceName = endpoint2.getParent().getName();
                    }
                }
                break;
            case NETWORK:
            case DEVICE:
                break;

        }
        return deviceName;
    }

    @Override
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        if ((event.eventType == Event.EVENT_TYPE_REPO_SYNCED) && !isStarted) {
            init();
        }
        //  Log.d(TAG, "Zones update fail: canUpdate? " + canUpdate + " Current delay :" + (System.currentTimeMillis() - previousUpdate) + " event ID: " + onUpdate);

    }

    @Override
    protected void handlerConnectivityEvent(ObjectConnectivity event) {

    }

    class RoomListAdapter extends BaseListAdapter implements SpinnerAdapter {

        private int setSelectedItemPosition(int roomID) {
            for (int i = 0; i < roomList.size(); i++) {

                if (roomList.get(i).getId() == roomID) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            {
                TextView textView;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_spinner_config_drop_down, parent, false);
                    textView = (TextView) convertView.findViewById(R.id.textView);
                    convertView.setTag(textView);

                } else {
                    textView = (TextView) convertView.getTag();

                }
                textView.setTypeface(typeFaceUtils.getTypeFace("helvetica_neue_light.otf"));
                textView.setText(roomList.get(position).getName());
                return convertView;
            }
        }

        @Override
        public int getCount() {
            return roomList.size();
        }

        @Override
        public Rooms getItem(int position) {
            return roomList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_spinner_config, parent, false);
                textView = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(textView);

            } else {
                textView = (TextView) convertView.getTag();
            }
            textView.setTypeface(typeFaceUtils.getTypeFace("helvetica_neue_light.otf"));
            textView.setText(roomList.get(position).getName());
            return convertView;
        }
    }

    class TypeListAdapter extends BaseListAdapter implements SpinnerAdapter {

        private int setSelectedItemPosition(String endpointType) {
            for (int i = 0; i < systemTypesList.size(); i++) {
                if (systemTypesList.get(i).getEndpointType() == null)
                    continue;
                if (systemTypesList.get(i).getEndpointType().equals(endpointType)) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            {
                TextView textView;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_spinner_config_drop_down, parent, false);
                    textView = (TextView) convertView.findViewById(R.id.textView);
                    convertView.setTag(textView);

                } else {
                    textView = (TextView) convertView.getTag();

                }

                textView.setTypeface(typeFaceUtils.getTypeFace("helvetica_neue_light.otf"));
                if (position > 0) {
                    textView.setText(languageManager.translate(systemTypesList.get(position).getEndpointType()));
                } else {
                    textView.setText(systemTypesList.get(position).getEndpointType());
                }

                return convertView;
            }
        }

        @Override
        public int getCount() {
            return systemTypesList.size();
        }

        @Override
        public SystemTypes getItem(int position) {
            return systemTypesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_spinner_config, parent, false);
                textView = (TextView) convertView.findViewById(R.id.textView);
                convertView.setTag(textView);

            } else {
                textView = (TextView) convertView.getTag();
            }

            textView.setTypeface(typeFaceUtils.getTypeFace("helvetica_neue_light.otf"));

            if (position > 0) {
                textView.setText(languageManager.translate(systemTypesList.get(position).getEndpointType()));
            } else {
                textView.setText(systemTypesList.get(position).getEndpointType());
            }

            return convertView;
        }
    }
}
