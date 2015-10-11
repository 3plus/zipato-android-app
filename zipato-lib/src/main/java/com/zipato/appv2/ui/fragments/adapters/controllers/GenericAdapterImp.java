/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.adapters.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.ViewGroup;

import com.zipato.appv2.ZipatoApplication;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.appv2.ui.fragments.controller.ViewControllerLogic;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCDefault;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.util.TagFactoryUtils;

import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Created by murielK on 7/14/2015.
 */
public class GenericAdapterImp<T extends ViewController> extends Adapter implements GenericAdapter {

    private static final String TAG = TagFactoryUtils.getTag(GenericAdapterImp.class.getSimpleName());
    private static final Random random = new SecureRandom();
    final List<Integer> logicTypeQueues = new Stack<>(); // Stack????
    private final Object lock = new Object();
    private final Handler handler;
    private final Context context;
    private final List<TypeReportItem> items;
    private final RecyclerView recyclerView;

    @Inject
    AttributeRepository attributeRepository;

    private volatile int currentLogicWorkersID = -1;
    private boolean canUpdate = true;
    private boolean sceneMode;

    private List<UUID> exclusionList;
    private ExecutorService logicExecutors; // separate executorService to run logic from viewType
    private Map<Integer, ViewControllerLogic> mapLogicViewController;

    public GenericAdapterImp(Context context, List<TypeReportItem> items, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.items = items;
        handler = new GenericAdapterHandler(this);
        ((ZipatoApplication) this.context.getApplicationContext()).getObjectGraph().inject(this);
        TypeViewControllerFactory.setContext(context);
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        TypeViewControllerFactory.setContext(getContext());
        T viewController = TypeViewControllerFactory.getViewHolder(viewGroup, viewType, recyclerView);
        Log.d(TAG, String.format("onCreateViewHolder will return : %s", viewController.getClass().getSimpleName()));
        if (!isSceneMode() && viewController.hasLogic()) { // ViewController cannot be null anyway ....
            Log.d(TAG, String.format(" %s has logic", viewController.getClass().getSimpleName()));
            if (mapLogicViewController == null) {
                mapLogicViewController = new HashMap<>();
            }
            if (!mapLogicViewController.containsValue(viewController)) {
                mapLogicViewController.put(viewType, (ViewControllerLogic) viewController);
                pushLogic(viewType,
                          (ViewControllerLogic) viewController); //force execution @ onCreateViewHolder?? will this affect performance ?? will this be called constantly after notifyDataSetChanged??
            }
        }
        return viewController;
    }

    @Override
    public long getItemId(int position) {
        return ((items == null) || items.isEmpty()) ? super.getItemId(position) : (long) items.get(position).getUuid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        TypeReportItem item = items == null || items.isEmpty() ? null : items.get(0);
        return TypeViewControllerFactory.getViewType(getContext(), item, attributeRepository);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        if ((items == null) || items.isEmpty()) // Should never happened at least not being null
        {
            return;
        }
        final TypeReportItem item = items.get(i);
        ((ViewController) viewHolder).onBind(item);
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }

    @Override
    public void dataHasChangedNotify() {
        Log.d(TAG, String.format("dataHasChangedNotify is called canUpdate? %s , hasLogicQueues is empty? %s %d", canUpdate, logicTypeQueues.isEmpty(),
                                 logicTypeQueues.size()));

        if (!canUpdate || !logicTypeQueues.isEmpty()) {
            return;
        }

        if ((mapLogicViewController == null) || (mapLogicViewController.isEmpty())) {
            Log.d(TAG, "calling notifyDataSetChanged");
            notifyDataSetChanged();
        } else {
            for (Entry<Integer, ViewControllerLogic> entry : mapLogicViewController.entrySet()) {
                if (logicTypeQueues.contains(entry.getKey())) {
                    continue;
                }
                pushLogic(entry.getKey(), entry.getValue());
            }
        }
    }

    private void pushLogic(int viewType, ViewControllerLogic viewControllerLogic) {
        Log.d(TAG, String.format("pushing logic %s to the queue", viewControllerLogic.getClass().getSimpleName()));
        if (logicExecutors == null) {
            logicExecutors = Executors.newFixedThreadPool(2);
            currentLogicWorkersID = random.nextInt();
        }
        viewControllerLogic.setLogicQueueID(currentLogicWorkersID);
        logicTypeQueues.add(viewType);
        logicExecutors.execute(viewControllerLogic);
    }

    public void reset() {
        if (logicExecutors != null) {
            logicExecutors.shutdownNow();
            logicExecutors = null;
        }
        if (mapLogicViewController != null) {
            mapLogicViewController.clear();
            mapLogicViewController = null;
        }
        logicTypeQueues.clear();
        if (exclusionList != null) {
            exclusionList.clear();
        }
    }

    private boolean validateWorkersID(int id) {
        return currentLogicWorkersID == id;
    }

    @Override
    public void onItemRefresh(final int viewType, final int logicQueueID, final int itemPosition) {
        synchronized (lock) {
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (!validateWorkersID(currentLogicWorkersID)) {
                        Log.e(TAG, "Ignoring this as probably coming from an obsolete thread!");
                        return;
                    }
                    Log.d(TAG,
                          String.format("Refreshing data position: %d for typeView: %s", itemPosition, TypeViewControllerFactory.getCachedVCCls(viewType)));
                    final TypeReportItem item = getTypeReportItem(itemPosition);
                    if ((item != null) && ((exclusionList == null) || !exclusionList.contains(item.getUuid()))) {
                        notifyItemChanged(itemPosition);
                    } else {
                        Log.d(TAG,
                              String.format("Could not call notifyItemChanged... is Item null? %s is exclusionList null? %s or exclusionList contain item? %s",
                                            (item == null), (exclusionList == null),
                                            ((exclusionList != null) && ((item != null) && exclusionList.contains(item.getUuid())))));
                    }
                }
            });
        }
    }

    @Override
    public void logicExecuted(final int viewType, final boolean notify,
                              final int logicQueuesID) { // make sure you call this please if there is extra logic on a view otherwise it wont update the view
        //execute it on the handler as notifyDataSetChanged is a bitch
        synchronized (lock) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // ;lock really necessary ??
                    if (!validateWorkersID(logicQueuesID)) {
                        Log.e(TAG, "Ignoring this as probably coming from an obsolete thread!");
                        return;
                    }
                    Log.d(TAG, String.format("logic of viewType : %s executed with success!!", TypeViewControllerFactory.getCachedVCCls(viewType)));
                    if (!logicTypeQueues.isEmpty()) {
                        if (logicTypeQueues.contains(viewType)) {
                            logicTypeQueues.remove((Integer) viewType);
                        }

                        if ((logicTypeQueues.isEmpty() || notify) && canUpdate) {
                            Log.d(TAG, String.format("refreshing adapter... isNotify? %s isLogicTypeQueues.isEmpty()? %s", notify, logicTypeQueues.isEmpty()));
                            notifyDataSetChanged();
                        }
                    } else {
                        Log.e(TAG, "Empty logicTypeQueues... waiting for next trigger");
                    }
                }
            });
        }

    }

    @Override
    public void logicFailExecution(final int viewType, final int logicQueuesID) {
        logicExecuted(viewType, false, logicQueuesID);
    }

    @Override
    public boolean isExecutorDown() {
        return ((logicExecutors == null) || logicExecutors.isShutdown());
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public TypeReportItem getTypeReportItem(int position) {
        if ((items == null) || items.isEmpty() || (position >= items.size())) {
            return null;
        }
        return items.get(position);
    }

    @Override
    public int findTypeReportItemPos(TypeReportKey key) {
        if ((key == null) || (items == null) || items.isEmpty()) {
            return -1;
        }
        final int size = items.size();
        for (int i = size - 1; i >= 0; i--) {
            if (key.equals(items.get(i).getKey())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int findTypeReportItemPos(UUID key) {
        if ((key == null) || (items == null) || items.isEmpty()) {
            return -1;
        }
        final int size = items.size();
        for (int i = size - 1; i >= 0; i--) {
            if (key.equals(items.get(i).getUuid())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void enableUpdate(boolean canUpdate) {
        handler.removeMessages(GenericAdapterHandler.MSG_ENABLE_ADAPTER_UPDATE);
        this.canUpdate = canUpdate;
    }

    @Override
    public void resetUpdate(long delay) {
        handler.removeMessages(GenericAdapterHandler.MSG_ENABLE_ADAPTER_UPDATE);
        handler.sendEmptyMessageDelayed(GenericAdapterHandler.MSG_ENABLE_ADAPTER_UPDATE, delay);
    }

    @Override
    public void resetUpdate(long delay, UUID uuid) {
        handler.sendMessageDelayed(handler.obtainMessage(GenericAdapterHandler.MSG_ENABLE_ITEM_UPDATE, uuid), delay);
    }

    @Override
    public void enableItemUpdate(UUID uuid) {
        if ((exclusionList == null) || exclusionList.isEmpty()) {
            return;
        }
        exclusionList.remove(uuid);
    }

    @Override
    public void disableItemUpdate(UUID uuid) {
        if (exclusionList == null) {
            exclusionList = new ArrayList<>();
        }
        if (!exclusionList.contains(uuid)) {
            exclusionList.add(uuid);
        }
    }

    @Override
    public boolean isSceneMode() {
        return sceneMode;
    }

    @Override
    public void setSceneMode(boolean sceneMode) {
        this.sceneMode = sceneMode;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
//        if ((mapLogicViewController != null) && mapLogicViewController.containsValue(holder))
//            mapLogicViewController.remove(holder);
//        TypeViewControllerFactory.onViewRecycled((T) holder);
    }

    private static class GenericAdapterHandler extends Handler {

        public static final int MSG_ENABLE_ADAPTER_UPDATE = 0;

        public static final int MSG_ENABLE_ITEM_UPDATE = 1;

        private final WeakReference<GenericAdapter> weakReference;

        GenericAdapterHandler(GenericAdapter genericAdapter) {
            weakReference = new WeakReference<>(genericAdapter);
        }

        @Override
        public void handleMessage(Message msg) {

            final GenericAdapter genericAdapter = weakReference.get();
            if (genericAdapter == null) {
                super.handleMessage(msg);
                return;
            }
            switch (msg.what) {
                case MSG_ENABLE_ADAPTER_UPDATE:
                    genericAdapter.enableUpdate(true);
                    break;
                case MSG_ENABLE_ITEM_UPDATE:
                    genericAdapter.enableItemUpdate((UUID) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

}
