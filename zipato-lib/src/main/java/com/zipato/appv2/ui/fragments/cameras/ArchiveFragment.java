/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.cameras;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.activities.ScreenShotActivity;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.appv2.ui.fragments.cameras.ArchiveFileProvider.OnUpdateListner;
import com.zipato.helper.DeleteDialogHelper;
import com.zipato.model.DynaObject;
import com.zipato.model.camera.SVFileRest;
import com.zipato.model.event.Event;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import butterfork.ButterFork;
import butterfork.Bind;
import butterfork.OnClick;
import butterfork.OnItemClick;
import butterfork.OnItemLongClick;


/**
 * Created by murielK on 1/29/2015.
 */
public class ArchiveFragment extends BaseCameraFragment implements OnUpdateListner {


    public static final int ON_FILES_CHANGE = 500;
    private static final String TAG = ArchiveFragment.class.getSimpleName();
    private static final Object lock = new Object();
    public static int page = 0;
    @Inject
    List<SVFileRest> files;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Bind(B.id.progressBarMiddle)
    ProgressBar progressBarMiddle;
    @Bind(B.id.progressBarBottom)
    ProgressBar progressBarBottom;
    @Bind(B.id.gridViewThumb)
    GridView gridViewThumb;
    @Bind(B.id.buttonCalendar)
    FloatingActionButton fab;
    @Bind(B.id.textArchiveDate)
    TextView date;
    @Bind(B.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;
    @Inject
    ArchiveFileProvider archiveFileProvider;
    private boolean noMoreFlag = false;
    private long currentWorkerThreadID;
    private ThumbnailListAdapter adapter;
    private String startDate;
    private String endDate;
    private ActionMode mActionMode;


    @OnClick(B.id.buttonCalendar)
    public void onCalendarClick(View v) {

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Locale locale = (getActivity()).getResources().getConfiguration().locale;
                SimpleDateFormat time = new SimpleDateFormat("MMMM dd yyyy", locale);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                date.setText(time.format(calendar.getTime()));
                startDate = format.format(calendar.getTime());
                Log.d(TAG, "startDate: " + startDate);
                calendar.add(Calendar.MINUTE, 1439);
                endDate = format.format(calendar.getTime());
                Log.d(TAG, "endDate: " + endDate);
                onDateSetFetch();

            }
        }, calendar.get(Calendar.YEAR), calendar.get(calendar.MONTH), calendar.get(calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void onDateSetFetch() {
        noMoreFlag = false;
        currentWorkerThreadID = 0;
        files.clear();
        adapter.notifyDataSetChanged();
        eventBus.post(ON_FILES_CHANGE);
        page = 0;
        archiveFileProvider.fetchSVFile(this, startDate, endDate, page);

    }


    @OnItemLongClick(B.id.gridViewThumb)
    public boolean onItemLongClick(View v, int position) {
        adapter.toggleSelection(position);
        final int checkedCount = adapter.getSelectedCount();
        setOnActionBarMenu(checkedCount);
        return true;
    }

    @OnItemClick(B.id.gridViewThumb)
    public void onItemClick(View v, int position) {
        if ((mActionMode == null) || (adapter.getSelectedCount() == 0)) {

            Intent intent = new Intent(getActivity(), ScreenShotActivity.class);
            intent.putExtra(FragmentScreenShot.INDEX_KEY, position);
            startActivity(intent);

        } else {
            adapter.toggleSelection(position);
            final int checkedCount = adapter.getSelectedCount();
            setOnActionBarMenu(checkedCount);
        }
    }

    @Override
    protected boolean registerTimeout() {
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        archiveFileProvider.registerUpdate(this);
        adapter = new ThumbnailListAdapter();
        gridViewThumb.setAdapter(adapter);
        gridViewThumb.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (noMoreFlag || archiveFileProvider.isRunning() || (totalItemCount == 0)) {
                    //    Log.d(TAG, "returning onScroll, noMoreFlag? " + noMoreFlag + " isArchiveRunning? " + archiveFileProvider.isRunning() + " totalCount: " + totalItemCount + " page " + page);
                    return;
                }
                if ((firstVisibleItem + visibleItemCount) == totalItemCount)
                    archiveFileProvider.fetchSVFile(ArchiveFragment.this, startDate, endDate, page);
                //  Log.d(TAG, view.getId() + " firstVisibleItem: " + view.getFirstVisiblePosition() + " visibleItemCount: " + visibleItemCount + " totalItemCount: " + totalItemCount + " page: " + page);

            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                restart();
            }
        });

        archiveFileProvider.fetchSVFile
                (this, page);

        fab.attachToListView(gridViewThumb);
        date.setText(languageManager.translate("recent"));

    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_archive;
    }

    @Override
    protected void onPostViewCreate() {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        files.clear();
        eventBus.post(ON_FILES_CHANGE);
        page = 0;
        archiveFileProvider.unregisterUpdate(this);
        if (mActionMode != null)
            mActionMode.finish();
        Log.e(TAG, "Destroyed called fileSize: " + files.size());

    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onPostStart(long threadID) {
        currentWorkerThreadID = threadID;
        adapter.notifyDataSetChanged();
        if (files.isEmpty() && !swipeRefreshLayout.isRefreshing())
            progressBarMiddle.setVisibility(View.VISIBLE);
        else if (!swipeRefreshLayout.isRefreshing())
            progressBarBottom.setVisibility(View.VISIBLE);
        Log.e(TAG, "Thread Fucking ID: " + threadID);
    }

    @Override
    public void onFileFound(SVFileRest[] svFileRests, long threadID) {
        Log.d(TAG, "CurrentThreadID: " + currentWorkerThreadID + " incoming ThreadID: " + threadID);
        if (currentWorkerThreadID == threadID) {
            for (SVFileRest sv : svFileRests) {
                if (!files.contains(sv))
                    files.add(sv);
            }
            adapter.notifyDataSetChanged();
            eventBus.post(ON_FILES_CHANGE);
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            if (svFileRests.length < 100)
                noMoreFlag = true;
            else
                page += 1;

        } else {
            Log.e(TAG, "this result is not coming from current/latest Request, CurrentThreadID: " + currentWorkerThreadID + " incoming ThreadID: " + threadID);
        }
        disableProgress();

    }

    private void disableProgress() {
        if (progressBarMiddle.getVisibility() == View.VISIBLE)
            progressBarMiddle.setVisibility(View.GONE);
        if (progressBarBottom.getVisibility() == View.VISIBLE)
            progressBarBottom.setVisibility(View.GONE);
        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onFail() {

        disableProgress();
    }

    @Override
    public void noFileFound() {
        noMoreFlag = true;
        disableProgress();
    }


    private void restart() {
        date.setText(languageManager.translate("recent"));
        endDate = "";
        startDate = "";
        noMoreFlag = false;
        currentWorkerThreadID = 0;
        files.clear();
        eventBus.post(ON_FILES_CHANGE);
        adapter.notifyDataSetChanged();
        page = 0;
        archiveFileProvider.fetchSVFile(this, page);
    }

    @Override
    public void onEventMainThread(Event event) {
        super.onEventMainThread(event);
        if (event.eventType == Event.EVENT_TYPE_REFRESH_REQUEST) {
            try {
                restart();
            } catch (Exception e) {

                Log.d(TAG, "", e);
            }
        }
    }

    private void setOnActionBarMenu(int checkedCount) {
        if (checkedCount > 0) {
            if (mActionMode == null) {
                mActionMode = getActivity().startActionMode(new ModeCallback());
            }
            int tempAdapterSize = adapter.getCount();
            if (checkedCount > 1) {
                if (tempAdapterSize == checkedCount) {
                    mActionMode.getMenu().findItem(R.id.selectAll).setVisible(false);
                } else {
                    mActionMode.getMenu().findItem(R.id.selectAll).setVisible(true);
                }
            }
            mActionMode.setTitle(String.valueOf(checkedCount));
        } else {
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }
    }

    class ThumbnailListAdapter extends BaseListAdapter {

        @Override
        public int getCount() {

            return files.size();
        }

        @Override
        public SVFileRest getItem(int position) {
            return files.get(position);
        }

        @Override
        public long getItemId(int position) {
            return files.get(position).getCreated().getTime();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_archive_thumb, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //viewHolder.progressBar.setVisibility(View.VISIBLE);
            if (isSelected(position)) {

                viewHolder.imgChecked.setVisibility(View.VISIBLE);
            } else {
                viewHolder.imgChecked.setVisibility(View.GONE);

            }
            try {
                picasso.load(files.get(position).getThumbnailUrl()).fit().into(viewHolder.thumb, new Callback() {
                    @Override
                    public void onSuccess() {
                        //
                    }

                    @Override
                    public void onError() {
                        try {
                            viewHolder.thumb.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_img_fail));
                        } catch (Exception e) {
                            //empty
                        }


                    }
                });
            } catch (Exception e) {

                Log.d(TAG, "", e);
            }
            return convertView;
        }

        class ViewHolder {
            @Bind(B.id.imageViewThumb)
            ImageView thumb;
            @Bind(B.id.imageViewChecked)
            ImageView imgChecked;

            public ViewHolder(View v) {

                ButterFork.bind(this, v);

            }
        }
    }

    private final class ModeCallback implements ActionMode.Callback {


        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

            MenuInflater menuInflater = getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.menu_on_screen_shot_select, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            final SparseBooleanArray selected = adapter
                    .getSelectedIds();

            int i1 = menuItem.getItemId();
            if (i1 == R.id.selectAll) {
                int tempAdapterSize = adapter.getCount();
                for (int i = 0; i < tempAdapterSize; i++) {

                    if (!selected.get(i)) {
                        adapter.toggleSelection(i);
                    }
                }

                final int checkedItemCount = adapter.getSelectedCount();
                setOnActionBarMenu(checkedItemCount);

            } else if (i1 == R.id.delete) {
                Locale locale = getActivity().getResources().getConfiguration().locale;
                final SimpleDateFormat time = new SimpleDateFormat("MMM dd, HH:mm:ss", locale);
                final SparseBooleanArray sparseBooleanArray = adapter
                        .getSelectedIds();
                final int size = sparseBooleanArray.size();
                List<String> tempList = new ArrayList<String>();
                for (int i = 0; i < size; i++) {
                    tempList.add(time.format(files.get(sparseBooleanArray.keyAt(i)).getCreated()));
                }
                String text1 = languageManager.translate("remove_screen_shot_text_msg");
                String dialogTitle = (languageManager.translate("dialog_remove_screen_shot_title") + " (" + tempList.size() + ")");
                String positiveText = languageManager.translate("delete");
                String negativeText = languageManager.translate("cancel");

                DeleteDialogHelper deleteDialogHelper = new DeleteDialogHelper(getActivity(), tempList, text1, R.drawable.ic_warning, dialogTitle, negativeText,
                                                                               positiveText, new DeleteDialogHelper.OnPositiveClicked() {
                    @Override
                    public void onPositiveClicked() {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {

                                if (internetConnectionHelper.isOnline()) {
                                    if (size == 1) {
                                        baseFragmentHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showProgressDialog(languageManager.translate("removing_screen_shot") + " " +
                                                                           time.format(files.get(sparseBooleanArray.keyAt(0)).getCreated()), false);
                                            }
                                        });
                                        try {
                                            restTemplate.delete("v2/sv/{uuid}", files.get(sparseBooleanArray.keyAt(0)).getId());
                                        } catch (Exception e) {
                                            handlerException(e, TAG);
                                        }

                                    } else if (size > 1) {
                                        baseFragmentHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showProgressDialog(languageManager.translate("removing_screen_shot"), false);
                                            }
                                        });
                                        final List<String> listIDs = new ArrayList<String>();
                                        for (int i = 0; i < size; i++) {
                                            listIDs.add(files.get(sparseBooleanArray.keyAt(i)).getId());
                                        }
                                        try {
                                            restTemplate.postForObject("v2/sv/deleteBatch", listIDs, DynaObject.class);
                                        } catch (Exception e) {
                                            handlerException(e, TAG);
                                        }
                                    }
                                } else {
                                    baseFragmentHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            toast(languageManager.translate("internet_error"));
                                        }
                                    });

                                }
                                baseFragmentHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            restart();
                                        } catch (Exception e) {

                                        }
                                        dismissProgressDialog();
                                    }
                                });

                            }
                        });

                    }
                });
                deleteDialogHelper.show();
                if (mActionMode != null)
                    mActionMode.finish();

            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            adapter.removeSelection();
            if (mActionMode == actionMode) {
                mActionMode = null;
            }
        }
    }
}
