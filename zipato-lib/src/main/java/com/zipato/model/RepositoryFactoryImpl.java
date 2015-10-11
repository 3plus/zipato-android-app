/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import android.content.Context;

import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by dbudor on 17/06/2014.
 */
public class RepositoryFactoryImpl implements RepositoryFactory {

    private final HashMap<Class<?>, SimpleRepository<?, ?>> repositories = new HashMap<Class<?>, SimpleRepository<?, ?>>();
    protected ApiV2RestTemplate restTemplate;
    protected EventBus eventBus;
    private Context context;

    @Override
    public <R extends SimpleRepository> R getRepository(Class<R> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        SimpleRepository<?, ?> repo = repositories.get(clazz);
        if (repo != null) {
            return (R) repo;
        }
        try {
            R instance = clazz.newInstance();
            instance.factory = this;
            repositories.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ApiV2RestTemplate getRestTemplate() {
        return restTemplate;
    }

    @Override
    public void setRestTemplate(ApiV2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
