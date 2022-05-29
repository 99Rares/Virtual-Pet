package com.example.virtualpetpompi.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.virtualpetpompi.BuildConfig;
import com.example.virtualpetpompi.util.Util;

public class StepsService extends Service implements SensorEventListener {

    public final static int NOTIFICATION_ID = 12;
    private final static long MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static int SAVE_OFFSET_STEPS = 5;
    private SensorManager mySensorManager;
    private Sensor myStepDetectorSensor;
    private int totalSteps = 0;
    private DataBase db;
    private boolean running = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences coinsSharedPrefs;
    private SharedPreferences resetRecover;
    private SharedPreferences savedLifes;
    private SharedPreferences oneTimePrefs;

    private SensorManager sensorManager = null;
    private static int steps;
    private static int lastSaveSteps;
    private static long lastSaveTime;

    // Creating a variable  which counts previous total
    // steps and it has also been given the value of 0 float
    private int previousTotalSteps = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        running = true;
        mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        myStepDetectorSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (myStepDetectorSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
        } else {
            mySensorManager.registerListener(this, myStepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void init() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        oneTimePrefs = getSharedPreferences("firstTime", Context.MODE_PRIVATE);
        savedLifes = getSharedPreferences("savedLifes", Context.MODE_PRIVATE);
        coinsSharedPrefs = getSharedPreferences("coins", Context.MODE_PRIVATE);
        resetRecover = getSharedPreferences("recover", Context.MODE_PRIVATE);

        db = DataBase.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int
            startId) {
        return Service.START_STICKY;
    }

    /**
     * Resets the steps
     *
     * @param steps
     * @return
     */
    public int resetSteps(int steps) {
        if (steps < 0) {
            int coins;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("total", totalSteps);
            editor.putInt("prev", totalSteps);
            editor.apply();
            coins = coinsSharedPrefs.getInt("totalCoins", 0);
            coinsSharedPrefs.edit().putInt("spentCoins", 0).apply();
            steps = (totalSteps - sharedPreferences.getInt("prev", 0));
            resetRecover.edit().putInt("prevCoins", coins).apply();
        }
        return steps;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public final void onSensorChanged(SensorEvent event) {
        if (running) {
            steps = (int) event.values[0]; // toti pasii facuti de la ultimul reset
            totalSteps = (int) steps;
            if (!oneTimePrefs.contains("firstTime")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("total", totalSteps);
                editor.putInt("prev", totalSteps);
                editor.apply();
                //nrSteps.setText(String.valueOf(0));
                db.saveCurrentSteps(0);
                oneTimePrefs.edit().putString("firstTime", "true").apply();
            }
            int currentSteps = (totalSteps - sharedPreferences.getInt("prev", 0));
            int currentStepsString = resetSteps(currentSteps);
            sharedPreferences.edit().putInt("total", currentStepsString).apply();
            //nrSteps.setText(String.valueOf(currentStepsString));
            Toast.makeText(this, String.valueOf(currentSteps), Toast.LENGTH_SHORT).show();
            if (event.values[0] > Integer.MAX_VALUE) {
                Log.println(Log.DEBUG, "gg", "probably not a real value: " + event.values[0]);
            } else {
                steps = (int) event.values[0];
                updateIfNecessary();
//                Toast.makeText(this, String.valueOf(steps), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (BuildConfig.DEBUG)
            Log.println(Log.DEBUG, "gg", "sensor service task removed");
        // Restart service in 500 ms
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
                        .getService(this, 3, new Intent(this, SensorListener.class), 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG)
            Log.println(Log.DEBUG, "gg", "SensorListener onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.println(Log.DEBUG, "gg", String.valueOf(e));
            e.printStackTrace();
        }
    }


    /**
     *
     */
    private void updateIfNecessary() {
        if (steps > lastSaveSteps + SAVE_OFFSET_STEPS ||
                (steps > 0 && System.currentTimeMillis() > lastSaveTime + SAVE_OFFSET_TIME)) {

            if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
                int pauseDifference = steps - sharedPreferences.getInt("pauseCount", steps);
                int currentSteps = steps - pauseDifference;
                db.insertNewDay(Util.getToday(), currentSteps);
                if (pauseDifference > 0) {
                    // update pauseCount for the new day
                    sharedPreferences.edit().putInt("pauseCount", steps).apply();
                }
            }
            if (db.getCurrentSteps() + db.getSteps(Util.getToday()) < 0) {
                // no values for today
                // we dont know when the reboot was, so set todays steps to 0 by
                // initializing them with -STEPS_SINCE_BOOT
                db.removeNegativeEntries();
                db.insertNewDay(Util.getToday(), (int) steps);
            }
            db.saveCurrentSteps(steps);
            db.close();
            lastSaveSteps = steps;
            lastSaveTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
