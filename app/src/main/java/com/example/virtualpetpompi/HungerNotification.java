package com.example.virtualpetpompi;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * @author dan.rares and andrei.vasiu
 */
public class HungerNotification extends BroadcastReceiver {
    public HungerNotification() {
    }

    /**
     * Sets up the notification of the app
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent notifyIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 666, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
