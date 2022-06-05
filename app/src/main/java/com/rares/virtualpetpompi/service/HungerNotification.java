package com.rares.virtualpetpompi.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.rares.virtualpetpompi.R;
import com.rares.virtualpetpompi.activity.MainActivity;

/**
 * @author dan.rares
 */
public class HungerNotification extends BroadcastReceiver {
    public HungerNotification() {
    }

    /**
     * Sets up the notification of the app
     *
     * @param context context
     * @param intent  intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                666,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String description = context.getResources().getString(R.string.description_check);
        String text = context.getResources().getString(R.string.description_title);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "hunger")
                .setSmallIcon(R.drawable.a10)
                .setContentTitle(description)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(666, builder.build());
    }
}
