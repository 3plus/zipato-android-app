/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.controllers;

import android.content.Context;
import android.os.Handler;

import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;

import java.util.UUID;

/**
 * Created by murielK on 7/14/2015.
 */
public interface GenericAdapter {

    int findTypeReportItemPos(TypeReportKey key);

    int findTypeReportItemPos(UUID key);

    void dataHasChangedNotify();

    void onItemRefresh(int viewType, int logicQueueID, int itemPosition);

    void logicExecuted(int viewType,final boolean notify, final int logicQueuesID);

    void logicFailExecution(int viewType, int logicQueueID);

    boolean isExecutorDown();

    Context getContext();

    Handler getHandler();

    TypeReportItem getTypeReportItem(int position);

    void enableUpdate(boolean canUpdate);

    void resetUpdate(long delay);

    void resetUpdate(long delay, UUID uuid);

    void enableItemUpdate(UUID uuid);

    void disableItemUpdate(UUID uuid);

    boolean isSceneMode();

    void setSceneMode(boolean sceneMode);

    class Command {
        public UUID key;
        public String value;

        public Command(UUID key, String value) {
            this.value = value;
            this.key = key;
        }
    }

}
