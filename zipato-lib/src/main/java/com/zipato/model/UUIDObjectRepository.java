/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import java.util.UUID;

/**
 * Created by dbudor on 10/06/2014.
 */
public abstract class UUIDObjectRepository<T extends UUIDObject> extends SimpleRepository<UUID, T> {


    @Override
    public T add(T t) {
        return put(t.getUuid(), t);
    }

}
