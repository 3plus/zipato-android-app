/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

import com.zipato.model.client.RestObject;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Created by dbudor on 29/07/2014.
 */
public class RestObjectException extends HttpStatusCodeException {
    private static final long serialVersionUID = 1L;

    private final RestObject responseBody;

    /**
     * Construct a new instance of {@code RestObjectException} based on a {@link HttpStatus}, status text, and
     * response body content.
     *
     * @param statusCode   the status code
     * @param statusText   the status text
     * @param responseBody the response body content, may be {@code null}
     */
    public RestObjectException(HttpStatus statusCode,
                               String statusText,
                               RestObject responseBody) {
        super(statusCode, statusText, null, null);
        this.responseBody = responseBody;
    }

    public RestObject getResponseBody() {
        return responseBody;
    }
}
