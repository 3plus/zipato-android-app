/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters;

import android.util.SparseBooleanArray;
import android.widget.BaseAdapter;

import com.zipato.helper.SelectableAdapterHelper;
import com.zipato.helper.SelectableAdapterHelperImp;
import com.zipato.helper.SelectableAdapterHelperImp.SelectableAdapter;

/**
 * Created by murielK on 8/6/2014.
 */
public abstract class BaseListAdapter extends BaseAdapter implements SelectableAdapterHelper, SelectableAdapter {

    private final SelectableAdapterHelper selectableListAdapterHelper;

    protected BaseListAdapter() {
        selectableListAdapterHelper = new SelectableAdapterHelperImp(this);
    }

    @Override
    public void toggleSelection(int position) {
        selectableListAdapterHelper.toggleSelection(position);
    }

    @Override
    public void toggleSingleItem(int position) {
        selectableListAdapterHelper.toggleSingleItem(position);
    }

    @Override
    public void removeSelection() {
        selectableListAdapterHelper.removeSelection();
    }

    @Override
    public void clearSelections() {
        selectableListAdapterHelper.clearSelections();
    }

    @Override
    public int getSelectedCount() {
        return selectableListAdapterHelper.getSelectedCount();
    }

    @Override
    public boolean isSelected(int position) {
        return selectableListAdapterHelper.isSelected(position);
    }

    @Override
    public SparseBooleanArray getSelectedIds() {
        return selectableListAdapterHelper.getSelectedIds();
    }

    @Override
    public void notifySelection() {
        notifyDataSetChanged();
    }

    @Override
    public void notifySelection(int position) {
        notifyDataSetChanged();
    }
}
