/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

import android.content.Context;
import android.net.Uri;

import com.squareup.picasso.OkHttpDownloader;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by dbudor on 11/09/2014.
 */
public class CookieOkHttpDownloader extends OkHttpDownloader {

    private final BasicCookieStore cookieStore;

    public CookieOkHttpDownloader(Context context, BasicCookieStore cookieStore) {
        super(context);
        this.cookieStore = cookieStore;
    }


    @Override
    protected HttpURLConnection openConnection(Uri uri) throws IOException {
        HttpURLConnection conn = super.openConnection(uri);
        for (Cookie cookies : cookieStore.getCookies()) {
            if ("JSESSIONID".equalsIgnoreCase(cookies.getName())) {
                conn.setRequestProperty("Cookie", cookies.getName() + "=" + cookies.getValue());
                break;
            }

        }
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        return conn;
    }

}
