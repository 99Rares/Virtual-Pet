package com.example.virtualpetpompi.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.virtualpetpompi.R;
import com.example.virtualpetpompi.activity.MainActivity;

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
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                666,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "hunger")
                .setSmallIcon(R.drawable.a10)
                .setContentTitle("Check on your pet!")
                .setContentText("Your pet might be hungry go and feed it.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(666, builder.build());
    }
}