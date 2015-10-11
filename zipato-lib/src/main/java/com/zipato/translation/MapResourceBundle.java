/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.translation;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by dbudor on 18/06/2014.
 */
public class MapResourceBundle extends ResourceBundle {

    private final Map<String, String> map;

    public MapResourceBundle(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(map.keySet());
    }

    @Override
    protected Object handleGetObject(String s) {
        return map.get(s);
    }
}
