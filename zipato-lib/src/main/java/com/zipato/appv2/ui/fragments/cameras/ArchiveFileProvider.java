/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.cameras;

import android.util.Log;

import com.zipato.model.camera.SVFileRest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murielK on 1/29/2015.
 */
public class ArchiveFileProvider {

    private static String TAG = ArchiveFileProvider.class.getSimpleName();
    private static volatile ArchiveFileProvider archiveFileProvider;
    private final Object lock = new Object();
    private boolean isRunning;
    private Thread thread;
    private List<OnUpdateListner> listeners;

    public boolean isRunning() {
        return isRunning;
    }


    private <T extends BaseCameraFragment> Thread initThread(final T fragment, final String startDate, final String endDate, final int page) {
        Log.d(TAG, "initializing new thread");
        return new Thread(new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                if ((startDate == null) || (endDate == null)) {
                    try {
                        final String uuid = fragment.getCamera().getUuid().toString();

                        final SVFileRest[] svFileRests = fragment.getRestTemplate().getForObject("v2/sv/camera/" + uuid + "?page=" + page + "&pageSize=100", SVFileRest[].class);
                        fragment.getBaseFragmentHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if ((svFileRests == null) || (svFileRests.length == 0))
                                    fireNoFileFound();
                                else {
                                    fireOnUpdateDone(svFileRests, thread.getId());
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.d(TAG, "", e);
                        fragment.getBaseFragmentHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                fireOnUpdateFail();
                            }
                        });
                    }
                } else {

                    try {
                        final String uuid = fragment.getCamera().getUuid().toString();
                        final SVFileRest[] svFileRests = fragment.getRestTemplate().getForObject("v2/sv/camera/" + uuid + "?page=" + page + "&pageSize=100&startDate=" + startDate + "&endDate=" + endDate, SVFileRest[].class);
                        fragment.getBaseFragmentHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if ((svFileRests == null) || (svFileRests.length == 0))
                                    fireNoFileFound();
                                else
                                    fireOnUpdateDone(svFileRests, thread.getId());
                            }
                        });

                    } catch (Exception e) {
                        Log.d(TAG, "", e);
                        fragment.getBaseFragmentHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                fireOnUpdateFail();
                            }
                        });

                    }

                }
                isRunning = false;
            }
        }, TAG + "-thread-");
    }


    public <T extends BaseCameraFragment> void fetchSVFile(final T fragment, final int page) {
        fetchSVFile(fragment, null, null, page);
    }

    public <T extends BaseCameraFragment> void fetchSVFile(final T fragment, final String startDate, final String endDate, final int page) {
        Log.d(TAG, "is thread running? " + isRunning);
        if (isRunning && (thread != null)) {
            thread.interrupt();
            isRunning = false;
            thread = null;
        }
        thread = initThread(fragment, startDate, endDate, page);
        thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        fireOnPostStart(thread.getId());
        thread.start();
    }

    public void registerUpdate(OnUpdateListner listner) {
        if (listeners == null)
            listeners = new ArrayList<>();

        listeners.add(listner);
    }

    public void unregisterUpdate(OnUpdateListner listner) {

        if (!verifyListeners())
            return;
        listeners.remove(listner);

    }

    public void removeAllListener() {
        if (!verifyListeners())
            return;
        listeners.clear();

    }

    private boolean verifyListeners() {
        return (listeners != null) && !listeners.isEmpty();

    }

    private void fireOnUpdateDone(final SVFileRest[] svFileRests, final long threadID) {
        if (!verifyListeners())
            return;
        for (OnUpdateListner listner : listeners) {
            listner.onFileFound(svFileRests, threadID);
        }
    }

    private void fireOnUpdateFail() {
        if (!verifyListeners())
            return;
        for (OnUpdateListner listner : listeners) {
            listner.onFail();
        }
    }

    private void fireNoFileFound() {
        if (!verifyListeners())
            return;
        for (OnUpdateListner listner : listeners) {
            listner.noFileFound();
        }
    }

    private void fireOnPostStart(final long threadID) {
        if (!verifyListeners())
            return;
        for (OnUpdateListner listner : listeners) {
            listner.onPostStart(threadID);
        }
    }

    public interface OnUpdateListner {

        void onPostStart(final long threadID);

        void onFileFound(final SVFileRest[] svFileRests, final long threadID);

        void onFail();

        void noFileFound();
    }
}