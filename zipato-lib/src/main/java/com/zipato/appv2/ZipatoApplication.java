/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2;

import android.app.Application;

import com.zipato.icon.IconUpdater;

import dagger.ObjectGraph;

/**
 * Created by murielK on 30.5.2014..
 */
public class ZipatoApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IconUpdater iconUpdater = new IconUpdater(this);
        iconUpdater.checkForNewIcon();
    }

    public synchronized ObjectGraph getObjectGraph() {
        if (objectGraph == null) {
            objectGraph = ObjectGraph.create(new ZipatoApplicationModule(this));
        }

        return objectGraph;
    }


    public void inject(Object object) {
        getObjectGraph().inject(object);
    }
}

