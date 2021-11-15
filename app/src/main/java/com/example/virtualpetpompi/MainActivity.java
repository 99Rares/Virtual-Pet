package com.example.virtualpetpompi;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /**
     * Opens the shop
     */
    private Button shopBtn;

    /**
     * opens the settings
     */
    private Button settingsBtn;
    private FloatingActionButton menuBtn;
    private CardView menuPanel;

    private TextView nrSteps;

    private SensorManager sensorManager = null;
    private boolean running = false;
    private int totalSteps = 0;
    private int previousTotalSteps = 0;

    private TextView testText;
    SharedPreferences testPrefs;

    //SharedPrefs
    private SharedPreferences sharedPreferences;
    private SharedPreferences oneTimePrefs;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        requestActivityRecognition();
        resetSteps();
        //onWakeUpAlarm();
        onWakeUpReset();
        openMenuPanel();

        openSettings();
        openShop();

        menuPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuPanel.setVisibility(View.GONE);
            }
        });

    }
    private void initData() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        testText = findViewById(R.id.testText);
        menuBtn = findViewById(R.id.menuBtn);
        shopBtn = (Button) findViewById(R.id.shopBtn);
        settingsBtn = (Button) findViewById(R.id.settingsBtn);
        nrSteps = (TextView) findViewById(R.id.nrSteps);

        menuPanel = (CardView) findViewById(R.id.insideMenuPanel);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        oneTimePrefs = getSharedPreferences("firstTime", Context.MODE_PRIVATE);
    }

    private void firstTimeReset() {
        if (!oneTimePrefs.contains("firstTime")) {
            oneTimePrefs.edit().putString("firstTime", "true").apply();
            previousTotalSteps = totalSteps;
            nrSteps.setText(String.valueOf(0));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("total", totalSteps);
            editor.putInt("prev", totalSteps);
            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();

        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
        //testPrefs = getSharedPreferences("testPrefs",Context.MODE_PRIVATE);
        //testText.setText(testPrefs.getString("test", "empty"));
        //loadData();
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (running) {
            float steps = event.values[0]; // toti pasii facuti de la ultimul reset
            totalSteps = (int) steps;

            if (!oneTimePrefs.contains("firstTime")) {
                oneTimePrefs.edit().putString("firstTime", "true").apply();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("total", totalSteps);
                editor.putInt("prev", totalSteps);
                editor.apply();

                //previousTotalSteps = totalSteps;
                nrSteps.setText(String.valueOf(0));
            }
            int currentSteps = (totalSteps - sharedPreferences.getInt("prev", 0));
            String currentStepsString = String.valueOf(currentSteps);
            nrSteps.setText(currentStepsString);
        }
    }

    public void resetSteps() {
        nrSteps.setOnLongClickListener(v -> {
            Toast.makeText(MainActivity.this, String.valueOf(sharedPreferences.getInt("total", 0)), Toast.LENGTH_SHORT).show();
            testText.setText(String.valueOf(sharedPreferences.getInt("prev", 0)));
            return true;
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        sharedPreferences.edit().putInt("total", totalSteps).apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)

    private void onWakeUpReset() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 41);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        PendingIntent pi = PendingIntent.getService(this, 0,
                new Intent(this, ResetSteps.class), 0); //PendingIntent.FLAG_UPDATE_CURRENT

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), //sau .RTC
                AlarmManager.INTERVAL_DAY, pi);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onWakeUpAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 39);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        PendingIntent pi = PendingIntent.getService(this, 0,
                new Intent(this, Alarm.class), 0); //PendingIntent.FLAG_UPDATE_CURRENT

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }

    private void openMenuPanel() {
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuPanel.setVisibility(View.VISIBLE);
            }
        });
    }

    private void openShop() {
        shopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, ShopActivity.class));
            }
        });
    }

    private void openSettings() {
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    private void requestActivityRecognition() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Toast.makeText(MainActivity.this, "ba", Toast.LENGTH_SHORT).show();
                        running = true;
                        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                        if (stepSensor == null) {
                            Toast.makeText(MainActivity.this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();

                        } else {
                            sensorManager.registerListener(MainActivity.this, stepSensor, SensorManager.SENSOR_DELAY_UI);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Plz allow sensor to run", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

}