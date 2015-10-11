/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.vcmenu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.appv2.B;
import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectIcon;
import com.zipato.model.scene.Scene;
import com.zipato.model.scene.SceneRepository;
import com.zipato.util.TypeFaceUtils;
import com.zipato.util.Utils;

import java.util.UUID;

import javax.inject.Inject;

import butterfork.Bind;
import butterfork.ButterFork;
import butterfork.OnClick;
import butterfork.OnItemClick;

/**
 * Created by murielK on 7/4/2014.
 */
public class ScenesIconColorFragment extends BaseTypesFragment {

    public static final String COLOR_KEY = "COLOR_KEY";
    public static final String KK_KEY = "KK_KEY";

    protected IconGridListAdapter iconGridAdapter;
    protected ColorGridListAdapter colorGridAdapter;
    @Bind(B.id.gridViewSceneColor)
    protected GridView gridViewColor;
    @Bind(B.id.gridViewSceneIcon)
    protected GridView gridViewIcon;
    @Bind(B.id.layout)
    protected LinearLayout layout;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.button1)
    protected Button button1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.button2)
    protected Button button2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.progressBar3)
    protected ProgressBar progressBar;
    @Bind(B.id.layoutListViews)
    protected LinearLayout layoutListViews;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("choose_icon")
    @Bind(B.id.textView)
    TextView chooseICon;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("choose_background_color")
    @Bind(B.id.textView2)
    TextView chooseBackGroundColor;

    @Inject
    TypeFaceUtils typeFaceUtils;
    @Inject
    SceneRepository sceneRepository;

    String currentColor;
    String currentKK;

    protected static int getSelectedIndex(String value, String[] values) {

        int index = -1;

        if ((value == null) || (values == null)) {
            return index;
        }

        int size = values.length;

        for (int i = 0; i < size; i++) {
            if (values[i].equals(value)) {
                index = i;
                break;
            }
        }

        return index;
    }

    @OnClick(B.id.button1)
    public void onDefaultClick(View v) {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    @OnClick(B.id.button2)
    public void onSaveClick(View v) {
        Intent data = new Intent();
        data.putExtra(COLOR_KEY, currentColor);
        data.putExtra(KK_KEY, currentKK);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_color_icon;
    }

    @Override
    protected void onPostViewCreate() {
        typeFaceUtils.applyTypefaceFor(this);
        languageManager.translateFields(this);
    }

    @OnItemClick(B.id.gridViewSceneColor)
    public void onColorClick(int position) {
        colorGridAdapter.removeSelection();
        colorGridAdapter.toggleSelection(position);
        eventBus.post(new Event(new ObjectIcon(ObjectIcon.TYPE_COLOR, colorGridAdapter.getItem(position)), Event.EVENT_TYPE_KITKAT_ICON));
        currentColor = colorGridAdapter.getItem(position);
        iconGridAdapter.notifySelection();
    }

    @OnItemClick(B.id.gridViewSceneIcon)
    public void onIconClick(int position) {
        iconGridAdapter.removeSelection();
        iconGridAdapter.toggleSelection(position);
        eventBus.post(new Event(new ObjectIcon(ObjectIcon.TYPE_ICON, iconGridAdapter.getItem(position)), Event.EVENT_TYPE_KITKAT_ICON));
        currentKK = iconGridAdapter.getItem(position);
    }

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(savedInstanceState);
    }

    protected void init(Bundle savedInstanceState) {

        iconGridAdapter = new IconGridListAdapter();
        colorGridAdapter = new ColorGridListAdapter();

        gridViewColor.setAdapter(colorGridAdapter);
        gridViewIcon.setAdapter(iconGridAdapter);

        final Bundle bundle = getArguments();

        if ((bundle != null) && bundle.containsKey(SERIALIZABLE_KEY)) {
            final UUID sceneUUID = (UUID) bundle.getSerializable(SERIALIZABLE_KEY);
            final Scene scene = sceneRepository.get(sceneUUID);

            if (scene != null) {
                int iconIndex = getSelectedIndex(scene.getIcon(), iconGridAdapter.getIcons());
                if (iconIndex != -1) {
                    currentKK = iconGridAdapter.getItem(iconIndex);
                    iconGridAdapter.toggleSelection(iconIndex);
                    gridViewIcon.smoothScrollToPosition(iconIndex);
                }

                int colorIndex = getSelectedIndex(scene.getIconColor(), colorGridAdapter.getColors());
                if (colorIndex != -1) {
                    currentColor = colorGridAdapter.getItem(colorIndex);
                    colorGridAdapter.toggleSelection(colorIndex);
                    gridViewColor.smoothScrollToPosition(iconIndex);
                }
            }
        }

        button1.setText(languageManager.translate("cancel"));
        button2.setEnabled(true);
        button1.setEnabled(true);
        button2.setText("OK");
    }

    public class ColorGridListAdapter extends BaseListAdapter {

        private String[] colors = {"#000000", "#993300", "#333300", "#003300", "#003366", "#000080", "#333399", "#333333", "#800000", "#FF6600", "#808000",
                                   "#008000",
                                   "#008080", "#0000FF", "#666699", "#808080", "#FF0000", "#FF9900", "#99CC00", "#339966", "#33CCCC", "#3366FF", "#800080",
                                   "#969696",
                                   "#FF00FF", "#FFCC00", "#FFFF00", "#00FF00", "#00FFFF", "#00CCFF", "#993366", "#C0C0C0", "#FF99CC", "#FFCC99", "#FFFF99",
                                   "#CCFFCC",
                                   "#CCFFFF", "#99CCFF", "#CC99FF", "#FFFFFF"};

        public String[] getColors() {
            return colors;
        }

        public void setColors(String[] colors) {
            if (colors == null) {
                return;
            }

            int size = colors.length;
            this.colors = new String[size];
            System.arraycopy(colors, 0, this.colors, 0, size);

        }

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public String getItem(int position) {
            return colors[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewColorHolder viewColorHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_scene_color, parent, false);
                viewColorHolder = new ViewColorHolder(convertView);
                convertView.setTag(viewColorHolder);
            } else {
                viewColorHolder = (ViewColorHolder) convertView.getTag();
            }

            viewColorHolder.textView.setBackgroundColor(Color.parseColor(colors[position]));
            if (isSelected(position)) {
                viewColorHolder.textView.setText(Utils.getHexForKitKat("2713"));
            } else {
                viewColorHolder.textView.setText("");
            }

            return convertView;
        }

        class ViewColorHolder {

            @Bind(B.id.textViewSceneColor)
            TextView textView;

            public ViewColorHolder(View v) {

                ButterFork.bind(this, v);
            }

        }
    }

    public class IconGridListAdapter extends BaseListAdapter {

        private String[] icons = {
                "e602", "e610", "e618", "e61c", "e61d", "e61e", "e620",
                "e629", "e62a", "e636", "e63d", "e675", "e696", "e69d",
                "e6a5", "e726", "e72b", "e73b", "e73c", "e751", "e777",
                "e779", "e77d", "e78c", "e81e", "e845", "e90b", "e90d",
                "e918", "e9f2", "e9fc", "ea03", "ea0c", "ea1c", "ea2d",
                "ea2e", "ea30", "ea32", "ea38", "ea40", "ea42", "ea44",
                "ea48", "ea49", "ea4c", "ea50", "ea64", "ea6b", "ea6d",
                "ea6e", "ea90", "eaa0", "eaa2", "ead6", "eada", "eadd",
                "eaf5", "eafa", "eafb", "eb06", "eb08", "eb0c", "eb1b",
                "eb24", "eb73", "eb7d", "ebbf", "ebe8", "ec03", "ec53",
                "ec56", "ec6c", "ece8", "ed00", "ee50", "ed33", "ed49",
                "ed77", "ed78", "ed7c", "ed85", "ed8c", "ede9", "edb2",
                "edc7", "edc8", "edc9", "edca", "edcd", "edce", "edcf"
        };

        public String[] getIcons() {
            return icons;
        }

        public void setIcons(String[] strings) {
            if (strings == null) {
                return;
            }
            icons = new String[strings.length];
            System.arraycopy(strings, 0, icons, 0, strings.length);
        }

        @Override
        public int getCount() {
            return icons.length;
        }

        @Override
        public String getItem(int position) {
            return icons[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewIconHolder viewIconHolder;
            if (convertView == null) {

                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_scene_icon, parent, false);
                viewIconHolder = new ViewIconHolder(convertView);
                convertView.setTag(viewIconHolder);
            } else {
                viewIconHolder = (ViewIconHolder) convertView.getTag();
            }

            viewIconHolder.textView.setText(Utils.getHexForKitKat(icons[position]));
            if (isSelected(position)) {
                final SparseBooleanArray colorSpare = colorGridAdapter.getSelectedIds();
                if (currentColor != null) {

                    viewIconHolder.textView.setTextColor(Color.parseColor(currentColor));

                } else {
                    viewIconHolder.textView.setTextColor(Color.WHITE);
                }

                viewIconHolder.textView.setSelected(true);
            } else {
                viewIconHolder.textView.setTextColor(getContext().getResources().getColor(R.color.soft_grey));
                viewIconHolder.textView.setTextColor(getContext().getResources().getColor(R.color.grey_font));
                viewIconHolder.textView.setSelected(false);
            }

            return convertView;
        }

        class ViewIconHolder {

            @SetTypeFace("icomoon.ttf")
            @Bind(B.id.textViewSceneIconRow)
            TextView textView;

            public ViewIconHolder(View v) {

                ButterFork.bind(this, v);
                typeFaceUtils.applyTypefaceFor(this);
            }
        }
    }
}