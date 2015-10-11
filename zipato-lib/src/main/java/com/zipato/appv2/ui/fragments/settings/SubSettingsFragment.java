/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.settings;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.bool;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.R.string;
import com.zipato.appv2.activities.ShakeSettingActivity;
import com.zipato.appv2.ui.fragments.adapters.settings.ExpendableListerViewCustomAdapter;
import com.zipato.appv2.ui.fragments.adapters.settings.ExpendableListerViewCustomAdapter.GroupClickListner;
import com.zipato.appv2.ui.fragments.adapters.settings.ExpendableListerViewCustomAdapter.OnServerUrlChangedListner;
import com.zipato.appv2.ui.fragments.settings.SettingMenuFragment.Helper;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectLauncher;
import com.zipato.model.language.Language;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterfork.Bind;

/**
 * Created by murielK on 2/25/2015.
 */
public class SubSettingsFragment extends AbsBaseSubMenu implements OnServerUrlChangedListner {

    private static final String TAG = SubSettingsFragment.class.getSimpleName();
    private static final String SUBJECT = "[Android feedback]";
    private final List<Helper> groupList = new ArrayList<>();
    @Bind(B.id.expandableListView)
    AnimatedExpandableListView expandableListView;
    @Inject
    PreferenceHelper preferenceHelper;
    @Inject
    ApiV2RestTemplate restTemplate;
    private ExpendableListerViewCustomAdapter listerViewCustomAdapter;
    private HashMap<String, Event> map;
    private String shakeText;
    private String aboutText;
    private String feedback;


    @Override
    protected int getResourceView() {
        return R.layout.fragment_sub_setting_menu;
    }

    @Override
    protected void onPostViewCreate() {
        shakeText = languageManager.translate("shake");
        aboutText = languageManager.translate("about");
        feedback = languageManager.translate("feedback");

        initView();

    }

    private void sendFeedback() {
        String osName = "";
        Field[] fields = VERSION_CODES.class.getFields();
        final Object object = new Object();// the fields are static this wont matter
        for (Field field : fields) {
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(object);
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }

            if (fieldValue == VERSION.SDK_INT) {
                osName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1).toLowerCase();
                break;
            }
        }
        final String deviceInfo =
                "     -----------------     " + '\n' +
                        " Os: Android: " + VERSION.RELEASE + " " + osName + '\n' +
                        " SDK: " + VERSION.SDK_INT + '\n' +
                        " Device: " + Build.DEVICE + '\n' +
                        " Model: " + Build.MODEL + '\n' +
                        " Brand: " + Build.BRAND + '\n' +
                        "     -----------------     " + '\n' + '\n' +
                        feedback + ": " + '\n' + '\n';

        final String[] to = {getResources().getString(string.feedbackMail)};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("message/rfc822");

        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, SUBJECT);
        emailIntent.putExtra(Intent.EXTRA_TEXT, deviceInfo);

        try {
            startActivity(Intent.createChooser(emailIntent, languageManager.translate("choose_mail_client")));
        } catch (ActivityNotFoundException ex) {
            toast(languageManager.translate("mail_client_not_found"));
        }
    }

    private void initView() {
        listerViewCustomAdapter = new ExpendableListerViewCustomAdapter(getActivity(), groupList, languageManager, internetConnectionHelper, typeFaceUtils);
        listerViewCustomAdapter.setOnServerUrlChangedListner(this);
        listerViewCustomAdapter.setGroupClickListner(new GroupClickListner() {
            @Override
            public void onGroupClick(int position) {

                if ((groupList.get(position).children == null) || groupList.get(position).children.isEmpty()) {
                    int i = groupList.get(position).groupImage;
                    if (i == drawable.ic_about) {
                        showAbout();

                    } else if (i == drawable.ic_feedback) {
                        sendFeedback();

                    } else {
                        try {
                            eventBus.post(map.get(groupList.get(position).getSelected()));//
                        } catch (Exception e) {
                            Log.d(TAG, "", e);
                        }

                    }
                    return;
                }

                if (expandableListView.isGroupExpanded(position)) {
                    expandableListView.collapseGroupWithAnimation(position);
                } else {
                    expandableListView.expandGroupWithAnimation(position);
                }
            }

            @Override
            public void onInfoClick(int position) {

            }
        });

        expandableListView.setAdapter(listerViewCustomAdapter);
        expandableListView.setOnChildClickListener(new OnChildClickListener() {
            @TargetApi(VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //the default clicked language will be set here of course
                int i = groupList.get(groupPosition).groupImage;
                if (i == drawable.language_group_icon) {
                    listerViewCustomAdapter.toggleSelection(childPosition);
                    groupList.get(groupPosition).selected = groupList.get(groupPosition).children.get(childPosition);
                    expandableListView.setSelectedChild(groupPosition, childPosition, true);
                    preferenceHelper.putStringPref(Preference.LANGUAGE,
                                                   ((Language) groupList.get(groupPosition).children.get(childPosition))
                                                           .getCode());
                    listerViewCustomAdapter.notifyDataSetChanged();
                    getActivity().recreate();

                }

                return false;
            }
        });

        expandableListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                return true;
            }
        });
    }

    private void showAbout() {
        final Context context = getContext();
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        if (packageInfo == null)
            return;

        final View v = LayoutInflater.from(getContext()).inflate(layout.layout_about, null);
        final TextView version = (TextView) v.findViewById(id.textViewAPPVersion);
        final TextView versionName = (TextView) v.findViewById(id.textViewAPPVValue);
        final TextView title = (TextView) v.findViewById(id.textViewTitle);
        title.setTypeface(typeFaceUtils.getTypeFace("helvetica_neue_light.otf"));
        version.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
        versionName.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
        version.setText(languageManager.translate("version"));
        versionName.setText(packageInfo.versionName);

        final Builder builder = new Builder(context);
        builder.setView(v);
        builder.setPositiveButton(languageManager.translate("ok"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void setListView() {
        groupList.clear();
        listerViewCustomAdapter.notifyDataSetChanged();
        if (getContext().getResources().getBoolean(bool.portrait_only)) {
            if (map == null) {
                map = new HashMap<>();
            }
            groupList.add(new Helper(shakeText, 0, null, drawable.ic_shake));
            if (map.get(shakeText) == null) {
                map.put(shakeText, new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_ACTIVITY, ShakeSettingActivity.class, "Shake"), Event.EVENT_TYPE_LAUNCHER));
            }
        }
        Helper lang = new Helper();
        lang.groupImage = drawable.language_group_icon;
        lang.children = new ArrayList<>(languageManager.getLanguages().values());
        lang.layout = layout.row_expendable_list_view_item;
        lang.selected = languageManager.getLanguage();// get default language);

        Helper server = new Helper();
        server.groupImage = drawable.server_url_group_icon;
        server.selected = languageManager.translate("server_url_text_menu");
        server.children = Collections.singletonList(preferenceHelper.getStringPref(Preference.SERVER_URL, preferenceHelper.getBaseUrl()));
        server.layout = layout.row_expendable_list_view_item_edit;
        if (getContext().getResources().getBoolean(bool.menu_show_server_url))
            groupList.add(server);
        if (getContext().getResources().getBoolean(bool.menu_show_language))
            groupList.add(lang);
        if (getContext().getResources().getBoolean(bool.feedback))
            groupList.add(new Helper(feedback, 0, null, drawable.ic_feedback));

        groupList.add(new Helper(aboutText, 0, null, drawable.ic_about));

        listerViewCustomAdapter.toggleSelection(lang.children.indexOf(lang.selected));

    }

    @Override
    protected String provideTitle() {
        return languageManager.translate("SettingsScreenTitle");
    }

    @Override
    public void onResume() {
        super.onResume();
        setListView();
    }

    @Override
    public void onServerUrlChanged(String newUrl) {
        restTemplate.setRemoteUrl(newUrl);
        preferenceHelper.putStringPref(Preference.SERVER_URL, newUrl);
        for (Helper list : groupList) {
            if (list.groupImage == drawable.server_url_group_icon) {
                list.children = Collections.singletonList(newUrl);
                break;
            }
        }

    }
}
