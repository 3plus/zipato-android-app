/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.security;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateFormat;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.B;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.model.alarm.AlarmLog;
import com.zipato.model.client.RestObject;
import com.zipato.model.event.Event;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterfork.Bind;
import butterfork.ButterFork;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by murielK on 8/27/2014.
 */
public class SecurityEventFragment extends BaseSecurityFragment {

    private static final String TAG = SecurityEventFragment.class.getSimpleName();
    private static final Map<String, String> messageBody = new HashMap<String, String>();

    static {
        messageBody.put("message", "-");
    }

    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    TypeFaceUtils typeFaceUtils;
    @Bind(B.id.headerListView)
    StickyListHeadersListView listHeadersListView;
    @Bind(B.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    HeaderListAdapter headerAdapter;
    List<AlarmLog> alarmLogList = new ArrayList<AlarmLog>();
    private boolean isCollectingFlag;

    private ActionMode mActionMode;

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_security_even;
    }

    @Override
    protected void onPostViewCreate() {
        headerAdapter = new HeaderListAdapter();
        listHeadersListView.setAdapter(headerAdapter);
        listHeadersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!alarmLogList.get(position).isNeedAck()) {
                    return true;
                }
                headerAdapter.toggleSelection(position);
                setOnActionBarMenu(headerAdapter.getSelectedCount());
                return true;
            }
        });

        listHeadersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((mActionMode != null) && alarmLogList.get(position).isNeedAck()) {
                    headerAdapter.toggleSelection(position);
                    setOnActionBarMenu(headerAdapter.getSelectedCount());
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isCollectingFlag) {
                    collectData();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        collectData();
    }

    @Override
    protected void init() {

    }

    private void collectData() {

        if (isDetached() || !checkInternet()) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        isCollectingFlag = true;
        if (!swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        alarmLogList.clear();
        headerAdapter.notifyDataSetChanged();
        executor.execute(new Runnable() {
            @Override
            public void run() {

                if ((getItem() != null) && (partitionRepository.get(getItem().getUuid()) != null)) {
                    partition = partitionRepository.get(getItem().getUuid());
                    AlarmLog[] alarmLogs = null;
                    try {
                        alarmLogs = restTemplate.getForObject("v2/alarm/partitions/{uuid}/events?needAck=false", AlarmLog[].class, partition.getUuid());
                    } catch (Exception e) {
                        Log.d(TAG, "", e);

                    } finally {
                        final AlarmLog[] finalAlarmLogs = alarmLogs;
                        baseFragmentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    alarmLogList.clear();
                                    alarmLogList.addAll(Arrays.asList(finalAlarmLogs));
                                    Collections.sort(alarmLogList, AlarmLog.DATE_COMPARATOR);
                                    headerAdapter.notifySelection();
                                    swipeRefreshLayout.setRefreshing(false);
                                    isCollectingFlag = false;
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "", e);
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        if ((event.eventType == Event.EVENT_TYPE_REFRESH_REQUEST) && !isCollectingFlag) {
            collectData();
        }
    }

    private void setOnActionBarMenu(int checkedCount) {

        if (checkedCount > 0) {
            if (mActionMode == null) {
                mActionMode = getActivity().startActionMode(new ModeCallback());
            }
            int tempAdapterSize = headerAdapter.getCount();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public class HeaderListAdapter extends BaseListAdapter implements StickyListHeadersAdapter {
        private final java.text.DateFormat timeFormat;
        private final java.text.DateFormat dateFormat;

        public HeaderListAdapter() {
            timeFormat = DateFormat.getTimeFormat(getActivity());
            dateFormat = DateFormat.getLongDateFormat(getActivity());
        }

        @Override
        public View getHeaderView(int i, View view, ViewGroup viewGroup) {
            HeaderHolder headerHolder;
            if (view == null) {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.row_security_event_header, null);
                headerHolder = new HeaderHolder(view);
                view.setTag(headerHolder);
            } else {
                headerHolder = (HeaderHolder) view.getTag();
            }

            if (alarmLogList.get(i).getTimestamp() != null) {
                headerHolder.date.setText(dateFormat.format(alarmLogList.get(i).getTimestamp()));
            } else {
                headerHolder.date.setText("-");
            }
            return view;

        }

        @Override
        public long getHeaderId(int i) {

            return (long) dateFormat.format(alarmLogList.get(i).getTimestamp()).hashCode();
        }

        @Override
        public int getCount() {
            return alarmLogList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChildHolder childHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_security_event_child, null);
                childHolder = new ChildHolder(convertView);
                convertView.setTag(childHolder);
            } else {

                childHolder = (ChildHolder) convertView.getTag();
            }

            if (alarmLogList.get(position).isNeedAck()) {
                childHolder.status.setVisibility(View.VISIBLE);
                childHolder.status.setEnabled(false);
            } else {
                childHolder.status.setVisibility(View.GONE);

            }
            childHolder.ack.setTag(position);
            if (alarmLogList.get(position).getTimestamp() != null) {
                childHolder.time.setText(timeFormat.format(alarmLogList.get(position).getTimestamp()));
            } else {
                childHolder.time.setText("-");
            }
            if (alarmLogList.get(position).getMessage() != null) {
                childHolder.messageText.setText(alarmLogList.get(position).getMessage());
            } else {

                childHolder.messageText.setText("-");
            }
            if (isSelected(position)) {
                convertView.setBackgroundColor(getResources().getColor(R.color.click_transparent));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.full_transparent));
            }

            return convertView;
        }

        class HeaderHolder {
            @SetTypeFace("helvetica_neue_light.otf")
            @Bind(B.id.textViewDate)
            TextView date;

            public HeaderHolder(View v) {

                ButterFork.bind(this, v);
                typeFaceUtils.applyTypefaceFor(this);
            }
        }

        class ChildHolder {

            @SetTypeFace("helveticaneue_ultra_light.otf")
            @Bind(B.id.textViewMessage)
            TextView messageText;
            @SetTypeFace("helveticaneue_ultra_light.otf")
            @Bind(B.id.textViewTime)
            TextView time;
            @SetTypeFace("helveticaneue_ultra_light.otf")
            @Bind(B.id.buttonAck)
            Button ack;
            @Bind(B.id.imageEventStatus)
            ImageView status;

            public ChildHolder(View v) {
                ButterFork.bind(this, v);
                typeFaceUtils.applyTypefaceFor(this);
            }
        }
    }

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.menu_security, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            final SparseBooleanArray selected = headerAdapter
                    .getSelectedIds();

            final float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (10 * scale + 0.5f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dpAsPixels;
            params.topMargin = dpAsPixels;

            int i1 = menuItem.getItemId();
            if (i1 == R.id.selectAll) {
                int tempAdapterSize = headerAdapter.getCount();
                for (int i = 0; i < tempAdapterSize; i++) {

                    if (!selected.get(i) && alarmLogList.get(i).isNeedAck()) {
                        headerAdapter.toggleSelection(i);
                    }
                }

                final int checkedItemCount = headerAdapter
                        .getSelectedCount();
                setOnActionBarMenu(checkedItemCount);

            } else if (i1 == R.id.Acknowledge) {
                final SparseBooleanArray sparseBooleanArray = headerAdapter.getSelectedIds();
                final int size = sparseBooleanArray.size();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final java.text.DateFormat dateFormat = DateFormat.getLongDateFormat(getActivity());
                        final java.text.DateFormat timeFormat = DateFormat.getTimeFormat(getActivity());
                        for (int i = 0; i < size; i++) {
                            final int finalI = i;
                            baseFragmentHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showProgressDialog(languageManager.translate("acknowledging_event") + ": " +
                                                               dateFormat.format(alarmLogList.get(sparseBooleanArray.keyAt(finalI)).getTimestamp()) + ", " +
                                                               timeFormat.format(alarmLogList.get(sparseBooleanArray.keyAt(finalI)).getTimestamp()), true);

                                }
                            });
                            if (internetConnectionHelper.isOnline()) {

                                try {
                                    Log.d(TAG, "Sending acknowledgement...");
                                    RestObject resp = restTemplate.postForObject("v2/alarm/partitions/{uuidPartition }/events/{evenId}", messageBody,
                                                                                 RestObject.class, partition.getUuid(),
                                                                                 alarmLogList.get(sparseBooleanArray.keyAt(finalI)).getId());
                                    if (resp != null) {

                                        Log.d(TAG, "Sent acknowledgement is success? " + resp.isSuccess());
                                        if (resp.isSuccess()) {

                                        } else {
                                            Log.d(TAG, "errorMessage: " + resp.getError());
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                        isCollectingFlag = false;

                        baseFragmentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                                collectData();

                            }
                        });
                    }
                });
                // Close CAB
                mActionMode.finish();

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            headerAdapter.removeSelection();
            if (mActionMode == actionMode) {
                mActionMode = null;
            }
        }
    }

}