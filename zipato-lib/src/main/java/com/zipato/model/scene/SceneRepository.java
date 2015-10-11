/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.scene;

import com.zipato.model.Constant;
import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.types.UserIcons;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by murielK on 30.6.2014..
 */
public class SceneRepository extends UUIDObjectRepository<Scene> {

    private Map<UUID, String> cachedAttrValue;

    public static TypeReportItem adaptSceneToType(Scene scene) {
        TypeReportItem item = new TypeReportItem();
        item.setTemplateId(Constant.SCENE_FAKE_TEMPLATE_ID);
        item.setName((scene.getName() == null) ? "" : scene.getName());
        item.setUuid(scene.getUuid());
        UserIcons userIcons = new UserIcons();
        userIcons.setRelativeUrl("font:" + (scene.getIcon() == null ? Constant.SCENE_DEFAULT_KITKAT : scene.getIcon()));
        userIcons.setColor((scene.getIconColor() == null) ? Constant.SCENE_DEFAULT_KITKAT_COLOR : scene.getIconColor());
        item.setUserIcon(userIcons);
        item.setTags(scene.getTags());
        return item;
    }

    public void fetchAll() {
        Scene[] scenes = factory.getRestTemplate().getForObject("v2/scenes?x=icon,iconColor", Scene[].class);
        clear();
        clearCacheAttr();
        addAll(scenes);
    }

    public boolean runScene(UUID uuid) {
        ResponseEntity responseEntity = factory.getRestTemplate().getForEntity("v2/scenes/{uuid}/run", ResponseEntity.class, uuid);
        return responseEntity.getStatusCode().series() == HttpStatus.Series.SUCCESSFUL;
    }

    public void fetchOne(UUID uuid) {
        Scene scene = factory.getRestTemplate().getForObject("v2/scenes/{uuid}", Scene.class, uuid);
        add(scene);
    }

    public Collection<TypeReportItem> valueToTypes() {
        Collection<TypeReportItem> scenesTypes = new ArrayList<>(values().size());
        for (Scene scene : values()) {
            scenesTypes.add(adaptSceneToType(scene));
        }
        return scenesTypes;
    }

    @Override
    public Scene add(Scene scene) {
        final SceneSetting[] sceneSetting = scene.getSettings();
        if (sceneSetting != null)
            for (SceneSetting ss : sceneSetting) {
                if (cachedAttrValue == null) cachedAttrValue = new HashMap<>();
                cachedAttrValue.put(ss.getAttributeUuid(), ss.getValue());
            }
        super.add(scene);
        return scene;
    }

    private void clearCacheAttr() {
        if (cachedAttrValue == null)
            return;
        cachedAttrValue.clear();
    }

    public String getSettingValueFor(UUID attrUUID) {
        return (cachedAttrValue == null) ? null : cachedAttrValue.get(attrUUID);
    }

    public void putSettingValueFor(UUID attrUUID, String value) {
        if (cachedAttrValue == null) cachedAttrValue = new HashMap<>();
        cachedAttrValue.put(attrUUID, value);
    }

    public void deleteScene(UUID uuid) {
        factory.getRestTemplate().delete("v2/scenes/{uuid}", uuid);
    }
}
