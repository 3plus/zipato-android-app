/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.helper;

import android.util.SparseBooleanArray;

/**
 * Created by murielK on 9/18/2015.
 */
public interface SelectableAdapterHelper {

    public void toggleSelection(int position);

    public void toggleSingleItem(int position);

    public void removeSelection();

    public void clearSelections();

    public int getSelectedCount();

    public boolean isSelected(int position);

    public SparseBooleanArray getSelectedIds();
}
