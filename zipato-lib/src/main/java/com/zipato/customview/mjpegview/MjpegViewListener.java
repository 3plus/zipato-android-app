/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

/**
 *
 */
package com.zipato.customview.mjpegview;

/**
 * @author Micho Garcia
 */
public interface MjpegViewListener {

    void success();

    void error();

    void onThreadStart();
}
