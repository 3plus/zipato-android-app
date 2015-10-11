/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.zipato.util.Utils.checkMain;

/**
 * Created by murielK on 8/14/2015.
 */
public final class TypeFaceUtils {

    private static final String TAG = TagFactoryUtils.getTag(TypeFaceUtils.class);
    private final Map<String, Typeface> map;
    private final Context context;

    public TypeFaceUtils(Context context) {
        this.context = context;
        map = new HashMap<>();
    }

    public Typeface getTypeFace(String path) {
        checkMain();

        if (!map.containsKey(path)) {
            try {
                final Typeface typeface = Typeface.createFromAsset(context.getApplicationContext().getAssets(), path);
                map.put(path, typeface);
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        }
        Log.d(TAG, String.format("found typeface : %s cached...returning cached value", path));
        return map.get(path);
    }

    public void clear() {
        map.clear();
    }

    public void applyTypefaceFor(final Object obj) {
        ReflectionUtils.getAllFieldFor(obj.getClass(), new FieldCallback() {
            @Override
            public void onField(Field field) throws IllegalAccessException {
                applyTypeface(field);
            }

            private void applyTypeface(Field field) throws IllegalAccessException {
                SetTypeFace setTypeFace = field.getAnnotation(SetTypeFace.class);
                if (setTypeFace == null)
                    return;
                final String path = setTypeFace.value();
                if ((path == null) || path.isEmpty())
                    return;
                Typeface typeface = getTypeFace(path);
                if (typeface == null)
                    return;
                field.setAccessible(true);
                TextView tv = (TextView) field.get(obj);
                if (!typeface.equals(tv.getTypeface()))
                    tv.setTypeface(typeface);
            }
        }, ReflectionUtils.TEXT_BUTTON_EDIT_VIEW_FILTER);

    }
}
