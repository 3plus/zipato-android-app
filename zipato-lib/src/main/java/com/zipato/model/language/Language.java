/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.model.language;

import com.zipato.model.DynaObject;

import java.util.Date;

/**
 * Created by murielK on 17.6.2014..
 */
public class Language extends DynaObject {

    private String code;
    private String language;
    private Date modified;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Language language = (Language) o;

        if (!code.equals(language.code)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}


