package com.example.virtualpetpompi;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;
import android.hardware.SensorEvent;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private Button menuBtn;
    private CardView menuPanel;
    private TextView nrSteps;
    private CardView insideMenuPanel;
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
        menuBtn.setOnClickListener(v -> menuPanel.setVisibility(View.VISIBLE));

        menuPanel.setOnClickListener(v -> menuPanel.setVisibility(View.GONE));

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
        editor.putFloat("total",totalSteps);
        editor.putFloat("prev", previousTotalSteps);
        editor.apply();
    }

    private void loadData() {

        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("key1", 0);

        Log.d("MainActivity", "$savedNumber");

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
        menuBtn = (Button) findViewById(R.id.menuBtn);
        menuPanel = (CardView) findViewById(R.id.menuPanel);
        insideMenuPanel = (CardView) findViewById(R.id.insideMenuPanel);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)

    private void onWakeUp(){
        Context context=getApplicationContext();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        PendingIntent pi = PendingIntent.getService(context, 0,
                new Intent(context, ResetSteps.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }


}