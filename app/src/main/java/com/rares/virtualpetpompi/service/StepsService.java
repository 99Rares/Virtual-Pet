package com.rares.virtualpetpompi.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.rares.virtualpetpompi.BuildConfig;
import com.rares.virtualpetpompi.R;
import com.rares.virtualpetpompi.activity.MainActivity;
import com.rares.virtualpetpompi.util.Util;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author dan.rares
 * - Holds step sensor
 * - methods for managing the data
 */
public class StepsService extends Service implements SensorEventListener {

    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static int SAVE_OFFSET_STEPS = 100;
    private static final int MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private int totalSteps = 0;
    private DataBase db;
    private boolean running = false;

    public final static int NOTIFICATION_ID = 1;

    private SharedPreferences sharedPreferences;
    private SharedPreferences coinsSharedPrefs;
    private SharedPreferences resetRecover;
    private SharedPreferences oneTimePrefs;

    private static int steps;
    private static int lastSaveSteps;
    private static long lastSaveTime;

    private final BroadcastReceiver shutdownReceiver = new ShutdownReceiver();

    // Creating a variable  which counts previous total
    // steps and it has also been given the value of 0 float


    @Override
    public void onCreate() {
        super.onCreate();
        init();
        running = true;
        SensorManager mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor myStepDetectorSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (myStepDetectorSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
        } else {
            mySensorManager.registerListener(this, myStepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void init() {

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        oneTimePrefs = getSharedPreferences("firstTime", Context.MODE_PRIVATE);
        coinsSharedPrefs = getSharedPreferences("coins", Context.MODE_PRIVATE);
        resetRecover = getSharedPreferences("recover", Context.MODE_PRIVATE);

        db = DataBase.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int
            startId) {
        reRegisterSensor();
        registerBroadcastReceiver();
        if (!updateIfNecessary()) {
            showNotification();
        }

        // restart service every hour to save the current step count
        long nextUpdate = Math.min(Util.getTomorrow(),
                System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);
        if (BuildConfig.DEBUG) Log.d("gg", "next update: " + new Date(nextUpdate));
        AlarmManager am =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent
                .getService(getApplicationContext(), 2, new Intent(this, StepsService.class),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        am.setAndAllowWhileIdle(AlarmManager.RTC, nextUpdate, pi);
        return START_STICKY;
    }

    private void registerBroadcastReceiver() {
        if (BuildConfig.DEBUG) Log.d("gg", "register broadcast receiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);
    }

    private void reRegisterSensor() {
        if (BuildConfig.DEBUG) Log.d("gg", "re-register sensor listener");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("error", e.toString());
            e.printStackTrace();
        }

        if (BuildConfig.DEBUG) {
            Log.d("gg", "step sensors: " + sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size());
            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator
            Log.d("gg", "default: " + sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName());
        }

        // enable batching with delay of max 5 min
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL, 5 * MICROSECONDS_IN_ONE_MINUTE);
    }

    /**
     * Resets the steps
     *
     * @param steps nr of steps
     * @return steps
     */
    private int resetSteps(int steps) {
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
            steps = (int) event.values[0]; // all the steps taken since the last reset
            totalSteps = steps;
            if (!oneTimePrefs.contains("firstTime")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("total", totalSteps);
                editor.putInt("prev", totalSteps);
                editor.apply();
                db.saveAchievement();
                db.saveCurrentSteps(0);
                oneTimePrefs.edit().putString("firstTime", "true").apply();
            }
            int currentSteps = (totalSteps - sharedPreferences.getInt("prev", 0));
            db.saveUser(currentSteps);
            int currentStepsString = resetSteps(currentSteps);
            sharedPreferences.edit().putInt("total", currentStepsString).apply();
            if (event.values[0] > Integer.MAX_VALUE) {
                Log.println(Log.DEBUG, "gg", "probably not a real value: " + event.values[0]);
            } else {
                steps = (int) event.values[0];
                updateIfNecessary();
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
                        .getService(this, 3, new Intent(this, StepsService.class), 0));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG)
            Log.println(Log.DEBUG, "gg", "StepsService onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.println(Log.DEBUG, "gg", String.valueOf(e));
            e.printStackTrace();
        }
    }


    /**
     * updates steps if necessary
     *
     * @return true, if notification was updated
     */
    private boolean updateIfNecessary() {
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
            db.saveCurrentSteps(steps);
            db.close();
            lastSaveSteps = steps;
            lastSaveTime = System.currentTimeMillis();
            showNotification();
            return true;
        } else {
            return false;
        }
    }

    private void showNotification() {
        createNotificationChannel();
        try {
            startForeground(NOTIFICATION_ID, getNotification(this));
        } catch (Exception e) {
            Log.println(Log.DEBUG, "notification", "steps notification");
        }
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because
     * the NotificationChannel class is new and not in the support library
     */
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name_steps);
        String description = getString(R.string.channel_description_steps);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("steps", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification getNotification(final Context context) {

        Intent notifyIntent = new Intent(context, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                666,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        int goal = prefs.getInt("goal", 10000);
        DataBase db = DataBase.getInstance(context);
        int today_offset = db.getSteps(Util.getToday());
        if (steps == 0)
            steps = db.getCurrentSteps(); // use saved value if we haven't anything better
        db.close();
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "steps");
        if (steps > 0) {
            if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            notificationBuilder.setProgress(goal, today_offset + steps, false).setContentText(
                    today_offset + steps >= goal ?
                            context.getString(R.string.goal_reached_notification,
                                    format.format((today_offset + steps))) :
                            context.getString(R.string.notification_text,
                                    format.format((goal - today_offset - steps)))).setContentTitle(
                    format.format(today_offset + steps) + " " + context.getString(R.string.steps));
        } else { // still no step value?
            notificationBuilder.setContentText(
                            context.getString(R.string.your_progress_will_be_shown_here_soon))
                    .setContentTitle(context.getString(R.string.notification_title));
        }
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT).setShowWhen(false)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.a10)
                .setSilent(true)
                .setColor(Color.BLUE)
                .setOngoing(true);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        return notificationBuilder.build();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
