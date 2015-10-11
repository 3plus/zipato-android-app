/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.bm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;

import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.activities.ShowDialogActivity;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.DividerItemDecoration;
import com.zipato.appv2.ui.fragments.adapters.bm.RecyclerTouchEventListener;
import com.zipato.appv2.ui.fragments.adapters.bm.ScenesAdapter;
import com.zipato.appv2.ui.fragments.vcmenu.ScenesIconColorFragment;
import com.zipato.helper.DeleteDialogHelper;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.scene.Scene;
import com.zipato.model.scene.SceneRepository;
import com.zipato.util.TagFactoryUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.Bind;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 10/5/2015.
 */
public class SceneFragment extends BaseFragment implements RecyclerTouchEventListener {
    private static final String TAG = TagFactoryUtils.getTag(RoomFragment.class);

    @Inject
    EventBus eventBus;
    @Inject
    SceneRepository sceneRepository;
    @Inject
    ExecutorService executor;
    @Inject
    List<Scene> scenes;

    @Bind(B.id.recyclerViewScenes)
    RecyclerView customRecyclerView;

    private ScenesAdapter scenesAdapter;
    private Scene currentScene;


    @Override
    protected int getResourceView() {
        return R.layout.fragment_left_content_scenes;
    }

    @Override
    protected void onPostViewCreate() {
        scenesAdapter = new ScenesAdapter(getContext(), this);
        customRecyclerView.setHasFixedSize(false);
        customRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        customRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), R.drawable.line_separator_empty, DividerItemDecoration.VERTICAL_LIST));
        customRecyclerView.setAdapter(scenesAdapter);
    }

    @Override
    public void onClick(int position) {
        final Scene scene = scenes.get(position);
        if (scene == null)
            return;

        scenesAdapter.toggleSingleItem(position);

        eventBus.post(new Event(new ObjectItemsClick(ObjectItemsClick.SCENES, position), Event.EVENT_TYPE_ITEM_CLICK));
        runScene(scene);
    }

    @Override
    public void onLongClick(int position) {
        currentScene = scenes.get(position);
        showDialogItems();
    }

    private void changeSceneName() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) ((15 * scale) + 0.5f);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpAsPixels;
        params.topMargin = dpAsPixels;
        final EditText editText = new EditText(getContext());
        editText.setLayoutParams(params);
        editText.setGravity(Gravity.CENTER);
        editText.setHint((currentScene.getName() == null) ? languageManager.translate("room_name") : currentScene.getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(languageManager.translate("rename") + ((currentScene.getName() == null) ? "" : ": " + currentScene.getName()));
        builder.setView(editText);
        builder.setPositiveButton(languageManager.translate("rename"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ((editText.getText() == null) || editText.getText().toString().isEmpty()) {
                    toast(languageManager.translate("invalid_room_name"));
                    return;
                }

                updateSceneName(editText.getText().toString());

            }
        });

        builder.setNegativeButton(languageManager.translate("cancel"), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void updateSceneName(String sceneName) {
        currentScene.setName(sceneName);
        scenesAdapter.notifySelection();
    }

    private void showDialogItems() {
        final String[] items = new String[]{languageManager.translate("rename"), languageManager.translate("change_icon"), languageManager.translate("edit_devices"), languageManager.translate("delete")};
        Builder builder = new Builder(getContext());
        builder.setTitle((currentScene.getName() == null) ? "" : currentScene.getName());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        changeSceneName();
                        break;
                    case 1:
                        dispatchChangeIcon();
                        break;
                    case 2:
                        editDevice();
                        break;
                    case 3:
                        deleteScene();
                        break;
                }
            }
        });

        builder.show();
    }

    private void dispatchChangeIcon() {
        ShowDialogActivity.showForResult(getParentFragment(), currentScene.getUuid(), ShowDialogActivity.SHOW_ICON_COLOR_ID, ScenesIconColorFragment.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (requestCode == ScenesIconColorFragment.REQUEST_CODE)) {
            currentScene.setIconColor(data.getStringExtra(ScenesIconColorFragment.COLOR_KEY));
            currentScene.setIcon(data.getStringExtra(ScenesIconColorFragment.KK_KEY));
            final int index = scenes.indexOf(currentScene);
            if (index >= 0)
                scenesAdapter.notifyItemChanged(index);
        }
    }

    private void deleteScene() {
        DeleteDialogHelper deleteDialogHelper = new DeleteDialogHelper(getContext(), Collections.singletonList((currentScene.getName() == null) ? "" : currentScene.getName()), "", R.drawable.ic_warning, languageManager.translate("remove"), languageManager.translate("cancel"), languageManager.translate("remove"), new DeleteDialogHelper.OnPositiveClicked() {
            @Override
            public void onPositiveClicked() {
                if (!checkInternet())
                    return;
                performDeleteScene();

            }
        });
        deleteDialogHelper.show();
    }

    private void performDeleteScene() {
        final int itemPos = scenes.indexOf(currentScene);
        currentScene.setFlag(Scene.FLAG_DELETED);
        scenesAdapter.notifyItemRemoved(itemPos);
        scenesAdapter.getSelectedIds().delete(itemPos);
        eventBus.post(new Event(new ObjectItemsClick(ObjectItemsClick.SCENES, scenes.indexOf(currentScene)), Event.EVENT_TYPE_ITEM_CLICK));
        scenes.remove(currentScene);
    }

    private void editDevice() {
        final int index = scenes.indexOf(currentScene);
        scenesAdapter.toggleSingleItem(index);
        eventBus.post(new Event(new ObjectItemsClick(ObjectItemsClick.SCENES, index), Event.EVENT_TYPE_ITEM_CLICK));
    }

    public void onEventMainThread(Event event) {
        if (event.eventType == Event.EVENT_TYPE_LIST_VIEW_REFRESH) {
            final ObjectListRefresh objectListRefresh = (ObjectListRefresh) event.eventObject;
            if (objectListRefresh.fromTo == ObjectItemsClick.SCENES) {
                Log.d(TAG, "Refreshing scenes adapter");
                if (objectListRefresh.reset)
                    scenesAdapter.clearSelections();
                scenesAdapter.notifyDataSetChanged();
            }
        }
    }

    private void runScene(final Scene scene) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sceneRepository.runScene(scene.getUuid());

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scenes.clear();
    }
}
