package com.example.virtualpetpompi.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.virtualpetpompi.BuildConfig;
import com.example.virtualpetpompi.util.Util;

public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Log.d("gg","shutting down");

        context.startService(new Intent(context, StepsService.class));

        // if the user used a root script for shutdown, the DEVICE_SHUTDOWN
        // broadcast might not be send. Therefore, the app will check this
        // setting on the next boot and displays an error message if it's not
        // set to true
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                .putBoolean("correctShutdown", true).apply();

        DataBase db = DataBase.getInstance(context);
        // if it's already a new day, add the temp. steps to the last one
        if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            db.insertNewDay(Util.getToday(), steps);
        } else {
            db.addToLastEntry(db.getCurrentSteps());
        }
        // current steps will be reset on boot @see BootReceiver
        db.close();
    }

}

