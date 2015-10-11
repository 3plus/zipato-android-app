/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;

/**
 * Created by dbudor on 03/12/14.
 */
public class StatefulHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
    private final HttpContext httpContext;

    public StatefulHttpComponentsClientHttpRequestFactory(HttpClient httpClient, HttpContext httpContext) {
        super(httpClient);
        this.httpContext = httpContext;
    }

    @Override
    protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
        return this.httpContext;
    }
}
