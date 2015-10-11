/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

/**
 * Created by murielK on 9/4/2014.
 */
public class IDRepository<T extends IDObject> extends SimpleRepository<Integer, T> {

    public T add(T t) {
        return put(t.getId(), t);

    }

}
