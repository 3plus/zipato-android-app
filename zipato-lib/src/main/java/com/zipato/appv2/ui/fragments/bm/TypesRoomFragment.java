/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.bm;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.RecycledViewPool;
import android.util.Log;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.DividerItemDecoration;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapterImp;
import com.zipato.customview.CustomRecyclerView;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterfork.Bind;
import butterfork.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 7/14/2015.
 */
public class TypesRoomFragment extends BaseFragment {

    private static final String TAG = TagFactoryUtils.getTag(TypesRoomFragment.class);
    private static final long ADAPTER_REFRESH_DELAY_MILLIS = 400L;
    private static final long DELAY_TO_UPDATE_SINCE_LAST = 1000L;

    @Bind(B.id.recyclerView)
    CustomRecyclerView recyclerView;
    @Bind(B.id.fbTypesRoom)
    FloatingActionButton fab;
    @Inject
    EventBus eventBus;
    @Inject
    @Named("rooms")
    List<TypeReportItem> roomTypes;
    GenericAdapterImp adapter;
    private Handler handler;

    private long longLastRefresh;
    private boolean postponeNotifyAdapter;
    private boolean scrolling;
    private final Runnable postRefreshAdapter = new Runnable() {
        @Override
        public void run() {
            if (scrolling) {
                Log.d(TAG, "postponed will be re-postpone as the user is still scrolling");
                return;
            }

            if ((adapter != null)) {
                Log.d(TAG, "postponed called of dataHasChanged");
                adapter.dataHasChangedNotify();
                longLastRefresh = System.currentTimeMillis();
                postponeNotifyAdapter = false;
            }
        }
    };

    private boolean wasPaused;

    @OnClick(B.id.fbTypesRoom)
    public void onClick(View v) {
        eventBus.post(new Event(new ObjectItemsClick(ObjectItemsClick.FAVORITE_BUTTON), Event.EVENT_TYPE_ITEM_CLICK));
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handler = new Handler();
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_right_content_rooms;
    }

    @Override
    protected void onPostViewCreate() {
        initRecyclerView();
        setAdapterToRecyclerView();
        fab.attachToRecyclerView(recyclerView);
    }

    private void setAdapterToRecyclerView() {
        adapter = new GenericAdapterImp<>(getContext(), roomTypes, recyclerView);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        // Log.d(TAG, "scroll state dragging");
                        scrolling = true;
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        //Log.d(TAG, "scroll state settling");
                        scrolling = true;
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        // Log.d(TAG, "scroll state idle");
                        scrolling = false;
                        if (postponeNotifyAdapter) {// smart recyclerView reset to reduce lag when user is scrolling due to the aggressive reset mechanism triggered by zipaService
                            handler.removeCallbacks(postRefreshAdapter);
                            handler.postDelayed(postRefreshAdapter, ADAPTER_REFRESH_DELAY_MILLIS);
                        }
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        if (!fab.isVisible())
            fab.show(true);
    }

    private void initRecyclerView() {
        LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), R.drawable.line_separator_empty, DividerItemDecoration.VERTICAL_LIST));
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPaused = true;
        eventBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.reset();
        roomTypes.clear();
    }


    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        if (wasPaused) {
            adapter.notifyDataSetChanged();
            wasPaused = false;
        }
    }

    /* unfortunately i cannot just notifyDataSetChanged here for many reasons!!!
    adapter.enableUpdate(false);
    adapter.reset();
    recyclerView.getRecycledViewPool().clear();
    adapter.notifyDataSetChanged();
    recyclerView.smoothScrollToPosition(0);
    adapter.enableUpdate(true);
     */
    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_LIST_VIEW_REFRESH:
                final ObjectListRefresh objectListRefresh = (ObjectListRefresh) event.eventObject;
                if (objectListRefresh.fromTo == ObjectItemsClick.ROOM_TYPES) {
                    if (roomTypes.isEmpty() && (adapter != null)) {
                        adapter.notifyDataSetChanged();
                        return;
                    }
                    GenericAdapterImp oldAdapter = adapter;
                    setAdapterToRecyclerView();
                    if (oldAdapter != null)
                        oldAdapter.reset();
                }
                break;
            case Event.EVENT_TYPE_AUTO_UPDATER_SERVICE:
                Log.d(TAG, "received ON_UPDATE eventBus");
                if ((System.currentTimeMillis() - longLastRefresh) < DELAY_TO_UPDATE_SINCE_LAST) {
                    Log.d(TAG, "last update happened not long ago returning");

                } else if (!postponeNotifyAdapter && scrolling) {

                    Log.d(TAG, "postponing because scrolling");
                    postponeNotifyAdapter = true;

                } else if (!scrolling) {

                    Log.d(TAG, "calling dataHasChangedNotify");
                    handler.removeCallbacks(postRefreshAdapter);
                    adapter.dataHasChangedNotify();
                }
                break;
        }
    }

}
