/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.files;

import com.zipato.model.DynaObject;

/**
 * Created by murielK on 11/3/2014.
 */
public class ImagesObject extends DynaObject {

    private String fileUrl;
    private String name;

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
