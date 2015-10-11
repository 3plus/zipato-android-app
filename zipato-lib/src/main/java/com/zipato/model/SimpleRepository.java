/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import android.content.Context;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dbudor on 10/06/2014.
 */
public abstract class SimpleRepository<K, T> extends ConcurrentHashMap<K, T> {
    private final JavaType type;
    protected RepositoryFactory factory;

    public SimpleRepository() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: no type information");
        }
        /* 22-Dec-2008, tatu: Not sure if this case is safe -- I suspect
         *   it is possible to make it fail?
         *   But let's deal with specific
         *   case when we know an actual use case, and thereby suitable
         *   workarounds for valid case(s) and/or error to throw
         *   on invalid one(s).
         */
        Type[] actualTypeArguments = ((ParameterizedType) superClass).getActualTypeArguments();
        Type type1 = actualTypeArguments[actualTypeArguments.length - 1]; // FIXME: ugly shit!!
        if (!(type1 instanceof Class)) {
            throw new IllegalArgumentException("Internal error... Blah");
        }
        type = TypeFactory.defaultInstance().constructArrayType((Class<?>) type1);
    }

    public void write() throws Exception {
        FileOutputStream fos = factory.getContext().openFileOutput(getFileName(), Context.MODE_PRIVATE);
        ObjectMapper mapper = factory.getRestTemplate().getMapper();
        mapper.writeValue(fos, values());
        fos.flush();
        fos.close();

    }

    public void restore() throws Exception {
        FileInputStream fis = factory.getContext().openFileInput(getFileName());
        ObjectMapper mapper = factory.getRestTemplate().getMapper();
        T[] ts = mapper.readValue(fis, type);
        fis.close();
        clear();
        addAll(ts);
    }

    protected String getFileName() {
        return getClass().getSimpleName();

    }

    public abstract T add(T t);

    public void addAll(T[] ts) {
        for (T t : ts) {
            add(t);
        }
    }

    public void addAll(Iterable<T> ts) {
        for (T t : ts) {
            add(t);
        }
    }

}
