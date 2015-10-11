/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.bm;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.adapters.BaseRecyclerViewAdapter;
import com.zipato.model.scene.Scene;
import com.zipato.util.TypeFaceUtils;
import com.zipato.util.Utils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by murielK on 10/6/2015.
 */
public class ScenesAdapter extends BaseRecyclerViewAdapter {

    private final Context context;
    @Inject
    protected TypeFaceUtils typeFaceUtils;
    @Inject
    List<Scene> scenes;

    private RecyclerTouchEventListener listener;

    public ScenesAdapter(Context context, RecyclerTouchEventListener listener) {
        ((ZipatoApplication) context.getApplicationContext()).inject(this);
        this.context = context;
        this.listener = listener;
    }

    public void setListener(RecyclerTouchEventListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_scenes, parent, false);
        return new ScenesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((ScenesViewHolder) holder).onBind();
    }

    @Override
    public int getItemCount() {
        return (scenes == null) ? 0 : scenes.size();
    }

    public Scene getScene(int position) {
        return (scenes == null) ? null : ((!scenes.isEmpty()) ? scenes.get(position) : null);
    }

    class ScenesViewHolder extends RecyclerView.ViewHolder {


        @SetTypeFace("helveticaneue_ultra_light.otf")
        @InjectView(R.id.textViewSceneName)
        TextView textView;
        @SetTypeFace("icomoon.ttf")
        @InjectView(R.id.textViewSceneKK)
        TextView textViewKK;
        @InjectView(R.id.filterViewClick)
        View viewClick;

        public ScenesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            typeFaceUtils.applyTypefaceFor(this);
        }

        public void onBind() {
            final Scene scene = scenes.get(getAdapterPosition());
            if (scene == null)
                return;

            textViewKK.setText(Utils.getHexForKitKat(scene.getIcon()));
            textViewKK.setTextColor(Color.parseColor(scene.getIconColor()));

            textView.setText(scene.getName());
            if (isSelected(getAdapterPosition()))
                ViewCompat.animate(textViewKK).scaleX(1.7f).scaleY(1.7f).setDuration(400).start();
            else if (textViewKK.getScaleX() > 1f)
                ViewCompat.animate(textViewKK).scaleX(1f).scaleY(1).setDuration(225).start();
        }

        @OnClick(R.id.filterViewClick)
        public void onClick(View v) {
            if (listener != null)
                listener.onClick(getAdapterPosition());
        }

        @OnLongClick(R.id.filterViewClick)
        public boolean onLongClick(View v) {
            if (listener != null)
                listener.onLongClick(getAdapterPosition());
            return true;
        }


    }
}
