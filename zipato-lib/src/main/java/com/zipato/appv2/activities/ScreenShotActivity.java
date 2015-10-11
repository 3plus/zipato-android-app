/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.zipato.appv2.R;
import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.cameras.ArchiveFragment;
import com.zipato.appv2.ui.fragments.cameras.FragmentScreenShot;
import com.zipato.appv2.ui.fragments.cameras.PhotoController;
import com.zipato.customview.CustomViewPager;
import com.zipato.helper.DeleteDialogHelper;
import com.zipato.model.camera.SVFileRest;
import com.zipato.translation.LanguageManager;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 2/2/2015.
 */
public class ScreenShotActivity extends AppCompatActivity implements PhotoController.OnPhotoControlListner {

    @InjectView(R.id.viewPagerSS)
    CustomViewPager viewPager;
    @Inject
    ExecutorService executor;
    @Inject
    LanguageManager languageManager;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    List<SVFileRest> files;
    PageAdapter pageAdapter;
    PhotoController photoController;
    private boolean isConfigChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
                LayoutParams.FLAG_FULLSCREEN);
        if (VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        super.onCreate(savedInstanceState);
        Log.e("ScreenShotActivity", "onCreate");

        ((ZipatoApplication) getApplication()).inject(this);
        setContentView(R.layout.activity_screen_shot);
        getWindow().getDecorView().findViewById(android.R.id.content).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if ((photoController != null) && isConfigChange) {
                    if (photoController.isShowing()) {
                        photoController.hide();
                        photoController.show(8000);
                    }
                }
                isConfigChange = false;
            }
        });
        ButterKnife.inject(this);
        EventBus.getDefault().register(this); //FIXME  this is to receive even when files are behind modify : its a stupid hack to fix the stupid viewpager getCount being call somewhere even tough the activity is closed  :FUCK YOU GOOGLE
        photoController = new PhotoController.Builder(this).setListner(this).build();
        pageAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);
        if ((!files.isEmpty()) && (getIntent() != null)) {
            viewPager.setCurrentItem(getIntent().getIntExtra(FragmentScreenShot.INDEX_KEY, 0));
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("ScreenShotActivity", "onDestroy");
        EventBus.getDefault().unregister(this);

    }

    private void onDelete(final int index) throws Exception {
        if (!files.isEmpty()) {
            final Locale locale = getResources().getConfiguration().locale;
            final SimpleDateFormat time = new SimpleDateFormat("MMM dd, HH:mm:ss", locale);
            String text1 = languageManager.translate("remove_screen_shot_text_msg");
            String dialogTitle = (languageManager.translate("dialog_remove_screen_shot_title"));
            String positiveText = languageManager.translate("delete");
            String negativeText = languageManager.translate("cancel");
            DeleteDialogHelper deleteDialogHelper = new DeleteDialogHelper(this, Collections.singletonList(time.format(files.get(index).getCreated())), text1, R.drawable.ic_warning, dialogTitle, negativeText, positiveText, new DeleteDialogHelper.OnPositiveClicked() {
                @Override
                public void onPositiveClicked() {
                    final ProgressDialog progressDialog = new ProgressDialog(ScreenShotActivity.this);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(languageManager.translate("removing_screen_shot") + " " + time.format(files.get(index).getCreated()));
                    progressDialog.show();
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            boolean isOk = true;
                            try {
                                restTemplate.delete("v2/sv/{id}", files.get(index).getId());
                            } catch (Exception e) {
                                Log.d("ScreenShotActivity", "", e);
                                isOk = false;
                            }

                            final boolean finalIsOk = isOk;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (finalIsOk) {
                                        if (!files.isEmpty()) {
                                            files.remove(index);
                                        }
                                        if (pageAdapter != null)
                                            pageAdapter.notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(ScreenShotActivity.this, languageManager.translate("deletion_fail"), Toast.LENGTH_LONG).show();
                                    }
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });

                }
            });
            deleteDialogHelper.show();
        }
    }

    public void onEventMainThread(Integer onUpdate) {
        if (onUpdate == ArchiveFragment.ON_FILES_CHANGE)
            pageAdapter.notifyDataSetChanged(); //FIXME FUCK YOU GOOGLE
    }

    public void onEventMainThread(View v) {
        if (photoController.isShowing())
            photoController.hide();
        else
            photoController.show(8000);
    }


    @Override
    public void onDeleteClick() {
        photoController.hide(8000);
        try {
            onDelete(viewPager.getCurrentItem());
        } catch (Exception e) {
            Log.d("ScreenShotActivity", "", e);
            Toast.makeText(this, languageManager.translate("deletion_fail"), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onDownloadClick() {
        photoController.hide(8000);
    }

    @Override
    public void onShow() {


    }

    @Override
    public void onHide() {


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isConfigChange = true;

    }

    class PageAdapter extends FragmentStatePagerAdapter {

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentScreenShot.getInstance(position);
        }

        @Override
        public int getCount() {
            return files.size();
        }

    }
}
