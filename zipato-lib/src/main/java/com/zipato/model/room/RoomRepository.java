/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.room;

import com.zipato.model.IDRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by murielK on 9/4/2014.
 */
public class RoomRepository extends IDRepository<Rooms> {

    public static final String ROOM_NAME_UNDEFINED = "Undefined";
    public static final String ROOM_NAME_ALL_ROOM = "All Rooms";
    public static final int ID_UNDEFINED = -1;
    public static final int ID_NO_FILTER = -2;


    public void fetchAll() {
        Rooms[] rooms = factory.getRestTemplate().getForObject("v2/rooms/", Rooms[].class);
        addAll(rooms);
    }

    @Override
    public void addAll(Rooms[] rooms) {
        Rooms undefined = new Rooms();

        undefined.setName(ROOM_NAME_UNDEFINED);
        undefined.setId(ID_UNDEFINED);

        tempUpdate(undefined);

        for (Rooms r : rooms) {
            final Rooms r2 = get(r.getId());
            if (r2 != null) {
                final String uri = r2.getStringUri();
                if (uri != null)
                    r.setStringUri(uri);
            }
        }
        clear();
        add(undefined);
        super.addAll(rooms);
    }

    private void tempUpdate(Rooms rooms) {
        if (get(rooms.getId()) != null) {
            final String uri = get(rooms.getId()).getStringUri();
            if (uri != null)
                rooms.setStringUri(uri);
        }
    }

    public void updateRoomName(int roomID, String newName) {
        final Map<String, String> map = new HashMap<>();
        map.put("name", newName);
        factory.getRestTemplate().put("v2/rooms/{roomID}", map, roomID);
    }


}
