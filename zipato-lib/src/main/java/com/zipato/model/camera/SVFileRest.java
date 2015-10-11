/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.camera;

import com.zipato.model.DynaObject;

import java.util.Date;

/**
 * Created by murielK on 9/8/2014.
 */
public class SVFileRest extends DynaObject {

    private String id;
    private long fileSize;
    private String thumbnailUrl;
    private String contentType;
    private Date created;
    private FileType fileType;
    private TriggerType triggerType;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SVFileRest)) return false;

        SVFileRest that = (SVFileRest) o;

        if (fileSize != that.fileSize) return false;
        if (contentType != null ? !contentType.equals(that.contentType) : that.contentType != null)
            return false;
        if (created != null ? !created.equals(that.created) : that.created != null) return false;
        if (fileType != that.fileType) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (int) (fileSize ^ (fileSize >>> 32));
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (created != null ? created.hashCode() : 0);
        result = 31 * result + (fileType != null ? fileType.hashCode() : 0);
        return result;
    }
}
