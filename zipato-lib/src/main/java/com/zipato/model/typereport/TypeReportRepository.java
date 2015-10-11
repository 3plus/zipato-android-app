/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.typereport;

import com.zipato.model.SimpleRepository;

/**
 * Created by dbudor on 03/06/2014.
 */
public class TypeReportRepository extends SimpleRepository<TypeReportKey, TypeReportItem> {

    public static boolean isTypeIsFavorite(TypeReportItem item, int userID) {
        if (item == null) return false;

        String[] tags = item.getTags();
        if ((tags == null) || (tags.length == 0))
            return false;
        final String favoriteUser = "favorite-" + userID;
        for (String tag : tags) {
            if (favoriteUser.equals(tag)) return true;
        }

        return false;
    }

    public void searchAll() {
        TypeReportItem[] items = factory.getRestTemplate().getForObject("v2/types/search/all?x=location,room,master,templateId,endpointType", TypeReportItem[].class);
        clear();

        addAll(items);
    }

    public void searchRoom(int room) {
        TypeReportItem[] items = factory.getRestTemplate().getForObject("v2/types/search/rooms/{room}", TypeReportItem[].class,
                room);
        clear();
        addAll(items);
    }

    @Override
    public TypeReportItem add(TypeReportItem item) {
        return put(item.getKey(), item);
    }

    @Override
    public void addAll(TypeReportItem[] typeReportItems) {
        for (TypeReportItem typeReportItem : typeReportItems) {
            typeReportItem.setUpAttributes();
            add(typeReportItem);
        }
    }
}
