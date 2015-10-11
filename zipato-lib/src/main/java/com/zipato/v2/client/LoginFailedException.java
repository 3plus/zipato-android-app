/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

import org.springframework.web.client.RestClientException;

/**
 * Created by dbudor on 04/12/14.
 */
public class LoginFailedException extends RestClientException {
    public LoginFailedException(String err) {
        super(err);
    }
}
