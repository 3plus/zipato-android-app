/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.bm;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;

import com.zipato.appv2.R;
import com.zipato.appv2.ui.fragments.BaseFragment;
import com.zipato.appv2.ui.fragments.adapters.DividerItemDecoration;
import com.zipato.appv2.ui.fragments.adapters.bm.RecyclerTouchEventListener;
import com.zipato.appv2.ui.fragments.adapters.bm.RoomAdapter;
import com.zipato.model.event.Event;
import com.zipato.model.event.ObjectItemsClick;
import com.zipato.model.event.ObjectListRefresh;
import com.zipato.model.room.RoomRepository;
import com.zipato.model.room.Rooms;
import com.zipato.util.TagFactoryUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by murielK on 9/9/2015.
 */
public class RoomFragment extends BaseFragment implements RecyclerTouchEventListener {

    private static final String TAG = TagFactoryUtils.getTag(RoomFragment.class);

    @Inject
    EventBus eventBus;

    @InjectView(R.id.recyclerViewRoom)
    RecyclerView customRecyclerView;

    @Inject
    ExecutorService executor;
    @Inject
    RoomRepository roomRepository;
    @Inject
    List<Rooms> rooms;

    private Rooms currentRoom;

    private RoomAdapter roomAdapter;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null)
            getParentFragment().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

    }

    private void dispatchSelectPictureIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getContext().getPackageManager()) != null)
            getParentFragment().startActivityForResult(photoPickerIntent, REQUEST_SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode != Activity.RESULT_OK) || (data == null))
            return;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                currentRoom.setStringUri(data.getDataString());
                roomAdapter.notifyDataSetChanged();
                commitRoomsRepo();
                break;
            case REQUEST_SELECT_PICTURE:
                currentRoom.setStringUri(data.getDataString());
                roomAdapter.notifyDataSetChanged();
                commitRoomsRepo();
                break;
        }
    }

    private void changeRoomName() {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) ((15 * scale) + 0.5f);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpAsPixels;
        params.topMargin = dpAsPixels;
        final EditText editText = new EditText(getContext());
        editText.setLayoutParams(params);
        editText.setGravity(Gravity.CENTER);
        editText.setHint((currentRoom.getName() == null) ? languageManager.translate("room_name") : currentRoom.getName());
        Builder builder = new Builder(getContext());

        builder.setTitle(languageManager.translate("rename") + ((currentRoom.getName() == null) ? "" : ": " + currentRoom.getName()));
        builder.setView(editText);
        builder.setPositiveButton(languageManager.translate("rename"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if ((editText.getText() == null) || editText.getText().toString().isEmpty()) {
                    toast(languageManager.translate("invalid_room_name"));
                    return;
                }

                updateRoom(editText.getText().toString());

            }
        });

        builder.setNegativeButton(languageManager.translate("cancel"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void updateRoom(final String roomName) {
        showProgressDialog(languageManager.translate("updating_room"), false);
        sendNewRomName(currentRoom.getId(), roomName, new RoomEvent() {
            @Override
            public void onSuccess(int roomID) {
                if (roomID == currentRoom.getId()) { //  should be always true

                    currentRoom.setName(roomName);
                    roomAdapter.notifyDataSetChanged();
                    commitRoomsRepo();

                } else toast(languageManager.translate("fail_update_room"));

                dismissProgressDialog();
            }

            @Override
            public void onFail() {
                dismissProgressDialog();
                toast(languageManager.translate("fail_update_room"));
            }
        });
    }

    private void sendNewRomName(final int roomID, final String roomName, final RoomEvent listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    roomRepository.updateRoomName(roomID, roomName);
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onSuccess(roomID);
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                    baseFragmentHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onFail();
                        }
                    });
                }
            }
        });

    }

    private void showDialogItems() {
        final String[] items = new String[]{languageManager.translate("rename"), languageManager.translate("take_picture"), languageManager.translate("select_image")};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle((currentRoom.getName() == null) ? "" : currentRoom.getName());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (!internetConnectionHelper.isOnline()) {
                            toast(languageManager.translate("internet_error"));
                            return;
                        }
                        changeRoomName();
                        break;
                    case 1:
                        dispatchTakePictureIntent();
                        break;
                    case 2:
                        dispatchSelectPictureIntent();
                        break;
                }
            }
        });

        builder.show();
    }

    private void commitRoomsRepo() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    roomRepository.write();
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
            }
        });
    }

    @Override
    protected int getResourceView() {
        return R.layout.fragment_left_content_rooms;
    }

    @Override
    protected void onPostViewCreate() {
        roomAdapter = new RoomAdapter(getContext(), this);
        customRecyclerView.setHasFixedSize(false);
        customRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        customRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), R.drawable.line_separator_empty, DividerItemDecoration.VERTICAL_LIST));
        customRecyclerView.setAdapter(roomAdapter);
    }

    public void onEventMainThread(Event event) {
        if (event.eventType == Event.EVENT_TYPE_LIST_VIEW_REFRESH) {
            final ObjectListRefresh objectListRefresh = (ObjectListRefresh) event.eventObject;
            if (objectListRefresh.fromTo == ObjectItemsClick.ROOMS) {
                Log.d(TAG, "Refreshing filter adapter");
                if (objectListRefresh.reset)
                    roomAdapter.clearSelections();
                roomAdapter.notifyDataSetChanged();
            }
        }
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
        rooms.clear();
    }

    @Override
    public void onClick(int position) {
        roomAdapter.toggleSingleItem(position);
        eventBus.post(new Event(new ObjectItemsClick(ObjectItemsClick.ROOMS, position), Event.EVENT_TYPE_ITEM_CLICK));
    }

    @Override
    public void onLongClick(int position) {
        currentRoom = roomAdapter.getRoom(position);
        showDialogItems();
    }

    public interface RoomEvent {
        void onSuccess(int roomID);

        void onFail();
    }
}
