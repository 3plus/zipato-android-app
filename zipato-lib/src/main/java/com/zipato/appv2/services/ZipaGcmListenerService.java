/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.activities.LogInActivity;
import com.zipato.util.TagFactoryUtils;

/**
 * Created by murielK on 8/28/2015.
 */
public class ZipaGcmListenerService extends GcmListenerService {

    public static final String GCM_ACTION = "GCM_ACTION";
    private static final String TAG = TagFactoryUtils.getTag(ZipaGcmListenerService.class);
    private static final int ON_MS = 500;
    private static final int OFF_MS = 500;

    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {

            // normal downstream message.
            String title = data.getString("subject");
            if ((title == null) || title.isEmpty())
                title = getResources().getString(R.string.app_nameMain);
            String msg = data.getString("message");
            int notificationId = 0;
            try {
                notificationId = Integer.valueOf(data.getString("id"));
            } catch (Exception e) {
                notificationId = (int) System.currentTimeMillis();
            }
            if (msg != null)
                sendNotification(title, msg, notificationId);
            Log.d(TAG, "Received: " + msg);
            sendNotification(title, msg, notificationId);
        }

    }

    private void sendNotification(CharSequence title, CharSequence msg, int notificationID) {
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(this, LogInActivity.class);
        i.setAction(GCM_ACTION);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i
                , PendingIntent.FLAG_UPDATE_CURRENT);
        int drawableRes = (Build.VERSION.SDK_INT < 21) ? R.drawable.ic_launcher_2 : R.drawable.ic_launcher_2; // TODO need to provide here a smaller icon for lollipop
        Builder builder =
                new Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(drawableRes)
                        .setContentTitle(title)
                        .setStyle(new BigTextStyle()
                                .bigText(msg))
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                        .setLights(Color.BLUE, ON_MS, OFF_MS)
                        .setContentText(msg);
        builder.setContentIntent(contentIntent);
        mNotificationManager.notify(notificationID, builder.build());
    }
}
