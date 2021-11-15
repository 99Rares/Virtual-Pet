package com.example.virtualpetpompi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class ResetSteps extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPrefs = getSharedPreferences("testPrefs", Context.MODE_PRIVATE);
        sharedPrefs.edit().putString("test", "bine ba").apply();
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int savedNumber = sharedPreferences.getInt("total", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("prev", savedNumber).apply();
        Log.i("service", "service1");
        stopService(intent);
        return super.onStartCommand(intent, flags, startId);
    }
}
