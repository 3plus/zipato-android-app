/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

/**
 * Created by murielK on 8/13/2014.
 */
public abstract class NameObjectRepository<T extends BaseObject> extends SimpleRepository<String, T> {

    public T add(T t) {
        return put(t.getName(), t);
    }

}
