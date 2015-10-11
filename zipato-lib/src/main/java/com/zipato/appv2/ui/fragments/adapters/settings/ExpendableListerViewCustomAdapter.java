/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.settings;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.settings.AnimatedExpandableListView.AnimatedExpandableListAdapter;
import com.zipato.appv2.ui.fragments.settings.SettingMenuFragment.Helper;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.model.box.Box;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TypeFaceUtils;

import java.util.List;

/**
 * Created by murielK on 16.6.2014..
 */
public class ExpendableListerViewCustomAdapter extends AnimatedExpandableListAdapter {

    private final LayoutInflater inflater;
    private final Context context;
    private final List<Helper> groupList;
    private final SparseBooleanArray selectedItemsIds;
    private final LanguageManager languageManager;
    private final InternetConnectionHelper internetConnectionHelper;
    private final TypeFaceUtils typeFaceUtils;
    private GroupClickListner groupClickListner;
    private OnServerUrlChangedListner onServerUrlChangedListner;

    public ExpendableListerViewCustomAdapter(Context context, List<Helper> groupList, LanguageManager languageManager, InternetConnectionHelper connectionHelper, TypeFaceUtils typeFaceUtils) {

        this.context = context;
        this.groupList = groupList;
        inflater = LayoutInflater.from(context);
        selectedItemsIds = new SparseBooleanArray();
        this.languageManager = languageManager;
        internetConnectionHelper = connectionHelper;
        this.typeFaceUtils = typeFaceUtils;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public Helper getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groupList.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public void setGroupClickListner(GroupClickListner listener) {
        this.groupClickListner = listener;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = getGroup(groupPosition).selected.toString();
        GroupViewHolder groupViewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_expendable_list_view_group, null);
            groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }

        //groupViewHolder.getGroupName().setTypeface(null, Typeface.BOLD);
        if (getGroup(groupPosition).groupImage == R.drawable.ic_box_offline) {
            Box box = (Box) getGroup(groupPosition).box;
            if ((box != null) && (box.getSerial() != null)) {
                if ((box.getName() != null) && !box.getName().isEmpty()) {
                    headerTitle = box.getName();
                } else {
                    headerTitle = box.getSerial();
                }
                if (box.isFirmwareUpgradeRequired())
                    groupViewHolder.arrow.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info_red));
                else
                    groupViewHolder.arrow.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_info));
                groupViewHolder.layoutArrow.setEnabled(true);
                groupViewHolder.layoutArrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if (editText != null) {
//                            editText.setFocusable(false);
//                        }
                        if (groupClickListner != null) {
                            groupClickListner.onInfoClick(groupPosition);
                        }
                    }
                });

                if (box.isOnline() && internetConnectionHelper.isOnline()) {
                    groupViewHolder.groupIcon.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_box_online));
                } else {
                    groupViewHolder.groupIcon.setBackgroundDrawable(context.getResources().getDrawable(groupList.get(groupPosition).groupImage));
                }


            } else {
                final String textTemp = languageManager.translate("register_product_name");
                headerTitle = textTemp.replace("{productName}", context.getResources().getString(R.string.reg_box));
                groupViewHolder.groupIcon.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.add_new_device_icon));
                groupViewHolder.layoutArrow.setEnabled(false);
            }

        } else {
            groupViewHolder.groupIcon.setBackgroundDrawable(context.getResources().getDrawable(groupList.get(groupPosition).groupImage));
            groupViewHolder.arrow.setImageDrawable(context.getResources().getDrawable(R.drawable.drawable_transparent));
            groupViewHolder.layoutArrow.setEnabled(false);
            groupViewHolder.layoutArrow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.drawable_transparent));

        }
        groupViewHolder.groupName.setText(headerTitle);

        groupViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (editText != null) {
//                    editText.setFocusable(false);
//                }
                if ((groupClickListner != null)) {
                    groupClickListner.onGroupClick(groupPosition);
                }
            }
        });

        return convertView;
    }

    @Override
    public View getRealChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        try {
            convertView = inflater.inflate(groupList.get(groupPosition).layout, null);

            int i = groupList.get(groupPosition).layout;
            if (i == R.layout.row_expendable_list_view_item) {
                if (selectedItemsIds.get(childPosition)) {
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.soft_green));
                }
                TextView txtListChild = (TextView) convertView.findViewById(R.id.textViewExpendableChild);
                txtListChild.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
                txtListChild.setText(getChild(groupPosition, childPosition).toString());

            } else if (i == R.layout.row_expendable_list_view_item_edit) {
                final EditText editText = (EditText) convertView.findViewById(R.id.editTextChild);
                editText.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
                editText.setText(getChild(groupPosition, childPosition).toString());
                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            try {
                                String url = editText.getText().toString();
                                if (!url.equals(getChild(groupPosition, childPosition).toString()))
                                    ;
                                onServerUrlChangedListner.onServerUrlChanged(url);

//                                String url = editText.getText().toString();
//                                preferenceManager.putStringPref(PreferenceManager.Preference.SERVER_URL, url);
//                                groupList.get(groupPosition).children = Collections.singletonList(url);
                            } catch (Exception e) {
                                //
                            }
                        }
                    }
                });

            } else if (i == R.layout.row_multibox_children) {
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageViewBoxStatus);
                TextView textView = (TextView) convertView.findViewById(R.id.textViewBoxName);
                textView.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
                if (childPosition > 0) {
                    Box boxChild = (Box) getChild(groupPosition, childPosition);
                    if (boxChild != null) {
                        if ((boxChild.getName() != null) && !boxChild.getName().isEmpty()) {
                            textView.setText(boxChild.getName());
                        } else if (boxChild.getSerial() != null) {
                            textView.setText(boxChild.getSerial());
                        } else {
                            textView.setText("-");
                        }
                        if (boxChild.isOnline() && internetConnectionHelper.isOnline()) {
                            imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_box_online));
                        } else {
                            imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_box_offline));
                        }
                        Box box = (Box) groupList.get(groupPosition).box;
                        if ((box != null) && box.getSerial().equals(boxChild.getSerial()))
                            convertView.setBackgroundColor(context.getResources().getColor(R.color.soft_green));
                    } else {
                        textView.setText("-");
                        imageView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_box_offline));
                    }
                } else {
                    imageView.setVisibility(View.INVISIBLE);
                    final String textTemp = languageManager.translate("register_product_name");
                    final String textFinal = textTemp.replace("{productName}", context.getResources().getString(R.string.reg_box));
                    textView.setText(textFinal);
                }

            }
        } catch (Exception e) {
            return null;
        }

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        try {
            return groupList.get(groupPosition).children.size();
        } catch (NullPointerException e) {
            return 1;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void toggleSelection(int position) {
        selectView(position, true);
    }

    public void selectView(int position, boolean value) {

        selectedItemsIds.clear();
        selectedItemsIds.put(position, value);
        notifyDataSetChanged();
    }

    public void setOnServerUrlChangedListner(OnServerUrlChangedListner listner) {
        onServerUrlChangedListner = listner;
    }

    public interface OnServerUrlChangedListner {

        void onServerUrlChanged(String newUrl);
    }

    public interface GroupClickListner {
        void onGroupClick(int position);

        void onInfoClick(int position);
    }

    private class GroupViewHolder {
        TextView groupName;
        ImageView arrow;
        LinearLayout linearLayout;
        ImageView groupIcon;
        LinearLayout layoutArrow;


        public GroupViewHolder(View v) {

            groupName = (TextView) v.findViewById(R.id.textViewGroup);
            groupName.setTypeface(typeFaceUtils.getTypeFace("helvetica_neue_light.otf"));
            arrow = (ImageView) v.findViewById(R.id.imageViewGroupArrow);
            linearLayout = (LinearLayout) v.findViewById(R.id.LayoutRowGroup);
            groupIcon = (ImageView) v.findViewById(R.id.imageViewGroupImg);
            layoutArrow = (LinearLayout) v.findViewById(R.id.layoutRowArrow);
        }

        public LinearLayout getLinearLayout() {
            return linearLayout;
        }

    }
}