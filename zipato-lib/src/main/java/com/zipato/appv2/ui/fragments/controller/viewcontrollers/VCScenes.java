/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.model.scene.SceneRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterfork.Bind;
import butterfork.OnClick;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 9/14/2015.
 */
@ViewType("view_controller_scenes")
public class VCScenes extends AbsHeader {

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.butRunScene)
    TextView butRunScene;

    @Inject
    SceneRepository sceneRepository;
    @Inject
    ExecutorService executor;

    public VCScenes(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        butRunScene.setText(capitalizer(languageManager.translate("but_run_scene_title").toLowerCase()));
        buttonSubMenu.setVisibility(View.INVISIBLE); // temporary disabling scenes Menu
    }

    @OnClick(B.id.butRunScene)
    public void onRunSceneClick(View v) {
        final TypeReportItem item = getTypeReportItem();
        if (item != null)
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        sceneRepository.runScene(item.getUuid());
                    } catch (Exception e) {
                        Log.d(TagFactoryUtils.getTag(VCScenes.class), "", e);
                    }
                }
            });
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        final TypeReportItem item = (TypeReportItem) object;
        textViewKK.setTextColor(Color.parseColor(item.getUserIcon().getColor()));
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return 0;
    }

    @Override
    public boolean hasLogic() {
        return false;
    }


}
