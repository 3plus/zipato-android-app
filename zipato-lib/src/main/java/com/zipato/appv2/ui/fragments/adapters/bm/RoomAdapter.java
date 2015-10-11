/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.bm;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.B;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.adapters.BaseRecyclerViewAdapter;
import com.zipato.model.room.Rooms;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TypeFaceUtils;

import java.util.List;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import butterfork.OnClick;
import butterfork.OnLongClick;

/**
 * Created by murielK on 9/18/2015.
 */
public class RoomAdapter extends BaseRecyclerViewAdapter {

    private static final SparseIntArray mapImg = new SparseIntArray(3);

    static {
        mapImg.put(0, R.drawable.img_room_kit);
        mapImg.put(1, R.drawable.img_room_living);
        mapImg.put(2, R.drawable.img_room_office);
    }

    private final Context context;
    @Inject
    protected TypeFaceUtils typeFaceUtils;
    @Inject
    List<Rooms> rooms;
    @Inject
    LanguageManager languageManager;
    @Inject
    Picasso picasso;

    private RecyclerTouchEventListener listener;

    public RoomAdapter(Context context, RecyclerTouchEventListener listener) {
        ((ZipatoApplication) context.getApplicationContext()).inject(this);
        this.context = context;
        this.listener = listener;
    }

    public RoomAdapter(Context context) {
        ((ZipatoApplication) context.getApplicationContext()).inject(this);
        this.context = context;
    }


    public void setListener(RecyclerTouchEventListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return (rooms == null) ? 0 : rooms.size();
    }

    public Rooms getRoom(int position) {
        return (rooms == null) ? null : ((!rooms.isEmpty()) ? rooms.get(position) : null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((RoomViewHolder) holder).onBind();
    }

    class RoomViewHolder extends ViewHolder {

        @SetTypeFace("helveticaneue_ultra_light.otf")
        @Bind(B.id.textViewFilter)
        TextView textView;
        @Bind(B.id.imageFilter)
        ImageView imageFilter;
        @Bind(B.id.filterView)
        View view;
        @Bind(B.id.filterViewClick)
        View viewClick;

        public RoomViewHolder(View itemView) {
            super(itemView);
            ButterFork.bind(this, itemView);
            typeFaceUtils.applyTypefaceFor(this);
        }

        @OnClick(B.id.filterViewClick)
        public void onClick(View v) {
            if (listener != null)
                listener.onClick(getAdapterPosition());
        }

        @OnLongClick(B.id.filterViewClick)
        public boolean onLongClick(View v) {
            if (listener != null)
                listener.onLongClick(getAdapterPosition());
            return true;
        }

        public void onBind() {
            final Rooms room = getRoom(getAdapterPosition());
            if (room == null)
                return;


            if (getSelectedIds().get(getAdapterPosition())) {
                viewClick.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setBackgroundColor(context.getResources().getColor(R.color.color_view_controller_item_background_trans));
                    }
                });
            } else {
                viewClick.post(new Runnable() {
                    @Override
                    public void run() {
                        view.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                    }
                });
            }

            final int res;
            if ("Undefined".equals(room.getName())) {
                textView.setText(languageManager.translate("undefined"));
                res = R.drawable.img_room_home;
            } else {
                final String name = room.getName();
                if (name != null)
                    textView.setText(name);
                else textView.setText("");
                res = mapImg.get(getAdapterPosition() % 3);
            }

            try {
                picasso.load(Uri.parse(room.getStringUri())).fit().error(res).into(imageFilter);
            } catch (Exception e) {
                picasso.load(res).fit().error(res).into(imageFilter);
            }
        }
    }

}
