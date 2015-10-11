/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.translation;

import android.widget.TextView;

import com.zipato.annotation.Translated;
import com.zipato.annotation.TranslatedHint;
import com.zipato.util.ReflectionUtils;
import com.zipato.util.ReflectionUtils.FieldCallback;

import java.lang.reflect.Field;

/**
 * Created by dbudor on 25/06/2014.
 */
public final class TranslateField {

    private TranslateField() {

    }

    public static void translate(final LanguageManager languageManager, final Object obj) {

        ReflectionUtils.getAllFieldFor(obj.getClass(), new FieldCallback() {
            @Override
            public void onField(Field field) throws IllegalAccessException {
                doTranslated(field);
                doTranslatedHint(field);
            }

            private void doTranslated(Field field) throws IllegalAccessException {
                Translated translated = field.getAnnotation(Translated.class);
                if (translated == null) {
                    return;
                }
                field.setAccessible(true);
                TextView tv = (TextView) field.get(obj);
                CharSequence key = ((translated.value() != null) && translated.value().isEmpty()) ? tv.getText() : translated.value();
                if ((key == null) || (key.length() == 0)) {
                    return;
                }
                String value = languageManager.translate(key.toString());
                tv.setText(value);
            }

            private void doTranslatedHint(Field field) throws IllegalAccessException {
                TranslatedHint hint = field.getAnnotation(TranslatedHint.class);
                if (hint == null) {
                    return;
                }
                field.setAccessible(true);
                TextView tv = (TextView) field.get(obj);
                CharSequence key = "".equals(hint.value()) ? tv.getHint() : hint.value();
                if ((key == null) || (key.length() == 0)) {
                    return;
                }
                String value = languageManager.translate(key.toString());
                tv.setHint(value);
            }
        }, ReflectionUtils.TEXT_BUTTON_EDIT_VIEW_FILTER);
    }
}
