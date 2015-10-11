/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.util.SparseBooleanArray;

/**
 * Created by murielK on 9/18/2015.
 */
public class SelectableAdapterHelperImp implements SelectableAdapterHelper {

    private final SelectableAdapter adapter;
    private final SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

    public SelectableAdapterHelperImp(SelectableAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void toggleSelection(int position) {
        selectView(position, !sparseBooleanArray.get(position));
    }

    @Override
    public void toggleSingleItem(int position) {
        final int prev = sparseBooleanArray.keyAt(0);
        if ((position == prev) && (sparseBooleanArray.size() != 0))
            return;

        sparseBooleanArray.delete(prev);
        adapter.notifySelection(prev);
        sparseBooleanArray.clear();
        sparseBooleanArray.put(position, true);
        adapter.notifySelection(position);
    }

    @Deprecated
    @Override
    public void removeSelection() {
        sparseBooleanArray.clear();
        adapter.notifySelection();
    }

    private void selectView(int position, boolean value) {
        if (value) {
            sparseBooleanArray.put(position, value);
        } else {
            sparseBooleanArray.delete(position);
        }
        adapter.notifySelection(position);
    }

    @Override
    public void clearSelections() {
        sparseBooleanArray.clear();
        adapter.notifySelection();
    }

    @Override
    public int getSelectedCount() {
        return sparseBooleanArray.size();
    }

    @Override
    public boolean isSelected(int position) {
        return sparseBooleanArray.get(position);
    }

    @Override
    public SparseBooleanArray getSelectedIds() {
        return sparseBooleanArray;
    }

    public interface SelectableAdapter {
        void notifySelection(int position);

        void notifySelection();
    }
}
