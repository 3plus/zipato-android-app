/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.v2.client;

/**
 * Created by dbudor on 04/12/14.
 */
public interface APIV2RestCallback {

    void loginSuccessful();

    void loginFailed(String error);

    void onGCMRegistered();

    void onGCMUnregistered();
}
