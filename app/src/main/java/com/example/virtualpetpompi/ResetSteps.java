package com.example.virtualpetpompi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

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
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("total", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("prev", savedNumber).apply();
        Log.i("service","service1");
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }
}
