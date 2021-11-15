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
    private float totalSteps = 0;
    private float previousTotalSteps = 0;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        loadData();
        resetSteps();
        onWakeUp();
        saveData();
        openMenuPanel();

        openSettings();
        openShop();
        requestActivityRecognition();

        menuPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuPanel.setVisibility(View.GONE);
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


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
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        nrSteps = (TextView) findViewById(R.id.nrSteps);
        if (running) {
            totalSteps = event.values[0];
            float currentSteps = (totalSteps - previousTotalSteps);
            String currentStepsString = String.valueOf(currentSteps);
            nrSteps.setText(currentStepsString);
        }
    }

    public void resetSteps() {
        nrSteps = (TextView) findViewById(R.id.nrSteps);
        nrSteps.setOnLongClickListener(v -> {
            previousTotalSteps = totalSteps;
            nrSteps.setText(String.valueOf(0.0));
            saveData();
            return true;
        });
    }

    private void saveData() {

        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("total", totalSteps);
        editor.putFloat("prev", previousTotalSteps);
        editor.apply();
    }

    private void loadData() {

        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("prev", 0);

        previousTotalSteps = savedNumber;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    private void initData() {
        menuBtn = findViewById(R.id.menuBtn);
        shopBtn = (Button) findViewById(R.id.shopBtn);
        settingsBtn = (Button) findViewById(R.id.settingsBtn);

        menuPanel = (CardView) findViewById(R.id.insideMenuPanel);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)

    private void onWakeUp() {
        Context context = getApplicationContext();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, ResetSteps.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
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
                        loadData();
                        saveData();
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