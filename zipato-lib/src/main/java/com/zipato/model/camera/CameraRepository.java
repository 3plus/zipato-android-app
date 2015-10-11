/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.camera;

import android.util.Log;

import com.zipato.model.UUIDObjectRepository;
import com.zipato.model.client.RestObject;

import java.util.UUID;

/**
 * Created by murielK on 9/8/2014.
 */
public class CameraRepository extends UUIDObjectRepository<Camera> {

    public void fetchAll() {

        Camera[] cameras = factory.getRestTemplate().getForObject("v2/cameras", Camera[].class);
        for (Camera cam : cameras) {
            fetchOne(cam.getUuid(), false);
        }
    }

    public Camera fetchOne(UUID uuid, boolean local) {
        Camera camera = factory.getRestTemplate().getForObject("v2/cameras/{uuid}?local={local}", Camera.class, uuid, local);
        if (camera != null) {
            add(camera);
            return camera;
        }
        return null;
    }

    public boolean performPan(UUID uuid, String camerasActions) {
        Log.e("CameraRepo", "sending pan command");
        RestObject resp = factory.getRestTemplate().getForObject("v2/cameras/{uuid}/ptz/{action}", RestObject.class, uuid, camerasActions);
        Log.e("CameraRepo", "isSuccess? " + resp.isSuccess());
        if (resp.isSuccess()) {
            return true;
        }
        return false;
    }

    public boolean takeSnapShot(UUID uuid) {

        RestObject res = factory.getRestTemplate().getForObject("v2/cameras/{uuid}/takeSnapshot", RestObject.class, uuid);
        if (res.isSuccess())
            return true;

        return false;
    }


}
