/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.discovery.BaseDiscoveryFragment;
import com.zipato.appv2.ui.fragments.discovery.SDeviceFragment;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectLauncher;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by murielK on 8/13/2014.
 */
public class DiscoveryActivity extends BaseActivity {

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewID)
    TextView textViewID;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.buttonStep)
    Button buttonStep;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.buttonCancel)
    Button buttonCancel;


    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_discovery;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterKnife.inject(this);
        if (savedInstanceState == null) {
            SDeviceFragment fragment = new SDeviceFragment();
            textViewID.setText(languageManager.translate(fragment.getTitle()));
            buttonStep.setText(generateStepText(fragment.getStepId()));
            getSupportFragmentManager().beginTransaction()
                    .replace(id.discoveryFrame, fragment, fragment.getFragmentTag())
                    .commit();
        } else {
            updateTitle();
        }

        buttonCancel.setText(languageManager.translate("cancel"));
        buttonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected boolean provideMenu() {
        return true;
    }


    private String generateStepText(int stepID) {
        return languageManager.translate("step") + " " + stepID;

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_LAUNCHER:
                handleLauncherEvent((ObjectLauncher) event.eventObject);
                break;
            case Event.EVENT_TYPE_DIS_REFRESH_TITLE:
                updateTitle();
                break;
            default:
                super.onEventMainThread(event);
                break;
        }
    }

    @Override
    public void onShake() {
        vib.vibrate(50);
        triggerScenes();
    }

    @Override
    protected void handleLauncherEvent(ObjectLauncher objectLauncher) {
        switch (objectLauncher.launchType) {
            case ObjectLauncher.LAUNCH_FRAGMENT:
                BaseDiscoveryFragment fragment = BaseDiscoveryFragment.newInstance((Class<? extends BaseDiscoveryFragment>) objectLauncher.clzz, (Parcelable) objectLauncher.object);
                if (!(getSupportFragmentManager().findFragmentById(id.discoveryFrame).getTag().equals(fragment.getTag()))) {
                    textViewID.setText(languageManager.translate(fragment.getTitle()));
                    buttonStep.setText(generateStepText(fragment.getStepId()));
                    getSupportFragmentManager().beginTransaction().replace(id.discoveryFrame, fragment, fragment.getFragmentTag())
                            .addToBackStack(null)
                            .commit();
                }
                break;
            case ObjectLauncher.LAUNCH_ACTIVITY:
                final Class<? extends BaseActivity> activityClass = (Class<? extends BaseActivity>) objectLauncher.clzz;
                if (activityClass.equals(DiscoveryActivity.class)) {
                    if (getSlidingMenu().isMenuShowing())
                        getSlidingMenu().toggle();
                    return;
                }
                Intent i = new Intent(this, activityClass);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.setAction(BrowserManagerActivity.RESUMING_ACTION);
                startActivity(i);
                break;
            default:
                super.handleLauncherEvent(objectLauncher);
        }

    }

    private <T extends BaseDiscoveryFragment> void updateTitle() {
        T fragment = (T) getSupportFragmentManager().findFragmentById(id.discoveryFrame);
        if (fragment != null) {
            textViewID.setText(languageManager.translate(fragment.getTitle()));
            buttonStep.setText(generateStepText(fragment.getStepId()));
            if (!"".equals(fragment.getButtTitle())) {
                buttonCancel.setText(languageManager.translate(fragment.getButtTitle()));
            } else {
                buttonCancel.setText(languageManager.translate("cancel"));
            }

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateTitle();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browser_manager, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.reset) {
//            eventBus.post(BrowserManagerActivity.REFRESH_REQUEST);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
