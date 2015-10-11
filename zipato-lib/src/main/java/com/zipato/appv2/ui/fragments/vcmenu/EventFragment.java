/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.vcmenu;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.helper.AttributesHelper;
import com.zipato.model.attribute.AttrLogValue;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.attribute.AttributesLog;
import com.zipato.model.event.Event;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by murielK on 7/29/2014.
 */

public class EventFragment extends BaseTypesFragment {
    private static final String TAG = EventFragment.class.getSimpleName();
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    AttributesHelper attributesHelper;
    @Inject
    TypeFaceUtils typeFaceUtils;

    @Bind(B.id.headerListView)
    StickyListHeadersListView listHeadersListView;
    @Bind(B.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    HeaderListAdapter headerAdapter;
    List<AttrLogValue> attrLogValueList = new ArrayList<AttrLogValue>();
    private boolean isCollectingFlag;
    private boolean isPaused;

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_event;
    }

    @Override
    protected void onPostViewCreate() {
        headerAdapter = new HeaderListAdapter();
        listHeadersListView.setAdapter(headerAdapter);
        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isCollectingFlag) collectData();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        collectData();
    }

    private void collectData() {

        if (isDetached() || !restTemplate.isAuthenticated() || !checkInternet()) {
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            return;
        }

        isCollectingFlag = true;

        attrLogValueList.clear();
        headerAdapter.notifySelection();
        if (!swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(true);

        executor.execute(new Runnable() {
            @Override
            public void run() {

                while (!isPaused && !isDetached() && isCollectingFlag) {

                    final List<AttrLogValue> tempAttrLogValue = new ArrayList<AttrLogValue>();

                    try {

                        for (Attribute attribute : getItem().getAttributes()) {
                            AttributesLog attributesLog = null;
                            try {
                                attributesLog = restTemplate.getForObject("v2/log/attribute/{uuid}?count=50&order=desc", AttributesLog.class, attribute.getUuid());
                            } catch (Exception e) {
                                Log.d(TAG, "", e);
                            }
                            if ((attributesLog != null) && (attributesLog.getValues() != null)) {
                                int size = attributesLog.getValues().length;
                                for (int i = 0; i < size; i++) {

                                    attributesLog.getValues()[i].setAttrName(attribute.getName());
                                    attributesLog.getValues()[i].setNameEntity(getItem().getName());
                                    attributesLog.getValues()[i].setUuid(attribute.getUuid());
                                }
                                tempAttrLogValue.addAll(Arrays.asList(attributesLog.getValues()));
                            }
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "", e);
                    } finally {
                        if (!tempAttrLogValue.isEmpty())
                            Collections.sort(tempAttrLogValue, AttrLogValue.DATE_COMPARATOR);

                        baseFragmentHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    attrLogValueList.clear();
                                    attrLogValueList.addAll(tempAttrLogValue);
                                    headerAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
                                    isCollectingFlag = false;
                                } catch (Exception e) {
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
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        if ((event.eventType == Event.EVENT_TYPE_REPO_SYNCED) && !isCollectingFlag) {
            collectData();
        }
        //  Log.d(TAG, "Zones update fail: canUpdate? " + canUpdate + " Current delay :" + (System.currentTimeMillis() - previousUpdate) + " event ID: " + onUpdate);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public class HeaderListAdapter extends BaseListAdapter implements StickyListHeadersAdapter {

        private final java.text.DateFormat timeFormat;
        private final java.text.DateFormat dateFormat;

        public HeaderListAdapter() {
            timeFormat = DateFormat.getTimeFormat(getContext());
            dateFormat = DateFormat.getLongDateFormat(getContext());

        }


        @Override
        public View getHeaderView(int i, View view, ViewGroup viewGroup) {
            HeaderHolder headerHolder;
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.row_security_event_header, null);
                headerHolder = new HeaderHolder(view);
                view.setTag(headerHolder);
            } else {
                headerHolder = (HeaderHolder) view.getTag();
            }
            headerHolder.date.setText(dateFormat.format(attrLogValueList.get(i).getT()));
            return view;
        }

        @Override
        public long getHeaderId(int i) {
            return (long) dateFormat.format(attrLogValueList.get(i).getT()).hashCode();
        }

        @Override
        public int getCount() {
            return attrLogValueList.size();
        }

        @Override
        public AttrLogValue getItem(int position) {
            return attrLogValueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChildViewHolder childHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_controller_event, null);
                childHolder = new ChildViewHolder(convertView);
                convertView.setTag(childHolder);
            } else {

                childHolder = (ChildViewHolder) convertView.getTag();
            }

            AttrLogValue attrLogValue = attrLogValueList.get(position);
            childHolder.time.setText(timeFormat.format(attrLogValue.getT()));
            if (attrLogValue.getNameEntity() != null)
                childHolder.entityName.setText(attrLogValue.getNameEntity());
            if (attrLogValue.getAttrName() != null)
                childHolder.attrName.setText(languageManager.translate(attrLogValue.getAttrName().toLowerCase()));
            if (attrLogValue.getV() != null)
                childHolder.attrValue.setText(attributesHelper.attrValueResolver(attributeRepository.get(attrLogValue.getUuid()).getUuid(), attrLogValue.getV().toString()));

            return convertView;
        }
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

    class ChildViewHolder {
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @Bind(B.id.textView)
        TextView entityName;
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @Bind(B.id.textView2)
        TextView time;
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @Bind(B.id.textView3)
        TextView attrName;
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @Bind(B.id.textView4)
        TextView attrValue;

        public ChildViewHolder(View v) {

            ButterFork.bind(this, v);
            typeFaceUtils.applyTypefaceFor(this);
        }
    }
}

