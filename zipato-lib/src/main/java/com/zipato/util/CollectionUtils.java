/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by murielK on 6.6.2014..
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    public static <E> void addMatching(Collection<E> src, Collection<E> dst, Predicate<E> predicate) {
        for (E e : src) {
            if (predicate.apply(e)) {
                dst.add(e);
            }
        }
    }

    public static <E> List<E> filterList(List<E> src, Predicate<E> predicate) {
        List<E> list = new ArrayList<E>();
        addMatching(src, list, predicate);
        return list;
    }


    public interface Predicate<T> {
        boolean apply(T t);
    }
}
