/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.anim;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.dm.ConfigObject;
import com.zipato.appv2.ui.fragments.dm.DMCMConfigFragment;
import com.zipato.appv2.ui.fragments.dm.DMConfigFragment;
import com.zipato.appv2.ui.fragments.dm.DmFragment;
import com.zipato.appv2.ui.fragments.dm.DmFragment.ListItemClickListener;
import com.zipato.appv2.ui.fragments.dm.NetworkFragment;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectLauncher;

import java.util.Stack;
import java.util.UUID;


public class DeviceManagerActivity extends BaseActivity implements ListItemClickListener {
    private final Stack<String> fragmentStack = new Stack<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {

    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_device_manager;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        if (savedInstanceState == null) {

            DmFragment<?> networkFragment = new NetworkFragment();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(anim.anim_fade_in, anim.anim_fade_out)
                    .replace(id.device_frame, networkFragment, networkFragment.getFragmentTag())
                    .commit();
            fragmentStack.add(networkFragment.getFragmentTag());
        }
    }

    @Override
    protected boolean provideMenu() {
        return true;
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

    @Override
    public void onParentClick(Class<? extends DmFragment<?>> fragmentClass, UUID parentUuid) {
        // FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        try {
            final DmFragment<?> dmFragment = fragmentClass.newInstance();
            Bundle bundle = new Bundle(2);
            bundle.putParcelable(DmFragment.KEY_UUID, new ParcelUuid(parentUuid));
            bundle.putBoolean(DmFragment.KEY_BOOL, true);
            dmFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().setCustomAnimations(anim.enter, anim.exit, anim.pop_enter, anim.pop_exit).replace(id.device_frame, dmFragment, dmFragment.getFragmentTag())
                    .addToBackStack(dmFragment.getFragmentTag())
                    .commit();
            fragmentStack.add(dmFragment.getFragmentTag());
        } catch (Exception e) {
            logD("", e);
        }
    }

    @Override
    public void onChildClick(int position) {
        removeFragment(position);
    }

    // Before 2.0
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
        if (!fragmentStack.isEmpty()) {
            fragmentStack.pop();
        }
        super.onBackPressed();
    }

    private void removeFragment(int position) {
        try {
            if ((position == (fragmentStack.size() - 1))) {
                return;
            }
            int initSize = fragmentStack.size();
            for (int test = initSize - 1; test > position; test--) {

                getSupportFragmentManager().popBackStack();
                fragmentStack.pop();
            }
            getSupportFragmentManager().beginTransaction().replace(id.device_frame,
                    getSupportFragmentManager().findFragmentByTag(fragmentStack.peek())).commit();
        } catch (Exception e) {
            logD("", e);
        }
    }

    public void onEventMainThread(Event event) {
        switch (event.eventType) {
            case Event.EVENT_TYPE_LAUNCHER:
                handleLauncherEvent((ObjectLauncher) event.eventObject);
                break;
            default:
                super.onEventMainThread(event);
                break;
        }

    }

    @Override
    public void onShake() {
        handleShakeEvent();
    }

    @Override
    protected void handleLauncherEvent(ObjectLauncher objectLauncher) {
        switch (objectLauncher.launchType) {
            case ObjectLauncher.LAUNCH_FRAGMENT:
                try {
                    final ConfigObject configObject = (ConfigObject) objectLauncher.object;
                    final DMCMConfigFragment dmcmConfigFragment = new DMCMConfigFragment();
                    Bundle bundle = new Bundle(2);
                    bundle.putParcelable(DMConfigFragment.UUID_KEY, new ParcelUuid(configObject.uuid));
                    bundle.putSerializable(DMConfigFragment.ENTITY_KEY, configObject.entityType);
                    dmcmConfigFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(id.device_frame, dmcmConfigFragment, "CONFIG_FRAGMENT_TAG")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();
                    fragmentStack.add("Config_fragment");

                } catch (Exception e) {
                    Log.d("DeviceManager", "", e);
                    toast(languageManager.translate("error_could'nt_load_configuration"));
                }
                break;
            case ObjectLauncher.LAUNCH_ACTIVITY:
                final Class<? extends BaseActivity> activityClass = (Class<? extends BaseActivity>) objectLauncher.clzz;
                if (activityClass.equals(DeviceManagerActivity.class)) {
                    if (getSlidingMenu().isMenuShowing())
                        getSlidingMenu().toggle();
                    return;
                }

                Intent intent = new Intent(this, activityClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .setAction(BrowserManagerActivity.RESUMING_ACTION);
                startActivity(intent);
                break;
            default:
                super.handleLauncherEvent(objectLauncher);
                break;
        }
    }

    public void handleShakeEvent() {
        vib.vibrate(50);
        triggerScenes();
    }


}