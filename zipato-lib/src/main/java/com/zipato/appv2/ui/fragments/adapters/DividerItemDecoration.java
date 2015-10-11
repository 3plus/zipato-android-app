/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * Created by murielK on 10/1/2015.
 */
public class DividerItemDecoration extends ItemDecoration {

    public static final int HORIZONTAL_LIST = 0;
    public static final int VERTICAL_LIST = 1;

    private final Drawable divider;

    private int orientation;

    public DividerItemDecoration(Context context, int res, int orientation) {

        divider = context.getResources().getDrawable(res);
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if ((orientation != HORIZONTAL_LIST) && (orientation != VERTICAL_LIST)) {
            throw new IllegalArgumentException("invalid orientation");
        }
        this.orientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
//        if (state.getItemCount() == (state.getTargetScrollPosition() - 1))
//            return;
//        if (orientation == VERTICAL_LIST) {
//            drawVertical(c, parent);
//        } else {
//            drawHorizontal(c, parent);
//        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final LayoutParams params = (LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final LayoutParams params = (LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        super.getItemOffsets(outRect, view, parent, state);

        final int itemPosition = parent.getChildAdapterPosition(view);

        if ((itemPosition == RecyclerView.NO_POSITION) || (itemPosition == (state.getItemCount() - 1))) { // don't add divider on last item
            return;
        }

        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, divider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, divider.getIntrinsicWidth(), 0);
        }
    }
}