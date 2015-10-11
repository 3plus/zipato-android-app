/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

import com.zipato.model.client.RestObject;

import org.springframework.http.HttpStatus;

/**
 * Created by dbudor on 29/07/2014.
 */
public class RestObjectClientException extends RestObjectException {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new instance of {@code RestObjectException} based on a {@link org.springframework.http.HttpStatus}, status text, and
     * response body content.
     *
     * @param statusCode   the status code
     * @param statusText   the status text
     * @param responseBody the response body content, may be {@code null}
     */
    public RestObjectClientException(HttpStatus statusCode, String statusText, RestObject responseBody) {
        super(statusCode, statusText, responseBody);
    }
}
