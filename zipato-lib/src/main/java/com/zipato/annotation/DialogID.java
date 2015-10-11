/*
 * Copyright (c) 2015 Tri plus d.o.o. All right reserved.
 */

package com.zipato.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.zipato.appv2.activities.ShowDialogActivity.SHOW_ICON_COLOR_ID;

/**
 * Created by Triplus3 on 10/9/2015.
 */

@Retention(RetentionPolicy.CLASS)
@IntDef(SHOW_ICON_COLOR_ID)
public @interface DialogID {
}
