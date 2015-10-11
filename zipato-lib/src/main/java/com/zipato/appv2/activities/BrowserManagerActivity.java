/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.widgets.SlidingTabLayout;
import com.zipato.appv2.R;
import com.zipato.appv2.interactor.BrowserManagerInteractor;
import com.zipato.appv2.services.AutoUpdaterService;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.bm.MainRoomFragment;
import com.zipato.appv2.ui.fragments.bm.MainSceneFragment;
import com.zipato.customview.CustomViewPager;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectLauncher;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BrowserManagerActivity extends BaseActivity {

    public static final String ACTION_SCENES = "ACTION_SCENES";
    public static final String RESUMING_ACTION = "RESUMING_ACTION";
    private static final String TAG = TagFactoryUtils.getTag(BrowserManagerActivity.class);

    @InjectView(R.id.bmViewPager)
    CustomViewPager customViewPager;
    @InjectView(R.id.slidingTabLayout)
    SlidingTabLayout slidingTabLayout;

    private BrowserManagerInteractor interactor;


    private static void clearBackStack(FragmentManager fm) {
        final int entryCount = fm.getBackStackEntryCount();
        for (int i = 0; i < (entryCount); i++) {
            try {
                fm.popBackStack();
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {
        interactor = new BrowserManagerInteractor(this);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_browser_manager;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        final int i = getResources().getConfiguration().orientation;

        if (savedInstanceState != null)
            interactor.restoreState(savedInstanceState);

        ButterKnife.inject(this);

        customViewPager.setAdapter(new PageAdapter(getSupportFragmentManager(), languageManager));

        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(customViewPager);

        interactor.loadRepoRefresh(false); // try to load first locally

    }

    @Override
    protected boolean provideMenu() {
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if ((intent != null) && ACTION_SCENES.equals(intent.getAction()))
            interactor.handleOnSceneEvent();
        Log.d(TAG, "onNewIntent called");
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        interactor.saveState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds roomTypes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.browser_manager, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch (id) {
            case R.id.menu_menu:
                if (slidingMenu != null) {
                    if (slidingMenu.isSecondaryMenuShowing())
                        slidingMenu.toggle();
                    else
                        slidingMenu.showSecondaryMenu();
                }
                return true;
            case R.id.menu_scene:
                eventBus.post(new Event(new ObjectLauncher(ObjectLauncher.LAUNCH_SCENES, null, null), Event.EVENT_TYPE_LAUNCHER));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void handleLauncherEvent(ObjectLauncher objectLauncher) {
        switch (objectLauncher.launchType) {
            case ObjectLauncher.LAUNCH_ACTIVITY:
                Class<? extends BaseActivity> activityClass = (Class<? extends BaseActivity>) objectLauncher.clzz;
                if (activityClass.equals(BrowserManagerActivity.class)) {
                    if (getSlidingMenu().isMenuShowing())
                        getSlidingMenu().toggle();
                    return;
                }
                startActivity(new Intent(this, activityClass));
                break;
            case ObjectLauncher.LAUNCH_FRAGMENT:

                break;
            case ObjectLauncher.LAUNCH_LOG_OUT:
                interactor.clearListItems();
                logout(LogInActivity.LOGOUT_ACTION);
                break;
            case ObjectLauncher.LAUNCH_SCENES:
                interactor.handleOnSceneEvent();
                break;
        }
    }

    public void onEventMainThread(Event event) { // TODO pass this work to the interactor
        switch (event.eventType) {
            case Event.EVENT_TYPE_ITEM_CLICK:
                ObjectItemsClick objectItemsClick = (ObjectItemsClick) event.eventObject;
                interactor.handleOnItemsClickEvent(objectItemsClick);
                break;
            case Event.EVENT_TYPE_LAUNCHER:
                handleLauncherEvent((ObjectLauncher) event.eventObject);
                break;
            case Event.EVENT_TYPE_REFRESH_REQUEST:
                interactor.onRefreshEvent();
                break;
            case Event.EVENT_TYPE_ON_BOX_CHANGE:
                interactor.changeBox();
                break;
            case Event.EVENT_TYPE_ENBALE_SWIPE_VIEW_PAGER:
                final boolean value = (Boolean) event.eventObject;
                customViewPager.setEnableSwipe(value);
                break;
            default:
                super.onEventMainThread(event);
                break;
        }

    }


    @Override
    public void onShake() {
        interactor.onShake();
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
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onResume() {
        super.onResume();
        startService(new Intent(this, AutoUpdaterService.class));
    }

    @Override
    public void onPause() {
        super.onPause();
        stopService(new Intent(this, AutoUpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
    }


    private static class PageAdapter extends FragmentStatePagerAdapter {

        private final LanguageManager languageManager;

        public PageAdapter(FragmentManager fm, LanguageManager lm) {
            super(fm);
            languageManager = lm;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return BaseFragment.newInstance(MainRoomFragment.class);
                case 1:
                    return BaseFragment.newInstance(MainSceneFragment.class);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return Utils.capitalizer(languageManager.translate("rooms"));
                case 1:
                    return Utils.capitalizer(languageManager.translate("scenes"));
                default:
                    return "";
            }
        }
    }

}
