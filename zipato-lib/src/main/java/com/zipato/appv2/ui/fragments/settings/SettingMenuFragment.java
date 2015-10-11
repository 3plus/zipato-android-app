/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.settings;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R;
import com.zipato.appv2.R.bool;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.R.string;
import com.zipato.appv2.activities.BaseActivity;
import com.zipato.appv2.activities.BrowserManagerActivity;
import com.zipato.appv2.activities.DeviceManagerActivity;
import com.zipato.appv2.activities.DiscoveryActivity;
import com.zipato.appv2.activities.WizardActivity;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.settings.ExpendableListerViewCustomAdapter;
import com.zipato.appv2.ui.fragments.adapters.settings.ExpendableListerViewCustomAdapter.GroupClickListner;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.box.Box;
import com.zipato.model.client.RestObject;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectConnectivity;
import com.zipato.model.event.ObjectLauncher;
import com.zipato.model.event.ObjectMenu;
import com.zipato.model.language.Language;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.ApiV2RestTemplate;
import com.zipato.v2.client.RestObjectClientException;

import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 16.6.2014..
 */
public class SettingMenuFragment extends BaseFragment {

    private static final String TAG = SettingMenuFragment.class.getSimpleName();
    private static final long LONG_DELAY = 120000L;
    private static final long SHORT_DELAY = 3000L;
    private final List<Box> boxList = new ArrayList<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
    ExpendableListerViewCustomAdapter listerViewCustomAdapter;
    @InjectView(id.expandableListView)
    AnimatedExpandableListView expandableListView;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    PreferenceHelper preferenceHelper;
    @Inject
    EventBus eventBus;
    @Inject
    DeviceStateRepository deviceStateRepository;
    @Inject
    AttributeValueRepository attributeValueRepository;
    @Inject
    TypeFaceUtils typeFaceUtils;
    @Inject
    ExecutorService executor;

    @SetTypeFace("helvetica_neue_light.otf")
    @InjectView(id.textView)
    TextView mainMenuText;

    private Box currentBox;
    private List<Helper> groupList;
    private HashMap<String, Event> map;
    private String browserManagerText;
    private String deviceManagerText;
    private String addNewDevice;
    private String refresh;
    private String synchronize;
    private String logOutText;
    private String loadingBox;
    private String settings;
    private volatile boolean isMultiStarted;
    private volatile boolean isPause;
    private volatile boolean isChanging;
    private long delay;
    private volatile boolean isMultiManual;
    private volatile boolean isMultiAuto;
    private boolean isOffline;
    private String wizard;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putBoolean(THREE_LOADED_KEY, isThreeLoaded);
    }

    private Box updateBoxList() {
        if (!restTemplate.isAuthenticated()) {
            // FIXME: i know it's a racist thing, but white man would never do shit like this (FUCK YOU DARKO)
            throw new IllegalStateException("not authenticated");
        }
        // to try again in 3 sec  :D
        loadingBox = languageManager.translate("loading_box");

        final Box box = restTemplate.getForObject("v2/box", Box.class);
        final Box[] boxArray = restTemplate.getForObject("v2/box/list", Box[].class);
        if ((box != null) && (box.getSerial() != null)) {
            for (final Helper helper : groupList) {
                if (helper.groupImage == drawable.ic_box_offline) {

                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            currentBox = box;
                            helper.box = currentBox;
                            boxList.clear();
                            boxList.addAll(Arrays.asList(boxArray));
                            boxList.remove(box);
                            boxList.add(0, new Box());
                            helper.children = boxList;
                            listerViewCustomAdapter.notifyDataSetChanged();
                        }
                    });

                    break;
                }
            }
        }
        return box;
    }

    private void boxUpdate() {
        final Box box = updateBoxList();
        final long saveDate = preferenceHelper.getLongPref(Preference.SAVE_DATA);
        final String saveSerial = preferenceHelper.getStringPref(Preference.BOX_SERIAL, "");
        Log.d(TAG, "Store saveDate: " + formatter.format(new Date(saveDate)) + " box saveDate: " +
                formatter.format(new Date(box.getSaveDate().getTime())));
        Log.d(TAG, "Store savSerial: " + saveSerial + " current Box Serial: " + box.getSerial());
        if ((box.getSaveDate().getTime() > saveDate) || !box.getSerial().equals(saveSerial)) {
            eventBus.post(new Event(null, Event.EVENT_TYPE_REFRESH_REQUEST));
            Log.d(TAG, "REFRESH_REQUEST even sent");
        }
    }

    private void multiBoxManual() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    isMultiManual = true;
                    boxUpdate();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                } finally {
                    isMultiManual = false;
                }
            }
        });
    }

    private void multiBoxAuto() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!restTemplate.isUseLocal() && !isDetached() && !isPause && !isChanging) {
                    isMultiStarted = true;
                    if (!isMultiManual) {
                        isMultiAuto = true;
                        try {
                            boxUpdate();
                            delay = LONG_DELAY;
                        } catch (Exception e) {
                            delay = SHORT_DELAY;
                            if ((e instanceof RestObjectClientException) && (((RestObjectClientException) e).getResponseBody() != null) &&
                                    (((RestObjectClientException) e).getResponseBody().getError() != null)) {

                                Log.d("SettingMenu", ((RestObjectClientException) e).getResponseBody().getError(), e);
                            } else {
                                Log.d("SettingMenu", "", e);
                            }
                            loadingBox = languageManager.translate("fail");
                        }
                        baseFragmentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listerViewCustomAdapter.notifyDataSetChanged();
                            }
                        });
                        isMultiAuto = false;
                    } else {
                        Log.d(TAG, "MENU_THREAD manual update is running skipping auto for this time :D");
                        delay = SHORT_DELAY;
                    }
                    long time = 0;
                    try {
                        Log.e(TAG, "MENU_THREAD will go to sleep");
                        time = System.currentTimeMillis();
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.e(TAG, "MENU_THREAD is up sleeping time: " + ((System.currentTimeMillis() - time) / 1000) + "s");
                }
                Log.e(TAG, "MENU_THREAD TERMINATE!!!!");
                isMultiStarted = false;
            }
        }, "Setting_Menu_multiBoxConfig_Thread").start();

    }

    private void configSettingView() {
        groupList.clear();
        listerViewCustomAdapter.notifyDataSetChanged();

        Helper lang = new Helper();
        lang.groupImage = drawable.language_group_icon;
        lang.children = new ArrayList<Language>(languageManager.getLanguages().values());
        lang.layout = R.layout.row_expendable_list_view_item;
        lang.selected = languageManager.getLanguage();// get default language);

        Helper server = new Helper();
        server.groupImage = drawable.server_url_group_icon;
        server.selected = languageManager.translate("server_url_text_menu");
        server.children = Collections.singletonList(preferenceHelper.getStringPref(Preference.SERVER_URL, preferenceHelper.getBaseUrl()));
        server.layout = R.layout.row_expendable_list_view_item_edit;

        if ((getActivity() instanceof BrowserManagerActivity) || (getActivity() instanceof DeviceManagerActivity) ||
                (getActivity() instanceof DiscoveryActivity)) {

            if (map == null) {
                map = new HashMap<>();
            }
            Helper boxHelper = new Helper(loadingBox, layout.row_multibox_children, boxList, drawable.ic_box_offline);
            boxHelper.setObject(currentBox);
            groupList.add(boxHelper);
            groupList.add(new Helper(browserManagerText, 0, null, drawable.device_browser_icon));
            if (map.get(browserManagerText) == null) {
                Event event = new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_ACTIVITY, BrowserManagerActivity.class, null), Event.EVENT_TYPE_LAUNCHER);
                map.put(browserManagerText, event);
            }
            //  if (isThreeLoaded) {
            groupList.add(new Helper(deviceManagerText, 0, null, drawable.device_manager_icon));
            if (map.get(deviceManagerText) == null) {
                Event event = new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_ACTIVITY, DeviceManagerActivity.class, null), Event.EVENT_TYPE_LAUNCHER);
                map.put(deviceManagerText, event);
            }
            //  }
            if (getActivity().getResources().getBoolean(bool.menu_add_new_device)) {
                groupList.add(new Helper(addNewDevice, 0, null, drawable.add_new_device_icon));
                if (map.get(addNewDevice) == null) {
                    Event event = new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_ACTIVITY, DiscoveryActivity.class, null), Event.EVENT_TYPE_LAUNCHER);
                    map.put(addNewDevice, event);
                }
            }

//            groupList.add(new Helper(sceneText, 0, null, R.drawable.scenes_group_icon));
//            if (map.get(sceneText) == null) {
//                map.put(sceneText, new LauncherEvent(SceneRunFragment.TAG, LauncherEvent.LaunchType.FRAGMENT_RUN_SCENE, SceneRunFragment.class));
//            }

            groupList.add(new Helper(refresh, 0, null, drawable.ic_refresh));
            groupList.add(new Helper(synchronize, 0, null, drawable.ic_synchronize));
            // if (getSherlockActivity().getResources().getBoolean(R.bool.menu_show_server_url) || getSherlockActivity().getResources().getBoolean(R.bool.menu_show_language)||getSherlockActivity().getResources().getBoolean(R.bool.portrait_only))
            groupList.add(new Helper(settings, 0, null, drawable.ic_settings));
            if (getActivity().getResources().getBoolean(bool.show_wizard)) {
                groupList.add(new Helper(wizard, 0, null, drawable.ic_wizard));
                if (map.get(wizard) == null) {
                    Event event = new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_ACTIVITY, WizardActivity.class, null), Event.EVENT_TYPE_LAUNCHER);
                    map.put(wizard, event);
                }
            }
            groupList.add(new Helper(logOutText, 0, null, drawable.logout_icon));
            if (map.get(logOutText) == null) {
                Event event = new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_LOG_OUT, null, null), Event.EVENT_TYPE_LAUNCHER);
                map.put(logOutText, event);
            }
            if (!isMultiStarted) {
                multiBoxAuto();
            }

        } else {
            groupList.add(new Helper(settings, 0, null, drawable.ic_settings));

        }
        listerViewCustomAdapter.toggleSelection(lang.children.indexOf(lang.selected));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_menu_layout;
    }

    @Override
    protected void onPostViewCreate() {
        typeFaceUtils.applyTypefaceFor(this);
        deviceManagerText = languageManager.translate("device_manager_text_menu");
        browserManagerText = languageManager.translate("device_browser_text_menu");
        addNewDevice = languageManager.translate("add_device_text_menu");
        logOutText = languageManager.translate("logout_text_menu");
        loadingBox = languageManager.translate("loading_box");
        refresh = languageManager.translate("refresh");
        synchronize = languageManager.translate("synchronize");
        mainMenuText.setText(languageManager.translate("main_menu"));
        settings = languageManager.translate("SettingsScreenTitle");
        wizard = languageManager.translate("wizard");
    }

    public void synchronize() {
        if (!checkInternet()) {
            return;
        }
        showProgressDialog(languageManager.translate("synchronizing"), false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    Log.d(TAG, "Starting synchronization...");
                    RestObject resp = restTemplate.getForObject("v2/box/synchronize?ifNeeded=false&wait=true&timeout=120", RestObject.class);
                    Log.d(TAG, "Synchronization done and isSuccess? " + resp.isSuccess());
                    success = resp.isSuccess();
                } catch (Exception e) {
                    handlerException(e, TAG);
                } finally {
                    final boolean finalSuccess = success;
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isDetached()) {
                                dismissProgressDialog();
                                if (!finalSuccess)
                                    toast(languageManager.translate("synch_fail"));
                            }
                        }
                    });
                }
            }
        });

    }

    private void setView() {
        groupList = new ArrayList<>();
        expandableListView = (AnimatedExpandableListView) getActivity().findViewById(id.expandableListView);
        listerViewCustomAdapter = new ExpendableListerViewCustomAdapter(getActivity(), groupList, languageManager, internetConnectionHelper, typeFaceUtils);
        //listerViewCustomAdapter.setOnServerUrlChangedListner(this);
        listerViewCustomAdapter.setGroupClickListner(new GroupClickListner() {
            @Override
            public void onGroupClick(int position) {
                if ((groupList.get(position).children == null) || groupList.get(position).children.isEmpty()) {
                    //  closeMenu();
                    switch (groupList.get(position).groupImage) {
                        case drawable.ic_refresh:
                            closeMenu();
                            eventBus.post(new Event(null, Event.EVENT_TYPE_REFRESH_REQUEST));
                            attributeValueRepository.clearETag();
                            deviceStateRepository.clearETag();
                            break;
                        case drawable.ic_synchronize:
                            closeMenu();
                            synchronize();
                            break;
                        case drawable.ic_box_offline:
                            regBoxInit();
                            break;
                        case drawable.ic_settings:
                            eventBus.post(new Event(new ObjectMenu(ObjectMenu.MENU_SETTINGS, null), Event.EVENT_TYPE_TYPE_MENU));
                            break;
                        default:
                            try {
                                final String key = (String) groupList.get(position).getSelected();
                                eventBus.post(map.get(key));//
                            } catch (Exception e) {
                                Log.d(TAG, "", e);
                            }
                            break;
                    }
                    //closeMenu();
                    return;
                }
                if (expandableListView.isGroupExpanded(position)) {
                    expandableListView.collapseGroupWithAnimation(position);
                } else {
                    expandableListView.expandGroupWithAnimation(position);
                }
            }

            @Override
            public void onInfoClick(int position) {
                eventBus.post(new Event(new ObjectMenu(ObjectMenu.MENU_BOX_INFO, currentBox), Event.EVENT_TYPE_TYPE_MENU));
            }
        });

        expandableListView.setAdapter(listerViewCustomAdapter);
        //expandableListView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        expandableListView.setOnChildClickListener(new OnChildClickListener() {
            @TargetApi(VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //the default clicked language will be set here of course
                switch (groupList.get(groupPosition).groupImage) {
//                    case R.drawable.language_group_icon:
//                        listerViewCustomAdapter.toggleSelection(childPosition);
//                        groupList.get(groupPosition).selected = groupList.get(groupPosition).children.get(childPosition);
//                        expandableListView.setSelectedChild(groupPosition, childPosition, true);
//                        preferenceManager.putStringPref(PreferenceManager.Preference.LANGUAGE,
//                                ((Language) groupList.get(groupPosition).children.get(childPosition))
//                                        .getCode());
//                        listerViewCustomAdapter.notifyDataSetChanged();
//                        getSherlockActivity().recreate();
//                        break;
                    case drawable.ic_box_offline:
                        if (childPosition == 0) {

                            regBoxInit();

                        } else {
                            changeBox((Box) groupList.get(groupPosition).children.get(childPosition));
                        }
                        break;
                }

                return false;
            }
        });

        expandableListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                return true;
            }
        });
    }

    private void closeMenu() {
        try {
            BaseActivity baseActivity = (BaseActivity) getActivity();
            final SlidingMenu slidingMenu = baseActivity.getSlidingMenu();
            if ((slidingMenu != null) && slidingMenu.isMenuShowing())
            slidingMenu.toggle();
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    private void registeringBox(final String serial) {
        if (!checkInternet()) {
            return;
        }
        showProgressDialog(languageManager.translate("registering_box_dialog_msg") + serial, false);
        executor.execute(new Runnable() {
                             @Override
                             public void run() {
                                 try {
                                     try {
                                         ResponseEntity resp = restTemplate.postForEntity("v2/box/register?serial={serial}", null, ResponseEntity.class, serial);
                                         if (!((resp.getStatusCode().value() >= 200) && (resp.getStatusCode().value() < 300))) {
                                             updateBoxList();
                                             eventBus.post(new Event(null, Event.EVENT_TYPE_REFRESH_REQUEST));
                                         } else {
                                             baseFragmentHandler.post(new Runnable() {
                                                 @Override
                                                 public void run() {
                                                     toast(languageManager.translate("box_reg_resp_false"));
                                                 }
                                             });
                                         }
                                     } catch (final RestObjectClientException e) {
                                         Log.e(TAG, e.getResponseBody().getError());
                                         baseFragmentHandler.post(new Runnable() {
                                             @Override
                                             public void run() {
                                                 toast(languageManager.translate(e.getResponseBody().getError().replace(" ", "_")));
                                             }
                                         });
                                     }
                                 } catch (Exception e) {
                                     Log.d(TAG, "", e);
                                     handlerException(e, TAG);

                                 } finally {
                                     baseFragmentHandler.post(new Runnable() {
                                         @Override
                                         public void run() {
                                             dismissProgressDialog();
                                         }
                                     });
                                 }
                             }
                         }
        );

    }

    private void regBoxInit() {
        final float scale = getActivity().getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) ((20 * scale) + 0.5f);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpAsPixels;
        params.topMargin = dpAsPixels;
        final EditText editText = new EditText(getActivity());
        editText.setLayoutParams(params);
        editText.setGravity(Gravity.CENTER);
        editText.setHint(languageManager.translate("reg_box_dialog_message_hint"));
        Builder builder = new Builder(getActivity());
        builder.setIcon(getActivity().getResources().getDrawable(drawable.add_new_device_icon));
        final String title = languageManager.translate("product_registration");
        final String titleFinal = title.replace("{productName}", getContext().getResources().getString(string.reg_box));
        //builder.setTitle(getSherlockActivity().getResources().getString(R.string.reg_box) + " " + languageManager.translate("registration"));
        builder.setTitle(titleFinal);
        builder.setView(editText);
        builder.setPositiveButton(languageManager.translate("register"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((editText.getText() == null)) {
                    toast(languageManager.translate("invalid_room_name"));
                    dialog.dismiss();
                    return;
                }
                registeringBox(editText.getText().toString());
                dialog.dismiss();

            }
        });

        builder.setNegativeButton(languageManager.translate("cancel"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    private void changeBox(final Box box) {
        if (!checkInternet()) {
            return;
        }
        showProgressDialog(languageManager.translate("box_select_message") + box.getName(), false);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    isChanging = true;
                    restTemplate.getForObject("v2/box/select?serial={serial}", Box.class, box.getSerial());
                    updateBoxList();
                } catch (Exception e) {
                    Log.d(TAG, "", e);

                } finally {
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!box.getSerial().equals(currentBox.getSerial()))
                                toast(languageManager.translate("fail"));
                            else
                                eventBus.post(new Event(null, Event.EVENT_TYPE_ON_BOX_CHANGE));
                            dismissProgressDialog();
                            isChanging = false;
                        }
                    });

                }
            }
        });

    }

//    @Override
//    public void onServerUrlChanged(String newUrl) {
//
//        restTemplate.setRemoteUrl(newUrl);
//        preferenceManager.putStringPref(PreferenceManager.Preference.SERVER_URL, newUrl);
//        for (Helper list : groupList) {
//            if (list.groupImage == R.drawable.server_url_group_icon) {
//                list.children = Collections.singletonList(newUrl);
//                break;
//            }
//        }
//
//    }

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
        eventBus.unregister(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        try {
            eventBus.register(this);
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPause = false;
        if ((getActivity() instanceof BrowserManagerActivity) && restTemplate.isAuthenticated()) {
            boolean refreshOnResume = preferenceHelper.getBooleanPref(Preference.REFRESH_ON_RESUME);
            if (refreshOnResume) {
                preferenceHelper.putBooleanPref(Preference.REFRESH_ON_RESUME, false);
                eventBus.post(new Event(null, Event.EVENT_TYPE_REFRESH_REQUEST));
            } else if (!isMultiAuto)
                multiBoxManual();
            else {
                Log.d(TAG, "MAIN MENU: AutoBOX update is running no manual update");
            }
        }
        configSettingView();
    }

    public void onEventMainThread(Event event) {
        if (event.eventType == Event.EVENT_TYPE_CONNECTIVITY_EVENT) {
            final ObjectConnectivity objectConnectivity = (ObjectConnectivity) event.eventObject;
            if (!objectConnectivity.isOnline) {
                isOffline = true;
                listerViewCustomAdapter.notifyDataSetChanged();
            } else if (isOffline && !isMultiManual && !isMultiAuto) {
                isOffline = false;
                multiBoxManual();
            }
        }
    }

    public static class Helper {
        public Object selected;
        public int layout;
        public int groupImage;
        public List<?> children;
        public Object box;

        public Helper() {
        }

        public Helper(Object selected, int layout, List<?> children, int groupImage) {
            this.selected = selected;
            this.layout = layout;
            this.children = children;
            this.groupImage = groupImage;
        }

        public Object getObject() {
            return box;
        }

        public void setObject(Object object) {
            box = object;
        }

        public Object getSelected() {
            return selected;
        }

        public int getLayout() {
            return layout;
        }

    }
}