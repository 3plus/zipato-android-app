/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.util;

import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by murielK on 8/27/2015.
 */
public final class ReflectionUtils {

    public static final FieldFilter TEXT_BUTTON_EDIT_VIEW_FILTER = new FieldFilter() {
        @Override
        public boolean matches(Field field) {
            return TextView.class.isAssignableFrom(field.getType())
                    || EditText.class.isAssignableFrom(field.getType())
                    || Button.class.isAssignableFrom(field.getType());
        }
    };

    private ReflectionUtils() {
    }

    public static void getAllFiledFor(Class<?> clazz, FieldCallback fcb) {
        getAllFieldFor(clazz, fcb, null);
    }


    public static void getAllFieldFor(Class<?> clazz, FieldCallback fcb, FieldFilter ff) {
        Class<?> targetClass = clazz;

        do {
            Field[] fields = targetClass.getDeclaredFields();
            for (Field field : fields) {
                if ((ff != null) && !ff.matches(field)) {
                    continue;
                }
                try {
                    fcb.onField(field);
                } catch (Exception e) {
                    Log.d(TagFactoryUtils.getTag(ReflectionUtils.class), String.format("something went wrong with field: %s", (field == null) ? "null" : field.getName()));
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        while ((targetClass != null) && (targetClass != Object.class));
    }

    public interface FieldCallback {
        void onField(Field field) throws IllegalAccessException;
    }

    public interface FieldFilter {
        boolean matches(Field field);
    }
}
