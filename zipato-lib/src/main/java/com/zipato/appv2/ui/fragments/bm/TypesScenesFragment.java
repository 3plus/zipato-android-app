/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.bm;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.RecycledViewPool;

import com.melnykov.fab.FloatingActionButton;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.DividerItemDecoration;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapterImp;
import com.zipato.customview.CustomRecyclerView;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.typereport.TypeReportItem;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 10/6/2015.
 */
public class TypesScenesFragment extends BaseFragment {

    @InjectView(R.id.recyclerViewScenes)
    CustomRecyclerView recyclerView;
    @InjectView(R.id.fbTypesscenes)

    FloatingActionButton fab;
    @Inject
    EventBus eventBus;

    @Inject
    @Named("scenes")
    List<TypeReportItem> scenesTypes;
    @Inject
    @Named("typeReportPool")
    RecycledViewPool recycledViewPool;


    GenericAdapterImp adapter;

    private boolean wasPaused;

    @Override
    protected int getResourceView() {
        return R.layout.fragment_right_content_scenes;
    }

    @Override
    protected void onPostViewCreate() {
        initRecyclerView();
        fab.hide();
    }

    private void initRecyclerView() {
        LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(4);
        recyclerView.setRecycledViewPool(recycledViewPool);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), R.drawable.line_separator_empty, DividerItemDecoration.VERTICAL_LIST));
        adapter = new GenericAdapterImp(getContext(), scenesTypes, recyclerView);
        recyclerView.setAdapter(adapter);
    }

    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_LIST_VIEW_REFRESH:
                final ObjectListRefresh objectListRefresh = (ObjectListRefresh) event.eventObject;
                if (objectListRefresh.fromTo == ObjectItemsClick.SCENES_TYPE) {
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(0);
                }
                //TODO show the add new device here
                break;
            case Event.EVENT_TYPE_HIDE_ADD_NEW_ON_SCENES:
                //TODO hide the add new device here
                break;
        }
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
        scenesTypes.clear();
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
}
