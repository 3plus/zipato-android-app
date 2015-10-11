/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.discovery;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.activities.BaseActivity;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.settings.AnimatedExpandableListView;
import com.zipato.model.BaseObject;
import com.zipato.model.brand.Brand;
import com.zipato.model.brand.BrandDevice;
import com.zipato.model.brand.BrandRepository;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectLauncher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;

/**
 * Created by murielK on 8/13/2014.
 */
public class SDeviceFragment extends BaseDiscoveryFragment {

    private static final String FRAGMENT_TAG = SDeviceFragment.class.getSimpleName();
    private static final Map<String, Integer> BRAND_NAME_RES_REPO = new HashMap<>();//Since getIdentifier() is a bit slow, will use a  hashMap for brand name_res_id to speed up the process...

    static {
        BRAND_NAME_RES_REPO.put("zwave", R.drawable.zwave);
        BRAND_NAME_RES_REPO.put("zigbee", R.drawable.zigbee);
        BRAND_NAME_RES_REPO.put("paradox", R.drawable.paradox);
        BRAND_NAME_RES_REPO.put("knx", R.drawable.knx);
        BRAND_NAME_RES_REPO.put("enocean", R.drawable.enocean);
        BRAND_NAME_RES_REPO.put("chacon", R.drawable.chacon);
        BRAND_NAME_RES_REPO.put("arw", R.drawable.arw);
        BRAND_NAME_RES_REPO.put("ipcamera", R.drawable.ipcamera);
        BRAND_NAME_RES_REPO.put("virtual", R.drawable.virtualdevice);
        BRAND_NAME_RES_REPO.put("coco", R.drawable.coco);
        BRAND_NAME_RES_REPO.put("intertechno", R.drawable.intertechno);
        BRAND_NAME_RES_REPO.put("intertek", R.drawable.intertek);
        BRAND_NAME_RES_REPO.put("chuango", R.drawable.chuango);
        BRAND_NAME_RES_REPO.put("kangtai", R.drawable.kangtai);
        BRAND_NAME_RES_REPO.put("somfy", R.drawable.somfy);
        BRAND_NAME_RES_REPO.put("lightwave", R.drawable.lightwave);
        BRAND_NAME_RES_REPO.put("longhorn", R.drawable.longhorn);
        BRAND_NAME_RES_REPO.put("nexa", R.drawable.nexa);
        BRAND_NAME_RES_REPO.put("oregon", R.drawable.oregon);
        BRAND_NAME_RES_REPO.put("owl", R.drawable.owl);
        BRAND_NAME_RES_REPO.put("vision", R.drawable.vision);
        BRAND_NAME_RES_REPO.put("visonic", R.drawable.visonic);
        BRAND_NAME_RES_REPO.put("x10", R.drawable.x10);
        BRAND_NAME_RES_REPO.put("goelst", R.drawable.goelst);
        BRAND_NAME_RES_REPO.put("x10plc", R.drawable.x10_plc);
        BRAND_NAME_RES_REPO.put("dakota", R.drawable.dakota);
        BRAND_NAME_RES_REPO.put("dsc", R.drawable.dsc);
        BRAND_NAME_RES_REPO.put("nest", R.drawable.nest);
        BRAND_NAME_RES_REPO.put("p1", R.drawable.p1);
        BRAND_NAME_RES_REPO.put("philipshue", R.drawable.plihlips_hue);
        BRAND_NAME_RES_REPO.put("pulseworx", R.drawable.pulseworx);
        BRAND_NAME_RES_REPO.put("sonos", R.drawable.sonos);
    }

    @Inject
    BrandRepository brandRepository;
    @Bind(B.id.expandableListViewSD)
    AnimatedExpandableListView exListViewBrand;
    @Bind(B.id.progressBarSD)
    ProgressBar progressBar;
    @Inject
    ExecutorService executor;
    List<Brand> brandList = new ArrayList<>();
    private String titleResId = "select_network";
    private int exCounter;
    private ExpListViewAdapter listViewAdapter;
    private boolean progress;
    private volatile boolean isDataCollect;


    @Override
    protected int getResourceView() {
        return R.layout.fragment_sd;
    }

    @Override
    protected void onPostViewCreate() {
        listViewAdapter = new ExpListViewAdapter(getContext());
        exListViewBrand.setAdapter(listViewAdapter);
        exListViewBrand.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (exCounter > 0) {
                    setTitleResId(true);
                    eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
                }
            }
        });
        exListViewBrand.setOnGroupClickListener(
                new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                        if ((brandList.get(groupPosition).getName() == null)) {
                            toast(languageManager.translate("error_data"));
                            return true;
                        }

                        Log.d("SDeviceFragment", "Brand selected: " + brandList.get(groupPosition).getName() + " Brand size: " + brandList.size());

                        if ("ipcamera".equalsIgnoreCase(brandList.get(groupPosition).getName())) {
                            try {
                                final ObjectParcel objectParcel = new ObjectParcel(brandList.get(groupPosition).getNetworks()[0].getUuid(), null, null);
                                eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, JIPCamFragment.class, objectParcel), Event.EVENT_TYPE_LAUNCHER));
                            } catch (Exception e) {
                                toast("error_data");
                            }
                            return true;
                        }

                        if ("zwave".equalsIgnoreCase(brandList.get(groupPosition).getName())) {
                            try {
                                ObjectParcel objectParcel = new ObjectParcel(brandList.get(groupPosition).getNetworks()[0].getUuid(), null, null);
                                objectParcel.setZwave(true);
                                Log.d(FRAGMENT_TAG, brandList.get(groupPosition).getName() + " Available? " + brandList.get(groupPosition).isAvailable());
                                eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, RDeviceFragment.class, objectParcel), Event.EVENT_TYPE_LAUNCHER));
                            } catch (Exception e) {
                                toast("error_data");
                            }
                            return true;

                        } else if (brandList.get(groupPosition).isAvailable() && ((brandList.get(groupPosition).getDevices() == null) || (brandList.get(groupPosition).getDevices().length == 0))) {
                            try {
                                Log.d(FRAGMENT_TAG, brandList.get(groupPosition).getName() + " Available? " + brandList.get(groupPosition).isAvailable());
                                final ObjectParcel objectParcel = new ObjectParcel(brandList.get(groupPosition).getNetworks()[0].getUuid(), null, null);
                                eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, RDeviceFragment.class, objectParcel), Event.EVENT_TYPE_LAUNCHER));
                            } catch (Exception e) {
                                toast("error_data");
                            }

                        } else if (!brandList.get(groupPosition).isAvailable() && ((brandList.get(groupPosition).getDevices() == null) || (brandList.get(groupPosition).getDevices().length == 0))) {

                            Log.d(FRAGMENT_TAG, brandList.get(groupPosition).getName() + " Available? " + brandList.get(groupPosition).isAvailable());
                            toast(languageManager.translate("dev_not_supported"));
                        } else {
                            if (exListViewBrand.isGroupExpanded(groupPosition)) {
                                exCounter--;
                                exCounter = (exCounter < 0) ? 0 : exCounter;
                                exListViewBrand.collapseGroupWithAnimation(groupPosition);
                                if (exCounter == 0) {
                                    setTitleResId(false);
                                    eventBus.post(new Event(null, Event.EVENT_TYPE_DIS_REFRESH_TITLE));
                                }


                            } else {
                                exCounter++;
                                exListViewBrand.expandGroupWithAnimation(groupPosition);
                            }

                        }
                        return true;
                    }
                });

        exListViewBrand.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (!progress) {
                    if (!brandList.get(groupPosition).isAvailable()) {
                        toast(languageManager.translate("dev_not_supported"));
                        return false;
                    }
                    // baseFragmentHandler.obtainMessage(BaseFragment.MAIN_UI_VISIBILITY_VISIBLE,v.findViewById(R.id.progressBarSDChild));
                    try {
                        final ObjectParcel objectParcel = new ObjectParcel(brandList.get(groupPosition).getNetworks()[0].getUuid(), brandList.get(groupPosition).getDevices()[childPosition].getDiscoveryData(), brandList.get(groupPosition).getDevices()[childPosition].getName());
                        eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, RDeviceFragment.class, objectParcel), Event.EVENT_TYPE_LAUNCHER));
                    } catch (Exception e) {
                        toast(languageManager.translate("error_data"));
                    }

                }
                return false;
            }

        });

    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        collectDate(false);


    }

    private void collectDate(final boolean refresh) {
        if (!internetConnectionHelper.isOnline()) {
            toast(languageManager.translate("internet_error_refresh"));
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                isDataCollect = true;
                baseFragmentHandler.obtainMessage(BaseFragment.MAIN_UI_VISIBILITY_VISIBLE, progressBar).sendToTarget();
                brandList.clear();
                if (refresh)
                    brandRepository.clear();
                baseFragmentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listViewAdapter.notifyDataSetChanged();
                    }
                });
                if (brandRepository.isEmpty()) {
                    try {
                        String[] brands = getResources().getStringArray(R.array.custom_brand_list);
                        if (brands.length > 0) {
                            for (String brandName : brands) {
                                brandRepository.fetchBrand(brandName);
                            }
                        } else {
                            brandRepository.fetchAll();
                        }
                    } catch (Exception e) {
                        handlerException(e, getFragmentTag());
                        // restTemplate.callingNop();
                    }
                }

                brandRepository.remove("Virtual");
                brandList.addAll(brandRepository.values());
                Collections.sort(brandList, BaseObject.ORDER_NAME_COMPARATOR);
                baseFragmentHandler.obtainMessage(BaseFragment.MAIN_UI_VISIBILITY_GONE, progressBar).sendToTarget();
                baseFragmentHandler.obtainMessage(BaseFragment.MAIN_UI_VISIBILITY_VISIBLE, exListViewBrand).sendToTarget();
                isDataCollect = false;
                baseFragmentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listViewAdapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    private void setTitleResId(boolean parentExpend) {
        if (parentExpend) {
            titleResId = "select_device";
            return;
        }
        titleResId = "select_network";

    }

    @Override
    String stringTitle() {
        return titleResId;
    }

    @Override
    int stepID() {
        return 1;
    }

    @Override
    String fragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    String stringButTitle() {
        return "cancel";
    }

    public void onEventMainThread(Event event) {
        if ((event.eventType == Event.EVENT_TYPE_REFRESH_REQUEST) && !isDataCollect) {
            if (getContext() != null) {
                final BaseActivity baseActivity = (BaseActivity) getActivity();
                final SlidingMenu slidingMenu = baseActivity.getSlidingMenu();
                if (slidingMenu.isMenuShowing())
                    slidingMenu.toggle();
            }
            collectDate(true);
        }
        //  Log.d(TAG, "Zones update fail: canUpdate? " + canUpdate + " Current delay :" + (System.currentTimeMillis() - previousUpdate) + " event ID: " + onUpdate);
    }

    class ExpListViewAdapter extends AnimatedExpandableListView.AnimatedExpandableListAdapter {

        private ExpListViewAdapter(Context context) {
        }

        @Override
        public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ChildHolder childHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_sd_child, null);
                childHolder = new ChildHolder(convertView);
                convertView.setTag(childHolder);

            } else {
                childHolder = (ChildHolder) convertView.getTag();
            }
            childHolder.textViewDevicesName.setText(brandList.get(groupPosition).getDevices()[childPosition].getName());
            return convertView;
        }

        @Override
        public int getRealChildrenCount(int groupPosition) {
            return (brandList.get(groupPosition).getDevices() == null) ? 0 : brandList.get(groupPosition).getDevices().length;
        }

        @Override
        public int getGroupCount() {
            return brandList.size();
        }

        @Override
        public Brand getGroup(int groupPosition) {
            return brandList.get(groupPosition);
        }

        @Override
        public BrandDevice getChild(int groupPosition, int childPosition) {
            return brandList.get(groupPosition).getDevices()[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

            ParentHolder parentHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_sd_parent, null);
                parentHolder = new ParentHolder(convertView);
                convertView.setTag(parentHolder);
            } else {
                parentHolder = (ParentHolder) convertView.getTag();
            }

            try {
                String drawableName = brandList.get(groupPosition).getName().toLowerCase();
                int resID = BRAND_NAME_RES_REPO.get(drawableName);
                if (resID != 0) {

                    parentHolder.logo.setBackgroundDrawable(getResources().getDrawable(resID));
                } else {
                    parentHolder.logo.setBackgroundDrawable(getResources().getDrawable(R.drawable.empty_drawable));
                }
            } catch (Exception e) {

                parentHolder.logo.setBackgroundDrawable(getResources().getDrawable(R.drawable.empty_drawable));
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        class ParentHolder {
            @Bind(B.id.imageBrand)
            ImageView logo;
            public ParentHolder(View v) {
                ButterFork.bind(this, v);
            }
        }

        class ChildHolder {
            @Bind(B.id.textViewBrandDevice)
            TextView textViewDevicesName;

            public ChildHolder(View v) {
                ButterFork.bind(this, v);

            }
        }
    }
}
