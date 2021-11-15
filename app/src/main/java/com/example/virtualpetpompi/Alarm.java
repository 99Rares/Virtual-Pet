package com.example.virtualpetpompi;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class Alarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("testPrefs", Context.MODE_PRIVATE);
        Toast.makeText(context.getApplicationContext(), "Mue", Toast.LENGTH_SHORT).show();
        sharedPrefs.edit().putString("test", "bine ba").apply();
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int savedNumber = sharedPreferences.getInt("total", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("prev", savedNumber).apply();
        Log.i("service", "service1");
    }
}
