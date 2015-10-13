/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.B;
import com.zipato.appv2.R;
import com.zipato.appv2.activities.AlarmTriggerActivity;
import com.zipato.appv2.activities.ShowVCMenu;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.appv2.ui.fragments.controller.ViewControllerLogic;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.UUIDObject;
import com.zipato.model.alarm.ArmMode;
import com.zipato.model.alarm.Partition;
import com.zipato.model.alarm.PartitionRepository;
import com.zipato.model.alarm.PartitionState;
import com.zipato.model.alarm.Zone;
import com.zipato.model.alarm.ZoneState;
import com.zipato.model.alarm.ZonesRepository;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.util.TagFactoryUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterfork.Bind;
import butterfork.OnClick;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 8/6/2015.
 */
@ViewType(TypeViewControllerFactory.VC_ID_SECURITY)
public class VCSecurity extends AbsBaseSimpleStatus implements ViewControllerLogic {

    private static final String TAG = TagFactoryUtils.getTag(VCSecurity.class);
    private static final long DELAY_MILLIS = 1000L;

    @Inject
    ZonesRepository zonesRepository;
    @Inject
    PartitionRepository partitionRepository;
    @Inject
    ApiV2RestTemplate restTemplate;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butArmHome)
    TextView textViewArmHome;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butArmAway)
    TextView textViewArmAway;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("disarm")
    @Bind(B.id.butDisarm)
    TextView textViewDisarm;
    private int logicQueueID;


    public VCSecurity(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        final String armHome = languageManager.translate("arm") + " " + languageManager.translate("home");
        final String armAway = languageManager.translate("arm") + " " + languageManager.translate("away");
        textViewArmHome.setText(armHome);
        textViewArmAway.setText(armAway);

    }

    private static void sendArmRequest(ArmMode armMode, UUID partition, Context context) {
        final Intent intent = new Intent(context, AlarmTriggerActivity.class);
        final Bundle bundle = new Bundle(2);
        bundle.putSerializable(AlarmTriggerActivity.KEY_BUNDLE_UUID, partition);
        bundle.putSerializable(AlarmTriggerActivity.KEY_BUNDLE_ARM_MODE, armMode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private boolean checkPartition(final Partition partition, boolean isHome) {
        final PartitionState state = partition.getState();

        if ((state.getArmMode() != null) && "DISARMED".equalsIgnoreCase(partition.getState().getArmMode().name())) {
            if (!checkZonesReady(partition, isHome)) {
                Toast.makeText(getAdapter().getContext(), languageManager.translate("partition_not_ready"), Toast.LENGTH_SHORT).show();
                return false;
            } else
                return true;
        } else {
            Toast.makeText(getAdapter().getContext(), languageManager.translate("error_please_disarm_first"), Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private Partition getPartition() {
        final GenericAdapter genericAdapter = getAdapter();
        if (genericAdapter == null)
            return null;
        final TypeReportItem typeReportItem = getTypeReportItem();
        if (typeReportItem == null)
            return null;
        return partitionRepository.get(typeReportItem.getUuid());
    }

    @OnClick(B.id.butArmAway)
    public void onAwayClick(View v) {
        final Partition partition = getPartition();
        final GenericAdapter genericAdapter = getAdapter();
        if ((partition == null) || (genericAdapter == null))
            return; // error message??
        if (!checkPartition(partition, false))
            return;

        //TODO should i change the text status here to disarm??
        sendArmRequest(ArmMode.AWAY, partition.getUuid(), genericAdapter.getContext());
        genericAdapter.disableItemUpdate(partition.getUuid());
        genericAdapter.resetUpdate(DELAY_MILLIS, partition.getUuid());
    }

    @OnClick(B.id.butDisarm)
    public void onDisarmClick(View v) {
        final Partition partition = getPartition();
        final GenericAdapter genericAdapter = getAdapter();
        if ((partition == null) || (genericAdapter == null))
            return; // error message??

        //TODO should i change the text status here to disarm??
        sendArmRequest(ArmMode.DISARMED, partition.getUuid(), genericAdapter.getContext());
        genericAdapter.disableItemUpdate(partition.getUuid());
        genericAdapter.resetUpdate(DELAY_MILLIS, partition.getUuid());
    }

    @OnClick(B.id.butArmHome)
    public void onHomeClick(View v) {
        final Partition partition = getPartition();
        final GenericAdapter genericAdapter = getAdapter();

        if ((partition == null) || (genericAdapter == null))
            return; // error message??
        if (!checkPartition(partition, true))
            return;

        //TODO should i change the text status here to disarm??
        sendArmRequest(ArmMode.HOME, partition.getUuid(), genericAdapter.getContext());
        genericAdapter.disableItemUpdate(partition.getUuid());
        genericAdapter.resetUpdate(DELAY_MILLIS, partition.getUuid());
    }

    private void clearZones() {
        //zonesRepository.clear();
        // zonesRepository.clearBypassed();
    }

    private void updateZoneState(ZoneState[] zoneStates, UUIDObject partition) {
        if ((zoneStates == null) || (zoneStates.length == 0))
            return;

        int arraySize = zoneStates.length;
        for (Zone zone : partitionRepository.get(partition.getUuid()).getZones()) {
            Log.d(TAG, String.format("applying state for zone: %s zoneState arraySize: %d", (zone.getName() == null) ? zone.getUuid() : zone.getName(), arraySize));
            for (int i = 0; i < arraySize; i++) {
                if (zone.getUuid().equals(zoneStates[i].getUuid())) {
                    zone.setZoneState(zoneStates[i]);
                    Log.d(TAG, "zoneState applied ");
                    if ((arraySize - i) > 1) {
                        Log.d(TAG, String.format("swapping state to last position... array size %d, index i %d ", arraySize, i));
                        for (int g = i; g < (arraySize - 1); g++) {
                            ZoneState tempZoneState = zoneStates[g];
                            zoneStates[g] = zoneStates[g + 1];
                            zoneStates[g + 1] = tempZoneState;
                        }
                    }
                    arraySize--;
                }
            }
        }
        zonesRepository.addAll(partitionRepository.get(partition.getUuid()).getZones());
        updateBypassedZones(partition.getUuid());
    }

    private void updateBypassedZones(UUID partition) {
        final List<UUID> listBypassedZones = zonesRepository.getBypassedZones(partition);
        if ((listBypassedZones == null) || listBypassedZones.isEmpty())
            return;
        for (Zone zone : zonesRepository.values()) {
            Log.d(TAG, "Zone: " + zone.getName() + " isBypassed?: " + zone.getZoneState().isBypassed());
            if (listBypassedZones.contains(zone.getUuid())) {
                Log.d(TAG, "Zone: " + zone.getName() + " is in the bypassZones List");
                zone.getZoneState().setBypassed(true);
            } else if (zone.getZoneState().isBypassed()) {
                zonesRepository.addBypassedZone(partition, zone.getUuid());
                Log.d(TAG, "Zone: " + zone.getName() + " added to armRequest bypassZone");
            }
        }

    }

    private boolean checkZonesReady(final Partition partition, boolean isArmHome) {
        for (Zone zone : partition.getZones()) {
            if (zonesRepository.get(zone.getUuid()) == null)
                continue;
            final Zone zoneFinal = zonesRepository.get(zone.getUuid());
            if (zoneFinal.getZoneState() == null)
                continue;

            if (!zoneFinal.getZoneState().isReady() && !zonesRepository.isZoneBypassed(partition.getUuid(), zoneFinal.getUuid())) {
                if (isArmHome) {
                    if ((zoneFinal.getConfig() != null) && "PERIMETER".equals(zoneFinal.getConfig().getType()))
                        return false;
                    else
                        continue;
                }
                return false;
            }

        }
        return true;
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        final TypeReportItem item = (TypeReportItem) object;
        final Partition partition = partitionRepository.get(item.getUuid());

        if ((partition == null) || (partition.getState() == null)) { // by default the controller will be ready or in loading when i get no state/no partition ??? probably because it is loading partitions from server!!
            textViewValue.setText("");
            textViewAtrName.setText(capitalizer(languageManager.translate("loading_box")));
            textViewArmHome.setTextColor(Color.WHITE);
            textViewValue.setTextColor(Color.WHITE);
            textViewArmAway.setTextColor(Color.WHITE);
            return;
        }

        Log.d(TAG, String.format("binding partition : %s", (partition.getName() == null) ? partition.getUuid() : partition.getName()));
        try {
            final String state = partition.getState().getArmMode().toString();
            Log.d(TAG, String.format("partition state: %s", state));
            if ("DISARMED".equalsIgnoreCase(state)) {
                textViewValue.setTextColor(Color.WHITE);
                textViewValue.setText(capitalizer(languageManager.translate(state)));

                if (!checkZonesReady(partition, true))  // check if i can arm Away
                    textViewArmHome.setTextColor(Color.RED);
                else
                    textViewArmHome.setTextColor(Color.WHITE);

                if (!checkZonesReady(partition, false))  // check if i can arm Home
                    textViewArmAway.setTextColor(Color.RED);
                else
                    textViewArmAway.setTextColor(Color.WHITE);

            } else {
                final PartitionState pState = partition.getState();
                textViewValue.setTextColor(Color.RED);
                if (pState.isRinging())
                    textViewValue.setText(capitalizer(languageManager.translate("ringing")));
                else if (pState.isTripped())
                    textViewValue.setText(capitalizer(languageManager.translate("tripped")));
                else {
                    textViewValue.setText(languageManager.translate(state.toLowerCase()));
                    textViewValue.setTextColor(Color.WHITE);
                }
            }
        } catch (Exception e) {
            textViewValue.setText(capitalizer(languageManager.translate("disarmed"))); //instead of ready
            textViewValue.setTextColor(Color.WHITE);
        }
    }

    @Override
    public void setLogicQueueID(int logicQueueID) {
        this.logicQueueID = logicQueueID;
    }

    @Override
    protected boolean isCustomUnit() {
        return false;
    }

    @Override
    protected String getCustomUnit(Attribute attr) {
        return null;
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return ((item == null) || (item.getAttributes() == null)) ? -1 : item.getIndexOfID(BaseTypesFragment.MODE_ALARM);
    }

    @Override
    public boolean hasLogic() {
        return true;
    }


    @Override
    public void run() {
        final ThreadLocal<Integer> localLogicID = new ThreadLocal<>();
        localLogicID.set(logicQueueID);
        final int viewTye = R.layout.view_controller_security;
        final GenericAdapter genericAdapter = getAdapter();
        boolean isSuccess = true;
        try {

            Log.d(TAG, "=== Starting security logic updater.... ===");
            partitionRepository.fetchAll();
            Log.d(TAG, "All partition fetch... updating all zones...");

            // clearZones();
            for (Partition p : partitionRepository.values()) {
                Log.d(TAG, String.format("updating zone for partition : %s", (p.getName() == null) ? p.getUuid().toString() : p.getName()));
                final int position = genericAdapter.findTypeReportItemPos(new TypeReportKey(p.getUuid(), EntityType.ENDPOINT));
                if (position == -1)
                    continue;

                final ZoneState[] states = restTemplate.getForObject("v2/alarm/partitions/{uuid}/zones/statuses", ZoneState[].class, p.getUuid());
                updateZoneState(states, p);

                genericAdapter.onItemRefresh(viewTye, localLogicID.get(), position);
            }

            Log.d(TAG, "=== security logic updater DONE!!! ===");

        } catch (Exception e) {
            Log.d(TAG, "", e);
            isSuccess = false;
        } finally {
            if (genericAdapter != null) {
                if (isSuccess)
                    genericAdapter.logicExecuted(viewTye, !isSuccess, localLogicID.get());
                else genericAdapter.logicFailExecution(viewTye, localLogicID.get());
            }
        }
    }

    @Override
    protected boolean handleMultiAttr() {
        return false;
    }

    @Override
    protected String[] getListMenu() {
        return new String[]{languageManager.translate("zones"), languageManager.translate("configuration"), languageManager.translate("change_icon"), languageManager.translate("event")};
    }

    @Override
    protected void handleWhichMenu(Context context, TypeReportKey key, int which) {
        switch (which) {
            case 0:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_ZONES);
                break;
            case 1:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_CONFIG);
                break;
            case 2:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_CHANGE_ICON);
                break;
            case 3:
                ShowVCMenu.show(context, key, ShowVCMenu.SHOW_ID_EVENT_SECURITY);
                break;
        }
    }

}
