/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.color;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import butterfork.Bind;
import butterfork.OnClick;
import butterfork.OnLongClick;

/**
 * Created by Mur0 on 8/22/2015.
 */
public abstract class AbsPagePointerAdapter extends AbsHeader {

    private static final String TAG = TagFactoryUtils.getTag(AbsIR.class);
    private static final String VC_CACHE_ENTRY_PAGE = "VC_CACHE_ENTRY_PAGE";
    private static final int BUTTON_PER_PAGE_MAX_COUNT = 6;
    private static final long PP_RESET_UPDATE_DELAY = 5000L;
    private final SparseArray<TextView> viewMap = new SparseArray<>(); //

    @Bind(B.id.butNext)
    ImageButton butNext;
    @Bind(B.id.butPrev)
    ImageButton butPrev;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butIndex0)
    TextView index0;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butIndex1)
    TextView index1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butIndex2)
    TextView index2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butIndex3)
    TextView index3;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butIndex4)
    TextView index4;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butIndex5)
    TextView index5;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewPage)
    TextView textViewPage;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewNPages)
    TextView textViewNPages;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewSeparator)
    TextView textViewSeparator;

    private int page;

    public AbsPagePointerAdapter(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        init();
    }

    public abstract Animation[] getOnNextAnimation();

    public abstract Animation[] getOnPrevAnimation();

    public abstract Animation[] getOnBindAnimation();

    public abstract int getPointerCount();

    public abstract String getLabelForPointer(int pointer);

    public abstract boolean isPointerEnable(int pointer);

    public abstract void handleViewClick(TextView textView);

    public abstract void handleLongClick(TextView textView);

    public abstract void onPreBind(TypeReportItem item);

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        final TypeReportItem item = (TypeReportItem) object;
        onPreBind(item);
        final Object cache = getValueFromVCCache(item.getKey(), VC_CACHE_ENTRY_PAGE);
        if (cache != null)
            page = (Integer) cache;

        verifyPage();
        applyAllTextEffect(getOnBindAnimation());
    }

    private void verifyPage() {
        if (page > getPointerCount()) {
            page = getMaxNumOfPages() - 1;
        }
        page = (page < 0) ? 0 : page;
    }

    protected void delayedUpdates() {
        final GenericAdapter adapter = getAdapter();
        if (adapter == null)
            return;

        adapter.enableUpdate(false);
        adapter.resetUpdate(PP_RESET_UPDATE_DELAY);
    }

    @OnClick(B.id.butNext)
    public void onClickNext() {
        delayedUpdates();
        pageNext();
    }

    @OnClick(B.id.butPrev)
    public void onClickPrev(View v) {
        delayedUpdates();
        pagePrev();
    }

    @OnClick(B.id.butIndex0)
    public void onClickButIndex0(View v) {
        delayedUpdates();
        handleViewClick((TextView) v);
    }

    @OnClick(B.id.butIndex1)
    public void onClickButIndex1(View v) {
        delayedUpdates();
        handleViewClick((TextView) v);
    }

    @OnClick(B.id.butIndex2)
    public void onClickButIndex2(View v) {
        delayedUpdates();
        handleViewClick((TextView) v);
    }

    @OnClick(B.id.butIndex3)
    public void onClickButIndex3(View v) {
        delayedUpdates();
        handleViewClick((TextView) v);
    }

    @OnClick(B.id.butIndex4)
    public void onClickButIndex4(View v) {
        delayedUpdates();
        handleViewClick((TextView) v);
    }

    @OnClick(B.id.butIndex5)
    public void onClickButIndex5(View v) {
        delayedUpdates();
        handleViewClick((TextView) v);
    }

    @OnLongClick(B.id.butIndex0)
    public boolean onLongClickButIndex0(View v) {
        delayedUpdates();
        handleLongClick((TextView) v);
        return true;
    }

    @OnLongClick(B.id.butIndex1)
    public boolean onLongClickButIndex1(View v) {
        delayedUpdates();
        handleLongClick((TextView) v);
        return true;
    }

    @OnLongClick(B.id.butIndex2)
    public boolean onLongClickButIndex2(View v) {
        delayedUpdates();
        handleLongClick((TextView) v);
        return true;
    }

    @OnLongClick(B.id.butIndex3)
    public boolean onLongClickButIndex3(View v) {
        delayedUpdates();
        handleLongClick((TextView) v);
        return true;
    }

    @OnLongClick(B.id.butIndex4)
    public boolean onLongClickButIndex4(View v) {
        delayedUpdates();
        handleLongClick((TextView) v);
        return true;
    }

    @OnLongClick(B.id.butIndex5)
    public boolean onLongClickButIndex5(View v) {
        delayedUpdates();
        handleLongClick((TextView) v);
        return true;
    }


    private void pageNext() {
        if (hasMorePage()) {
            page++;
            final TypeReportItem item = getTypeReportItem();
            if (item == null) {
                Log.e(TAG, String.format("%s null on %s method call", "item", "pageNext"));
                return;
            }

            putToVCCache(item.getKey(), VC_CACHE_ENTRY_PAGE, page);
            applyAllTextEffect(getOnNextAnimation());
        }
    }

    private void pagePrev() {
        page--;
        if (page < 0)
            page = 0;
        else {
            final TypeReportItem item = getTypeReportItem();
            if (item == null) {
                Log.e(TAG, String.format("%s null on %s method call", "item", "pagePrev"));
                return;
            }

            putToVCCache(item.getKey(), VC_CACHE_ENTRY_PAGE, page);
            applyAllTextEffect(getOnPrevAnimation());
        }
    }

    protected void init() {
        viewMap.put(0, index0);
        viewMap.put(1, index1);
        viewMap.put(2, index2);
        viewMap.put(3, index3);
        viewMap.put(4, index4);
        viewMap.put(5, index5);
    }

    protected int getPointFromTextView(TextView textView) {
        final int indexOfValue = viewMap.indexOfValue(textView);
        final int index = viewMap.keyAt(indexOfValue);// viewMap.indexOfValue(textView) should probably give me the same result but i do think that this way is safer
        return pointer(index);
    }

    protected void manualRefresh(Animation... animation) {
        applyAllTextEffect(animation);
    }

    private void applyAllTextEffect(Animation... animation) {
        textViewPage.setText(String.valueOf(page + 1));
        textViewNPages.setText(String.valueOf(getMaxNumOfPages()));

        final int size = viewMap.size();
        for (int i = 0; i < size; i++) {
            final TextView textView = viewMap.get(i);
            if ((animation == null) || (animation.length == 0))
                applyTextEffect(textView, i, null);
            else if (animation.length <= i) {
                final Animation defaultAnim = animation[0];
                if (defaultAnim == null)
                    throw new IllegalStateException("provide at least 1 animation or just nothing");
                applyTextEffect(textView, i, defaultAnim);

            } else {
                applyTextEffect(textView, i, animation[i]);
            }
        }
    }

    private void applyTextEffect(TextView textView, int index, Animation animation) {
        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on applyTextEffect method");
            return;
        }

        final int pointer = pointer(index);
        textView.setAnimation(null);

        if (!hasMorePage() && isOverPointing(pointer)) {
            textView.setVisibility(View.GONE);
            return;

        } else if (textView.getVisibility() == View.GONE)
            textView.setVisibility(View.VISIBLE);

        if (isPointerEnable(pointer)) // to set a kind of state to the button
            textView.setTextColor(context.getResources().getColor(color.color_white));
        else
            textView.setTextColor(context.getResources().getColor(color.color_view_controller_light_grey));

        textView.setText(getLabelForPointer(pointer));

        if (animation != null)
            textView.startAnimation(animation);

    }

    private int pointer(int index) {
        return index + (page * BUTTON_PER_PAGE_MAX_COUNT) + 1;
    }


    private boolean hasMorePage() {  // find if the user can go next
        final int maxIndex = BUTTON_PER_PAGE_MAX_COUNT - 1;
        final int nextMin = pointer(maxIndex) + 1;  // find the future next min pointer
        return (nextMin <= getPointerCount()); // return true if the next minimum pointer is less than the number of slots so that we can go to the next page
    }

    /*
      This is to check any unused button and the remove them, because the last page can point to less than available button.
      Example the last page can point to only 2 buttons, what about the other 4? well should be invisible to the user
     */
    private boolean isOverPointing(int pointer) {
        return (pointer > getPointerCount());
    }

    private int getMaxNumOfPages() { // to be used sometime in the future :)
        if (getPointerCount() == 0)
            return 1;
        final int mod = getPointerCount() % BUTTON_PER_PAGE_MAX_COUNT;
        return (getPointerCount() / BUTTON_PER_PAGE_MAX_COUNT) + ((mod > 0) ? 1 : 0);
    }


}
