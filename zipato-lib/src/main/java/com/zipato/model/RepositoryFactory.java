/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

import android.content.Context;

import com.zipato.v2.client.ApiV2RestTemplate;

import de.greenrobot.event.EventBus;

/**
 * Created by dbudor on 11/06/2014.
 */
public interface RepositoryFactory {

    <R extends SimpleRepository> R getRepository(Class<R> clazz);

    ApiV2RestTemplate getRestTemplate();

    void setRestTemplate(ApiV2RestTemplate restTemplate);

    EventBus getEventBus();

    void setEventBus(EventBus eventBus);

    Context getContext();
}
