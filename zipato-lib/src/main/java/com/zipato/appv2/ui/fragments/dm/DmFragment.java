/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.dm;

import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.zipato.appv2.R;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.R.string;
import com.zipato.appv2.activities.BaseActivity;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.helper.DeleteDialogHelper;
import com.zipato.helper.DeleteDialogHelper.OnPositiveClicked;
import com.zipato.helper.DeviceStateHelper;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.BaseEntityType;
import com.zipato.model.BaseObject;
import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.client.RestObject;
import com.zipato.model.device.Device;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.device.DeviceState;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.Endpoint;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectLauncher;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.network.Network;
import com.zipato.model.network.NetworkRepository;
import com.zipato.model.typereport.EntityType;
import com.zipato.translation.LanguageManager;
import com.zipato.util.CollectionUtils;
import com.zipato.util.CollectionUtils.Predicate;
import com.zipato.util.Utils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 24.6.2014..
 */
public abstract class DmFragment<T extends BaseEntityType> extends BaseFragment {

    public static final String KEY_UUID = "KEY";
    public static final String KEY_BOOL = "KEY_BOOL";
    private static final HashMap<Class<? extends BaseObject>, Integer> ICON_REPO = new HashMap<>();


    static {
        ICON_REPO.put(Network.class, drawable.network_dm_icon);
        ICON_REPO.put(Device.class, drawable.device_dm_icon);
        ICON_REPO.put(Endpoint.class, drawable.endpoint_dm_icon);
        ICON_REPO.put(ClusterEndpoint.class, drawable.cluster_endpoint_dm_icon);
        ICON_REPO.put(Attribute.class, drawable.attribute_dm_icon);
    }

    protected boolean filtered;
    protected UUID parentUuid;
    protected volatile boolean isDataCollect;
    @Inject
    EventBus eventBus;
    @Inject
    InternetConnectionHelper internetConnectionHelper;
    @InjectView(id.progressBarDeviceManager)
    ProgressBar progressBar;
    @InjectView(id.listViewDeviceManager)
    ListView listViewChildren;
    ListView listViewParent;
    @Inject
    DeviceStateRepository deviceStateRepository;
    @Inject
    ExecutorService executor;
    @Inject
    NetworkRepository networkRepository;
    @Inject
    AttributeRepository attributeRepository;
    @Inject
    ClusterEndpointRepository clusterEndpointRepository;
    SwingBottomInAnimationAdapter animatedAdapter;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    LanguageManager languageManager;
    @Inject
    PreferenceHelper preferenceHelper;
    @Inject
    DeviceStateHelper deviceStateHelper;

    private ActionMode mActionMode;
    private List<T> listParent;
    private DMAdapter deviceManagerAdapter;
    private ParentAdapter parentAdapter;
    private List<T> listBaseObject;
    private ListItemClickListener listner;


    @Override
    protected int getResourceView() {
        return R.layout.fragment_device_manager;
    }

    @Override
    protected void onPostViewCreate() {
        //
    }

    @Override
    protected void onViewReady(View v) {
        if (Utils.isPreJellyBean())
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {

            ParcelUuid parcelUuid = getArguments().getParcelable(KEY_UUID);
            if (parcelUuid != null)
            parentUuid = parcelUuid.getUuid();
            filtered = getArguments().getBoolean(KEY_BOOL);
        }
        listner = (ListItemClickListener) getActivity();
        setView();
        dataCollect(false);
    }

    protected void setView() {
        progressBar.setVisibility(View.GONE);
        listViewParent = (ListView) getActivity().findViewById(id.listViewDeviceParent);
        listParent = new ArrayList<T>();
        listBaseObject = new ArrayList<T>();
        parentAdapter = new ParentAdapter();
        listViewParent.setAdapter(parentAdapter);
        deviceManagerAdapter = new DMAdapter();
        animatedAdapter = new SwingBottomInAnimationAdapter(deviceManagerAdapter);
        animatedAdapter.setAbsListView(listViewChildren);
        listViewChildren.setAdapter(animatedAdapter);
    }

//    @OnItemLongClick(R.id.listViewDeviceManager)
//    public boolean onItemLongClickLVC(AdapterView<?> parent, View view, int position, long id) {
//        deviceManagerAdapter.toggleSelection(position);
//        final int checkedCount = deviceManagerAdapter.getSelectedCount();
//        setOnActionBarMenu(checkedCount);
//        return true;
//    }

    @OnItemClick(id.listViewDeviceManager)
    public void onItemClickLVC(int position) {
        if ((mActionMode != null) && !(this instanceof NetworkFragment)) {
            deviceManagerAdapter.toggleSelection(position);
            final int checkedCount = deviceManagerAdapter.getSelectedCount();
            setOnActionBarMenu(checkedCount);
        } else {
            T t = listBaseObject.get(position);
            if (t.getChildren() != null) {
                listner.onParentClick(getChildFragmentClass(), t.getUuid());
            }

        }
    }

    @OnItemLongClick(id.listViewDeviceManager)
    public boolean onLongItemClickLVC(int position) {
        if ((mActionMode != null) && !(this instanceof NetworkFragment)) {
            deviceManagerAdapter.toggleSelection(position);
            final int checkedCount = deviceManagerAdapter.getSelectedCount();
            setOnActionBarMenu(checkedCount);
        }
        return true;
    }


    @OnItemClick(id.listViewDeviceParent)
    public void onItemClickLVP(int position) {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        listner.onChildClick(position);
    }

    protected abstract Class<? extends DmFragment<?>> getChildFragmentClass();

    protected abstract int getIndex();

    public abstract String getFragmentTag();

    protected abstract UUIDObjectRepository<T> getRepository();

    protected abstract EntityType getEntityType();


    protected void dataCollect(final boolean force) {
        if (!internetConnectionHelper.isOnline()) {
            baseFragmentHandler.post(new Runnable() {
                @Override
                public void run() {
                    toast(languageManager.translate("internet_error_refresh"));
                }
            });
            return;
        }
        ((BaseActivity) getActivity()).showIndeterminateProgress(true);

        isDataCollect = true;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (getRepository().isEmpty() || force) {
                    try {
                        networkRepository.loadTree();
                        networkRepository.write();
                    } catch (Exception e) {
                        handlerException(e, getFragmentTag());
                    }
                }

                final Collection<T> bruteList = new ArrayList<>();
                final List<T> finalTempList = new ArrayList<>();
                final List<T> tempListParent = new ArrayList<>();

                try {
                    bruteList.addAll(getRepository().values());
                    if (filtered) {
                        final Predicate<T> predicate = new Predicate<T>() {
                            @Override
                            public boolean apply(T t) {
                                if ((t.getParent() == null) || (t.getParent().getUuid() == null))
                                    return false;
                                return t.getParent().getUuid().equals(parentUuid);
                            }
                        };

                        CollectionUtils.addMatching(bruteList, finalTempList, predicate);
                        Collections.sort(finalTempList, BaseObject.ORDER_NAME_COMPARATOR);

                        if (!finalTempList.isEmpty())
                            pathGenerator(finalTempList.get(0), getIndex(), tempListParent);

                        T networks = (T) new Network();
                        networks.setName("Networks");
                        tempListParent.add(0, networks);//force first to be network which is obviously the master root
                    } else {
                        finalTempList.addAll(bruteList);
                        if (!finalTempList.isEmpty())
                            pathGenerator(finalTempList.get(0), getIndex(), tempListParent);
                        Collections.sort(finalTempList, BaseObject.ORDER_NAME_COMPARATOR);
                    }

                } catch (Exception e) {
                    //Empty

                } finally {
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            listBaseObject.clear();
                            listParent.clear();
                            listBaseObject.addAll(finalTempList);
                            listParent.addAll(tempListParent);

                            ((BaseActivity) getActivity()).showIndeterminateProgress(false);
                            isDataCollect = false;

                            parentAdapter.notifyDataSetChanged();
                            animatedAdapter.setShouldAnimate(true);
                            animatedAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        });

    }

    @SuppressWarnings("InstanceofInterfaces")
    private void pathGenerator(T t, int parentLevel, List<T> parentOut) {

        if (!(t instanceof Network)) {
            for (int i = 0; i < parentLevel; i++) {
                t = (T) t.getParent();
                parentOut.add(0, t);
            }
        }
    }

    public boolean isFiltered() {
        return filtered;
    }

    private void setOnActionBarMenu(int checkedCount) {

        if (checkedCount > 0) {
            if (mActionMode == null) {
                mActionMode = getActivity().startActionMode(new ModeCallback());
            }
            final int tempAdapterSize = deviceManagerAdapter.getCount();
            if (checkedCount > 1) {
                if (tempAdapterSize == checkedCount) {
                    mActionMode.getMenu().findItem(id.selectAll).setVisible(false);
                } else {
                    mActionMode.getMenu().findItem(id.selectAll).setVisible(isVisible());
                }
                mActionMode.getMenu().findItem(id.edit).setVisible(false);
            } else {
                mActionMode.getMenu().findItem(id.edit).setVisible(true);
            }
            mActionMode.setTitle(String.valueOf(checkedCount));
        } else {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    public void onEventMainThread(Event event) {

        switch (event.eventType) {
            case Event.EVENT_TYPE_LIST_VIEW_REFRESH:
                final ObjectListRefresh objectListRefresh = (ObjectListRefresh) event.eventObject;
                if (objectListRefresh.fromTo == ObjectItemsClick.D_MANAGER) {
                    dataCollect(true);
                }
                break;
            case Event.EVENT_TYPE_REFRESH_REQUEST:

            case Event.EVENT_TYPE_REPO_SYNCED:
                if (!isDataCollect)
                    dataCollect(true);
                BaseActivity baseActivity = (BaseActivity) getActivity();
                final SlidingMenu slidingMenu = baseActivity.getSlidingMenu();
                if ((slidingMenu != null) && slidingMenu.isMenuShowing())
                    slidingMenu.toggle();
                break;
        }

    }

    public interface ListItemClickListener {
        void onParentClick(Class<? extends DmFragment<?>> fragmentClass, UUID parentUuid);

        void onChildClick(int position);
    }

    class DMAdapter extends BaseListAdapter {

        @Override
        public int getCount() {
            return (listBaseObject == null) ? 0 : listBaseObject.size();
        }

        @Override
        public Object getItem(int position) {
            return (listBaseObject == null) ? null : listBaseObject.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_device_manager_2, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.name.setText(listBaseObject.get(position).getName() == null ? "" : listBaseObject.get(position).getName());
            Device device = null;
            if (listBaseObject.get(position) instanceof Device) {
                device = (Device) listBaseObject.get(position);
            }

            if (device != null) {
                final DeviceState state = deviceStateRepository.get(device.getUuid());
                if (state != null) {
                    if (!state.isOnline()) {
                        viewHolder.imageStatus.setVisibility(View.VISIBLE);
                        viewHolder.imageStatus.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.custom_status_offline));
                    } else if (state.isTrouble() || (state.getBatteryLevel() == -1)) {
                        viewHolder.imageStatus.setVisibility(View.VISIBLE);
                        viewHolder.imageStatus.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.custom_status_trouble_battery));
                    } else {
                        viewHolder.imageStatus.setVisibility(View.GONE);
                        viewHolder.imageStatus.setEnabled(true);
                    }
                }
            } else {
                emptyViews(viewHolder);
            }

            viewHolder.uiType.setTag(position);
            viewHolder.uiType.setBackgroundDrawable(getActivity().getResources().getDrawable(ICON_REPO.get(listBaseObject.get(position).getClass())));

            if (!listParent.isEmpty()) {
                if (isSelected(position)) {
                    viewHolder.layout.setBackgroundColor(getResources().getColor(R.color.color_view_controller_item_background_50));
                } else {
                    viewHolder.layout.setBackgroundColor(Color.TRANSPARENT);
                }

            } else {
                viewHolder.layout.setBackgroundColor(getResources().getColor(R.color.color_view_controller_item_background_50));
            }
            if (listBaseObject.get(position).getChildren() != null) {
                viewHolder.arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.arrow));
            } else {
                viewHolder.arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.drawable_transparent));
            }


            return convertView;
        }

        public void emptyViews(ViewHolder viewHolder) {
            viewHolder.imageStatus.setVisibility(View.GONE);
        }


    }

    class ViewHolder {

        @InjectView(id.textViewBrowserRightName)
        TextView name;
        @InjectView(id.imageViewRightBrowserStatus)
        ImageView imageStatus;
        @InjectView(id.imageViewRightBrowserArrow)
        ImageView arrow;
        @InjectView(id.imageViewUiType)
        ImageView uiType;
        @InjectView(R.id.layout)
        LinearLayout layout;

        ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }

        @OnClick(id.imageViewUiType)
        public void onClickUiType(final View v) {

            if (v.getTag() != null) {
                final int position = (Integer) v.getTag();
                if (!(listBaseObject.get(position) instanceof Network)) {
                    deviceManagerAdapter.toggleSelection(position);
                    final int checkedCount = deviceManagerAdapter.getSelectedCount();
                    setOnActionBarMenu(checkedCount);
                }
            }

        }
    }

    private class ParentAdapter extends android.widget.BaseAdapter {

        @Override
        public int getCount() {
            return (listParent == null) ? 0 : listParent.size();
        }

        @Override
        public Object getItem(int position) {
            return (listParent == null) ? null : listParent.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(layout.row_device_manager_2, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText((listParent.get(position).getName() == null) ? "" : listParent.get(position).getName());
            Device device = null;
            if (listParent.get(position) instanceof Device) {
                device = (Device) listParent.get(position);
            }
            if (device != null) {
                final DeviceState state = deviceStateRepository.get(device.getUuid());
                if (state != null) {
                    if (!state.isOnline()) {
                        viewHolder.imageStatus.setVisibility(View.VISIBLE);
                        viewHolder.imageStatus.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.custom_status_offline));
                    } else if (state.isTrouble() || (state.getBatteryLevel() == -1)) {
                        viewHolder.imageStatus.setVisibility(View.VISIBLE);
                        viewHolder.imageStatus.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.custom_status_trouble_battery));
                    } else {
                        viewHolder.imageStatus.setVisibility(View.GONE);
                        viewHolder.imageStatus.setEnabled(true);
                    }
                }
            } else {
                emptyViews(viewHolder);
            }

            viewHolder.uiType.setBackgroundDrawable(getActivity().getResources().getDrawable(ICON_REPO.get(listParent.get(position).getClass())));
            viewHolder.layout.setBackgroundColor(getResources().getColor(R.color.color_view_controller_item_background_50));
            if (position == (listParent.size() - 1)) {
                viewHolder.arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.arrow_down));
            } else {
                viewHolder.arrow.setBackgroundDrawable(getActivity().getResources().getDrawable(drawable.arrow));
            }
            return convertView;
        }

        public void emptyViews(ViewHolder viewHolder) {
            viewHolder.imageStatus.setVisibility(View.GONE);
        }

    }

    private final class ModeCallback implements Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.menu_dm, menu);
            return true;
        }

        private void onSelectAll() {
            final SparseBooleanArray sparseBooleanArray = deviceManagerAdapter
                    .getSelectedIds();
            int tempAdapterSize = deviceManagerAdapter.getCount();

            for (int i = 0; i < tempAdapterSize; i++) {

                if (!sparseBooleanArray.get(i)) {
                    deviceManagerAdapter.toggleSelection(i);
                }
            }
            final int checkedItemCount = deviceManagerAdapter.getSelectedCount();
            setOnActionBarMenu(checkedItemCount);
        }

        private void onReApply() {
            if (!(DmFragment.this instanceof DeviceFragment)) {

                toast(languageManager.translate("error_only_available_for_device"));
                mActionMode.finish();
                return;

            }

            final SparseBooleanArray sparseBooleanArray = deviceManagerAdapter
                    .getSelectedIds();
            final int size = sparseBooleanArray.size();

            executor.execute(new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < size; i++) {

                        try {
                            if (internetConnectionHelper.isOnline()) {

                                final int finalI = i;
                                baseFragmentHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgressDialog(languageManager.translate("reapply_des_for_device") + " " + listBaseObject.get(sparseBooleanArray.keyAt(finalI)).getName(), false);

                                    }
                                });

                                DeviceRepository deviceRepository = (DeviceRepository) getRepository();
                                final RestObject res = deviceRepository.reapplyDesc(listBaseObject.get(sparseBooleanArray.keyAt(finalI)).getUuid());
                                if (!preferenceHelper.getBooleanPref(Preference.REFRESH_ON_RESUME))
                                    preferenceHelper.putBooleanPref(Preference.REFRESH_ON_RESUME, true);

                                if ((res != null) && res.isSuccess()) {
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showProgressDialog(getResources().getString(string.reapply_des_for_device) + " " + listBaseObject.get(sparseBooleanArray.keyAt(finalI)).getName() + " " + languageManager.translate("success"), false);

                                        }
                                    });
                                } else {
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showProgressDialog(languageManager.translate("reapply_des_for_device") + " " + listBaseObject.get(sparseBooleanArray.keyAt(finalI)).getName() + " " + languageManager.translate("fail"), false);
                                            if ((res != null) && (res.getError() != null))
                                                toast(res.getError());
                                        }
                                    });
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

                        } catch (Exception e) {
                            handlerException(e, getFragmentTag());
                            break;
                        }

                    }

                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!isDataCollect)
                                dataCollect(true);
                            dismissProgressDialog();
                        }
                    });

                }
            });
        }

        private void onEdit() {
            final SparseBooleanArray sparseBooleanArray = deviceManagerAdapter
                    .getSelectedIds();

            final ConfigObject configObject = new ConfigObject(listBaseObject.get(sparseBooleanArray.keyAt(0)).getUuid(), getEntityType());
            eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_FRAGMENT, null, configObject), Event.EVENT_TYPE_LAUNCHER));
            if (mActionMode != null)
                mActionMode.finish();
        }

        private void onDelete() {
            if (!(DmFragment.this instanceof DeviceFragment)) {

                toast(languageManager.translate("error_only_device_can_be_remove"));
                mActionMode.finish();
                return;
            }

            final SparseBooleanArray sparseBooleanArray = deviceManagerAdapter
                    .getSelectedIds();
            final int size = sparseBooleanArray.size();

            List<String> tempList = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                tempList.add(listBaseObject.get(sparseBooleanArray.keyAt(i)).getName());
            }
            String dialogTitle = (languageManager.translate("dialog_remove_device_title") + " (" + tempList.size() + ")");
            String positiveText = languageManager.translate("remove");
            String negativeText = languageManager.translate("cancel");

            DeleteDialogHelper deleteDialogHelper = new DeleteDialogHelper(getActivity(), tempList, null, drawable.ic_warning, dialogTitle, negativeText, positiveText, new OnPositiveClicked() {
                @Override
                public void onPositiveClicked() {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {

                            for (int i = 0; i < size; i++) {
                                try {
                                    if (internetConnectionHelper.isOnline()) {
                                        final int finalI = i;
                                        baseFragmentHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showProgressDialog(languageManager.translate("removing_device2") + " " + listBaseObject.get(sparseBooleanArray.keyAt(finalI)).getName(), false);

                                            }
                                        });
                                        DeviceRepository deviceRepository = (DeviceRepository) getRepository();
                                        deviceRepository.removeDevice(listBaseObject.get(sparseBooleanArray.keyAt(finalI)).getUuid());
                                        if (!preferenceHelper.getBooleanPref(Preference.REFRESH_ON_RESUME))
                                            preferenceHelper.putBooleanPref(Preference.REFRESH_ON_RESUME, true);

                                    } else {
                                        baseFragmentHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                toast(languageManager.translate("internet_error"));
                                            }
                                        });

                                        break;
                                    }
                                } catch (Exception e) {
                                    handlerException(e, getFragmentTag());
                                    break;
                                }
                            }
                            try {
                                baseFragmentHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgressDialog(languageManager.translate("synchronizing"), false);
                                    }
                                });
                                RestObject resp = restTemplate.getForObject("v2/box/synchronize?ifNeeded=false&wait=true&timeout=30", RestObject.class);
                                if ((resp != null) && !resp.isSuccess())
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            toast(languageManager.translate("synch_fail"));
                                        }
                                    });
                            } catch (Exception e) {
                                handlerException(e, getFragmentTag());

                            } finally {
                                baseFragmentHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isDataCollect)
                                            dataCollect(true);
                                        dismissProgressDialog();
                                    }
                                });
                            }


                        }
                    });

                }
            });
            deleteDialogHelper.show();
            if (mActionMode != null)
                mActionMode.finish();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case id.selectAll:
                    onSelectAll();
                    break;
                case id.reapply:
                    onReApply();
                    break;
                case id.edit:
                    onEdit();
                    break;
                case id.delete:
                    onDelete();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            deviceManagerAdapter.removeSelection();
            if (mActionMode == actionMode) {
                mActionMode = null;
            }
        }
    }
}