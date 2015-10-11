/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipato.model.client.RestObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * Created by dbudor on 29/07/2014.
 */
public class JsonErrorHandler extends DefaultResponseErrorHandler {
    private ObjectMapper mapper;

    public JsonErrorHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        Series series = response.getStatusCode().series();
        return series == Series.CLIENT_ERROR || series == Series.SERVER_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();
        MediaType contentType = response.getHeaders().getContentType();
        if ("application".equals(contentType.getType()) && "json".equals(contentType.getSubtype())) {
            RestObject body = getResponseBody(response);
            switch (statusCode.series()) {
                case CLIENT_ERROR:
                    throw new RestObjectClientException(statusCode, response.getStatusText(), body);
                case SERVER_ERROR:
                    throw new RestObjectServerException(statusCode, response.getStatusText(), body);
                default:
                    throw new RestObjectException(statusCode, response.getStatusText(), new RestObject("Unknown status code [" + statusCode + "]"));
            }
        }
        super.handleError(response);
    }

    private RestObject getResponseBody(ClientHttpResponse response) throws IOException {
        return mapper.readValue(response.getBody(), RestObject.class);
    }
}
