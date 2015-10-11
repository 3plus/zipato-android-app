/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.security;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.helper.DeleteDialogHelper;
import com.zipato.model.alarm.Zone;
import com.zipato.model.alarm.ZoneState;
import com.zipato.model.event.Event;
import com.zipato.util.TypeFaceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;


/**
 * Created by murielK on 8/27/2014.
 */
public class ZonesFragment extends BaseSecurityFragment {

    private static final String TAG = ZonesFragment.class.getSimpleName();
    @InjectView(R.id.listViewZone)
    ListView listViewZones;
    @InjectView(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    TypeFaceUtils typeFaceUtils;

    ZoneListAdapter adapter;
    List<Zone> listZones = new ArrayList<>();
    private ActionMode mActionMode;

    @Override
    protected int getResourceView() {
        return R.layout.fragment_zone;
    }

    @Override
    protected void onPostViewCreate() {
        adapter = new ZoneListAdapter();
        listViewZones.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init();
            }
        });
    }

    @OnItemClick(R.id.listViewZone)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.toggleSelection(position);
        setOnActionBarMenu(adapter.getSelectedCount());
    }

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        init();
    }

    @Override
    protected void init() {
        try {
            Log.d(TAG, "init zones");
            if (!swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(true);
            listZones.clear();
            adapter.notifyDataSetChanged();
            if (partitionRepository.get(getItem().getUuid()) != null) {
                partition = partitionRepository.get(getItem().getUuid());
                listZones.clear();
                for (Zone zones : partition.getZones()) {
                    if (zonesRepository.get(zones.getUuid()) != null) {

                        Zone zone = zonesRepository.get(zones.getUuid());

                        listZones.add(zone);
                    }
                }
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }

    }

    @Override
    public void onEventMainThread(Event event) {
        //TODO in case you want some update for later
    }


    private void setOnActionBarMenu(int checkedCount) {

        if (checkedCount > 0) {
            if (mActionMode == null) {
                mActionMode = getActivity().startActionMode(new ModeCallback());
            }
            int tempAdapterSize = adapter.getCount();
            if (checkedCount > 1) {
                if (tempAdapterSize == checkedCount) {
                    mActionMode.getMenu().findItem(R.id.selectAll).setVisible(false);
                } else {
                    mActionMode.getMenu().findItem(R.id.selectAll).setVisible(isVisible());
                }
            }
            mActionMode.setTitle(String.valueOf(checkedCount));
        } else {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    }

    class ZoneListAdapter extends BaseListAdapter {

        @Override
        public int getCount() {
            return listZones.size();
        }

        @Override
        public Zone getItem(int position) {
            return listZones.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_zone_fragment, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.buttonBypass.setTag(position);
            viewHolder.icon.setTag(position);
            try {
                final String tempName = listZones.get(position).getName();
                final String zoneName = (tempName == null) ? "" : (tempName);
                //viewHolder.textViewName.setText(zoneName + listZones.get(position).getDevice().getName());
                viewHolder.textViewName.setText(zoneName);
                // viewHolder.textViewStatus.setText();
            } catch (NullPointerException e) {

                viewHolder.textViewName.setText("");
            }
            ZoneState zoneState = listZones.get(position).getZoneState();
            try {
                switch (partition.getState().getArmMode()) {
                    case HOME:
                    case AWAY:
                        viewHolder.icon.setBackgroundDrawable(getResources().getDrawable(R.drawable.zone_ready));
                        Log.d(TAG, listZones.get(position).getName() + " trippedCount: " + zoneState.getTripCount());
                        if (zoneState.getTripCount() > 0) {
                            viewHolder.trippedCount.setText(languageManager.translate("was_tripped") + ": " + zoneState.getTripCount() + "x");
                        } else {
                            viewHolder.trippedCount.setText("");
                        }
                        if (zoneState.isTripped()) {
                            viewHolder.textViewStatus.setText(languageManager.translate("tripped").toUpperCase());
                            viewHolder.textViewStatus.setTextColor(Color.RED);

                        } else if (zoneState.isReady()) {

                            viewHolder.textViewStatus.setText(languageManager.translate("ok").toUpperCase());
                            viewHolder.textViewStatus.setTextColor(Color.WHITE);
                        } else {
                            viewHolder.textViewStatus.setText(languageManager.translate("not_ok").toUpperCase());
                            viewHolder.textViewStatus.setTextColor(Color.WHITE);
                        }
                        if (zoneState.isBypassed()) {
                            viewHolder.buttonBypass.setText(languageManager.translate("bypassed").toUpperCase());

                        } else {
                            viewHolder.buttonBypass.setText("");
                        }
                        viewHolder.buttonBypass.setTextColor(getResources().getColor(R.color.light_grey));
                        viewHolder.buttonBypass.setEnabled(false);
                        break;
                    case DISARMED:
                        if (zoneState.isReady()) {
                            viewHolder.textViewStatus.setText(languageManager.translate("ready").toUpperCase());
                            viewHolder.textViewStatus.setTextColor(Color.WHITE);
                            viewHolder.icon.setBackgroundDrawable(getResources().getDrawable(R.drawable.zone_ready));
                        } else {
                            viewHolder.textViewStatus.setText(languageManager.translate("not_ready").toUpperCase());
                            viewHolder.textViewStatus.setTextColor(Color.RED);
                            viewHolder.icon.setBackgroundDrawable(getResources().getDrawable(R.drawable.zone_not_ready));
                        }
                        if (zoneState.isBypassed()) {
                            viewHolder.buttonBypass.setText(languageManager.translate("bypassed").toUpperCase());

                        } else {
                            viewHolder.buttonBypass.setText(languageManager.translate("bypass").toUpperCase());
                        }
                        viewHolder.buttonBypass.setTextColor(getResources().getColor(R.color.color_white));
                        viewHolder.buttonBypass.setEnabled(true);
                        viewHolder.trippedCount.setText(languageManager.translate("was_tripped") + ": " + zoneState.getTripCount());
                        viewHolder.trippedCount.setText("");

                        break;
                }
            } catch (Exception e) {

            }

            if (isSelected(position)) {
                convertView.setBackgroundColor(getResources().getColor(R.color.click_transparent));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.full_transparent));
            }


            return convertView;
        }

        class ViewHolder {
            @SetTypeFace("helveticaneue_ultra_light.otf")
            @InjectView(R.id.textViewZoneName)
            TextView textViewName;
            @SetTypeFace("helveticaneue_ultra_light.otf")
            @InjectView(R.id.textViewZoneStatus)
            TextView textViewStatus;
            @SetTypeFace("helveticaneue_ultra_light.otf")
            @InjectView(R.id.buttonBypass)
            Button buttonBypass;
            @InjectView(R.id.imageViewZone)
            ImageView icon;
            @SetTypeFace("helveticaneue_ultra_light.otf")
            @InjectView(R.id.textViewTripCount)
            TextView trippedCount;

            public ViewHolder(View v) {

                ButterKnife.inject(this, v);
                typeFaceUtils.applyTypefaceFor(this);

            }

            @OnClick(R.id.imageViewZone)
            public void onIconClick(View v) {
                previousUpdate = System.currentTimeMillis();
                int position = (Integer) v.getTag();
                adapter.toggleSelection(position);
                setOnActionBarMenu(adapter.getSelectedCount());


            }

            @OnClick(R.id.buttonBypass)
            public void onButtonClick(View v) {
                previousUpdate = System.currentTimeMillis();
                final int position = (Integer) v.getTag();
                final Zone zone = listZones.get(position);
                if (!zone.getZoneState().isBypassed()) {
                    zone.getZoneState().setBypassed(true);
                } else {
                    zone.getZoneState().setBypassed(false);
                }
                zonesRepository.put(zone.getUuid(), zone);
                zonesRepository.upDateByPassedZone(partition.getUuid(), zone); //TODO this is the new way to deal with zoneBypassed
                notifyDataSetChanged();

            }

        }
    }

    private final class ModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.menu_zones, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            final SparseBooleanArray sparseBooleanArray = adapter
                    .getSelectedIds();
            final int size = sparseBooleanArray.size();
            switch (menuItem.getItemId()) {
                case R.id.selectAll:
                    int tempAdapterSize = adapter.getCount();
                    for (int i = 0; i < tempAdapterSize; i++) {

                        if (!sparseBooleanArray.get(i)) {
                            adapter.toggleSelection(i);
                        }
                    }

                    final int checkedItemCount = adapter.getSelectedCount();
                    setOnActionBarMenu(checkedItemCount);
                    break;
                case R.id.delete:
                    if ((partition.getState() != null) && (partition.getState().getArmMode() != null)) {
                        if ("HOME".equals(partition.getState().getArmMode().name()) || "AWAY".equals(partition.getState().getArmMode().name())) {
                            toast(languageManager.translate("error_partition_is_armed"));
                            mActionMode.finish();

                            return true;
                        }
                    }
                    previousUpdate = System.currentTimeMillis();
                    List<String> tempList = new ArrayList<String>();
                    for (int i = 0; i < size; i++) {
                        tempList.add(listZones.get(sparseBooleanArray.keyAt(i)).getName());
                    }
                    String text1 = languageManager.translate("remove_zones_textMsg");
                    String dialogTitle = (languageManager.translate("dialog_remove_zone_title") + " (" + tempList.size() + ")");
                    String positiveText = languageManager.translate("remove");
                    String negativeText = languageManager.translate("cancel");
                    DeleteDialogHelper deleteDialogHelper = new DeleteDialogHelper(getActivity(), tempList, text1, R.drawable.ic_warning, dialogTitle, negativeText, positiveText, new DeleteDialogHelper.OnPositiveClicked() {
                        @Override
                        public void onPositiveClicked() {
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    previousUpdate = System.currentTimeMillis();
                                    for (int i = 0; i < size; i++) {

                                        final int finalI = i;
                                        baseFragmentHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showProgressDialog(languageManager.translate("removing_zone") + " " + listZones.get(sparseBooleanArray.keyAt(finalI)).getName(), false);

                                            }
                                        });
                                        if (internetConnectionHelper.isOnline()) {
                                            UUID uuidZone = listZones.get(sparseBooleanArray.keyAt(i)).getUuid();
                                            try {
                                                previousUpdate = System.currentTimeMillis();
                                                zonesRepository.removeZone(partition.getUuid(), uuidZone);
                                                zonesRepository.removeBypassedZone(partition.getUuid(), uuidZone);
                                                zonesRepository.remove(uuidZone);


                                            } catch (Exception e) {
                                                Log.d(TAG, "", e);
                                                baseFragmentHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        toast(languageManager.translate("connection_error"));
                                                    }
                                                });

                                                break;
                                            }

                                        } else {

                                            baseFragmentHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    toast(languageManager.translate("internet_error"));
                                                }
                                            });

                                            break;
                                        }

                                    }
                                    previousUpdate = System.currentTimeMillis();
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            init();
                                            dismissProgressDialog();
                                            // eventBus.post(SMFragment.REQUEST_FETCH); // TODO CHECK THIS OUT
                                        }
                                    });

                                }
                            });
                        }
                    });
                    deleteDialogHelper.show();
                    mActionMode.finish();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

            adapter.removeSelection();
            if (mActionMode == actionMode) {
                mActionMode = null;
            }
        }
    }


}