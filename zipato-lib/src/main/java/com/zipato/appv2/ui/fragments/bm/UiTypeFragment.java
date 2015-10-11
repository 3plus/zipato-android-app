/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.bm;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.helper.AssetLoaderHelper;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.UiType;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.InjectView;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 7/21/2014.
 */

public class UiTypeFragment extends BaseFragment {

    public static final String RESTORE_POS_KEY = "RESTORE_POS_KEY";
    private static final String TAG = UiTypeFragment.class.getSimpleName();
    @Inject
    EventBus eventBus;
    @InjectView(R.id.listViewBrowserLeft)
    ListView listViewLeft;
    @Inject
    AssetLoaderHelper assetLoaderHelper;
    @Inject
    List<UiType> uiTypes;
    @Inject
    Map<UiType, List<TypeReportItem>> typeMap;
    @Inject
    @Named("rooms")
    List<TypeReportItem> items;

    private LeftViewListAdapter leftViewAdapter;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        leftViewAdapter = new LeftViewListAdapter(getContext());
        listViewLeft.setAdapter(leftViewAdapter);
        if (getArguments() != null) {
            int restorePosition = getArguments().getInt(RESTORE_POS_KEY, 0);
            if ((items != null) && !items.isEmpty())
                leftViewAdapter.toggleSelection(restorePosition);
            listViewLeft.smoothScrollToPosition(restorePosition);
        }
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_ui_type;
    }

    @Override
    protected void onPostViewCreate() {

    }

    @OnItemClick(R.id.listViewBrowserLeft)
    public void onLeftItemClick(int position) {
        if (!leftViewAdapter.getSelectedIds().get(position)) {
            leftViewAdapter.toggleSelection(position);
            eventBus.post(new Event(new ObjectItemsClick(ObjectItemsClick.UI_TYPE, position), Event.EVENT_TYPE_ITEM_CLICK));
        }
    }

    @Override
    public void onResume() {
        eventBus.register(this);
        super.onResume();
        try {
            if (!uiTypes.isEmpty() && !typeMap.isEmpty())
                leftViewAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    public void onEventMainThread(Event event) {
        if (event.eventType == Event.EVENT_TYPE_LIST_VIEW_REFRESH) {
            final ObjectListRefresh objectListRefresh = (ObjectListRefresh) event.eventObject;
            if (objectListRefresh.fromTo == ObjectItemsClick.UI_TYPE) {
                if (objectListRefresh.reset) leftViewAdapter.clearSelections();
                else leftViewAdapter.toggleSingleItem(objectListRefresh.position);
                leftViewAdapter.notifyDataSetChanged();
                Log.d(TAG, "List data changed and refreshed");
            }
        }

    }

    private class LeftViewListAdapter extends BaseListAdapter {

        Context context;

        public LeftViewListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();// LOl did i did that?
        }

        @Override
        public int getCount() {
            return uiTypes.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row_browser_left, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            try {
                if (uiTypes.get(position) != null) {
                    String translation = languageManager.translate(uiTypes.get(position).getEndpointType());
                    viewHolder.name.setText(translation);
                    viewHolder.counter.setText("(" + typeMap.get(uiTypes.get(position)).size() + ')');
                    assetLoaderHelper.loadAsset(uiTypes.get(position).getEndpointType() + ".png", viewHolder.iconType);
                }

                if (getSelectedIds().get(position)) {
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.transparent)); //TODO ui_type_filter_selected
                } else {
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.color_view_controller_item_background)); // TODO ui_type_transparent
                }
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView name;
            TextView counter;
            ImageView iconType;

            public ViewHolder(View v) {
                name = (TextView) v.findViewById(R.id.textViewBrowserLeftName);
                counter = (TextView) v.findViewById(R.id.textViewBrowserLeftCounter);
                iconType = (ImageView) v.findViewById(R.id.imageViewLeft);

            }
        }
    }
}
