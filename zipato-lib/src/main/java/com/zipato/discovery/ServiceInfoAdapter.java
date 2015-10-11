/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.discovery;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.util.TypeFaceUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by murielK on 30.5.2014..
 */
public class ServiceInfoAdapter extends ArrayAdapter<ZipatoServiceInfo> {

    Context context;
    LayoutInflater inflater;
    List<ZipatoServiceInfo> zipatoServiceInfos;
    @Inject
    TypeFaceUtils typeFaceUtils;
    private SparseBooleanArray mSelectedItemsIds;

    public ServiceInfoAdapter(Context context, int resID, List<ZipatoServiceInfo> zipatoServiceInfos) {

        super(context, resID, zipatoServiceInfos);
        ((ZipatoApplication) context.getApplicationContext()).inject(this);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.zipatoServiceInfos = zipatoServiceInfos;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_login_pane, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.boxName.setText("UNKNOWN".equalsIgnoreCase(zipatoServiceInfos.get(position).getName()) ? context.getString(R.string.reg_box) : zipatoServiceInfos.get(position).getName());
        viewHolder.ipValue.setText(zipatoServiceInfos.get(position).getIp());
        viewHolder.snValue.setText(zipatoServiceInfos.get(position).getSerial());
        viewHolder.macValue.setText(zipatoServiceInfos.get(position).getMac());

        return convertView;
    }

    @Override
    public void remove(ZipatoServiceInfo object) {
        zipatoServiceInfos.remove(object);
        notifyDataSetChanged();
    }

    public List<ZipatoServiceInfo> getFileInfoList() {
        return zipatoServiceInfos;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {
        if (value) {
            mSelectedItemsIds.put(position, value);
        } else {
            mSelectedItemsIds.delete(position);
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }

    class ViewHolder {
        @SetTypeFace("helvetica_neue_light.otf")
        @InjectView(R.id.textViewLogZipaBoxName)
        TextView boxName;
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @InjectView(R.id.textViewSNValue)
        TextView snValue;
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @InjectView(R.id.textViewIPValue)
        TextView ipValue;
        @SetTypeFace("helveticaneue_ultra_light.otf")
        @InjectView(R.id.textViewMacValue)
        TextView macValue;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
            typeFaceUtils.applyTypefaceFor(this);
        }
    }
}

