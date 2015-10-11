/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model;

/**
 * Created by dbudor on 10/06/2014.
 */
public interface Parent<T extends BaseObject> {

    T[] getChildren();
}
