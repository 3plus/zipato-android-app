/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.files;

import com.zipato.model.DynaObject;

import java.util.Date;

/**
 * Created by murielK on 11/3/2014.
 */
public class FilesObject extends DynaObject {

    private long rotation;
    private Date lastModified;
    private ImagesObject[] images;

    public ImagesObject[] getImages() {
        return images;
    }

    public void setImages(ImagesObject[] images) {
        this.images = images;
    }

    public long getRotation() {
        return rotation;
    }

    public void setRotation(long rotation) {
        this.rotation = rotation;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
