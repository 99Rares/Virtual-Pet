package com.example.virtualpetpompi.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.virtualpetpompi.BuildConfig;
import com.example.virtualpetpompi.R;
import com.example.virtualpetpompi.service.DataBase;
import com.example.virtualpetpompi.util.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity implements SensorEventListener {

    final static int DEFAULT_GOAL = 10000;
    @SuppressLint("ConstantLocale")
    final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    @SuppressLint("ConstantLocale")
    final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

    private TextView stepsView, totalView, averageView;
    private PieModel sliceGoal, sliceCurrent;
    private PieChart pg;

    private int todayOffset, total_start, goal, since_boot, total_days;
    @SuppressLint("ConstantLocale")
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
    private boolean showSteps = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        stepsView = findViewById(R.id.steps);
        totalView = findViewById(R.id.total);
        averageView = findViewById(R.id.average);

        pg = findViewById(R.id.graph);

        // slice for the steps taken today
        sliceCurrent = new PieModel("", 0, Color.parseColor("#99CC00"));
        pg.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        sliceGoal = new PieModel("", DEFAULT_GOAL, Color.parseColor("#CC0000"));
        pg.addPieSlice(sliceGoal);

        pg.setOnClickListener(view -> {
            showSteps = !showSteps;
            stepsDistanceChanged();
        });

        pg.setDrawValueInPie(false);
        pg.setUsePieRotation(true);
        pg.startAnimation();

        getRecord();
        goBack();
    }

    @Override
    public void onResume() {
        super.onResume();

        DataBase db = DataBase.getInstance(this);

        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        goal = prefs.getInt("goal", DEFAULT_GOAL);
        since_boot = db.getCurrentSteps();
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) {
            new AlertDialog.Builder(this).setTitle("No Sensor")
                    .setMessage(R.string.no_sensor_explain)
                    .setOnDismissListener(dialogInterface -> finish()).setNeutralButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss()).create().show();
        } else {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();
        saveUserAchievement();
        stepsDistanceChanged();
    }

    private void stepsDistanceChanged() {
        if (showSteps) {
            ((TextView) findViewById(R.id.unit)).setText(getString(R.string.steps));
        } else {
            String unit = getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                    .getString("stepsize_unit", DEFAULT_STEP_UNIT);
            if (unit.equals("cm")) {
                unit = "km";
            } else {
                unit = "mi";
            }
            ((TextView) findViewById(R.id.unit)).setText(unit);
        }

        updatePie();
        updateBars();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.println(Log.DEBUG, "gg", String.valueOf(e));
        }
        DataBase db = DataBase.getInstance(this);
        db.saveCurrentSteps(since_boot);
        db.close();
    }

    private void updateBars() {
        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        BarChart barChart = findViewById(R.id.bargraph);
        if (barChart.getData().size() > 0) barChart.clearChart();
        int steps;
        float distance, stepsize = DEFAULT_STEP_SIZE;
        boolean stepsize_cm = true;
        if (!showSteps) {
            // load some more settings if distance is needed
            SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            stepsize = prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE);
            stepsize_cm = prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)
                    .equals("cm");
        }
        barChart.setShowDecimal(!showSteps); // show decimal in distance view only
        BarModel bm;
        DataBase db = DataBase.getInstance(this);
        List<Pair<Long, Integer>> last = db.getLastEntries(8);
        db.close();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            if (steps > 0) {
                bm = new BarModel(df.format(new Date(current.first)), 0,
                        steps > goal ? Color.parseColor("#CCAD00") : Color.parseColor("#4789fc"));
                if (showSteps) {
                    bm.setValue(steps);
                } else {
                    distance = steps * stepsize;
                    if (stepsize_cm) {
                        distance /= 100000;
                    } else {
                        distance /= 5280;
                    }
                    distance = Math.round(distance * 1000) / 1000f; // 3 decimals
                    bm.setValue(distance);
                    bm.setColor(Color.parseColor("#99CC00"));
                }
                barChart.addBar(bm);
            }
        }
        if (barChart.getData().size() > 0) {
            barChart.startAnimation();
        } else {
            barChart.setVisibility(View.GONE);
        }
    }

    private void updatePie() {
        if (BuildConfig.DEBUG) Log.println(Log.DEBUG, "gg", "UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        int steps_today = Math.max(todayOffset + since_boot, 0);
        sliceCurrent.setValue(steps_today);
        if (goal - steps_today > 0) {
            // goal not reached yet
            if (pg.getData().size() == 1) {
                // can happen if the goal value was changed: old goal value was
                // reached but now there are some steps missing for the new goal
                pg.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(goal - steps_today);
        } else {
            // goal reached
            pg.clearChart();
            pg.addPieSlice(sliceCurrent);
        }
        pg.update();
        if (showSteps) {
            stepsView.setText(formatter.format(steps_today));
            totalView.setText(formatter.format(total_start + steps_today));
            averageView.setText(formatter.format((total_start + steps_today) / total_days));
        } else {
            // update only every 10 steps when displaying distance
            SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            float stepsize = prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE);
            float distance_today = steps_today * stepsize;
            float distance_total = (total_start + steps_today) * stepsize;
            if (prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)
                    .equals("cm")) {
                distance_today /= 100000;
                distance_total /= 100000;
            } else {
                distance_today /= 5280;
                distance_total /= 5280;
            }
            stepsView.setText(formatter.format(distance_today));
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (BuildConfig.DEBUG)
            Log.println(Log.DEBUG, "gg", "UI - sensorChanged | todayOffset: " +
                    todayOffset + " since boot: " + event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            DataBase db = DataBase.getInstance(this);
            db.insertNewDay(Util.getToday(), (int) event.values[0]);
            db.close();
        }
        since_boot = (int) event.values[0];
        updatePie();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("SetTextI18n")
    private void getRecord() {
        try {
            DataBase db = DataBase.getInstance(this);
            Pair<Date, Integer> record = db.getRecordData();
            if (record.second > 0) {
                ((TextView) findViewById(R.id.recordvalue)).setText(formatter.format(record.second) + " @ "
                        + java.text.DateFormat.getDateInstance().format(record.first));
            } else {
                findViewById(R.id.recordvalue).setVisibility(View.GONE);
                findViewById(R.id.record).setVisibility(View.GONE);
            }
        }catch (Exception e){
            Log.e("error","no record set");
        }


    }

    private void goBack() {
        FloatingActionButton goBackBtn = findViewById(R.id.goBackFromStatisticsToMain);
        goBackBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }
    private void saveUserAchievement(){
        DataBase db = DataBase.getInstance(this);
        if (todayOffset + since_boot>10000){
            db.saveHasAchievement(1,1);
        }
        if (todayOffset + since_boot>20000){
            db.saveHasAchievement(1,2);
        }
        if (todayOffset + since_boot>30000){
            db.saveHasAchievement(1,3);
        }
        if (db.getAchievement()==1){
            findViewById(R.id.achievement).setVisibility(View.VISIBLE);
            findViewById(R.id.achievements).setVisibility(View.VISIBLE);
            findViewById(R.id.tenK).setVisibility(View.VISIBLE);
        }
        if (db.getAchievement()==2){
            findViewById(R.id.achievement).setVisibility(View.VISIBLE);
            findViewById(R.id.achievements).setVisibility(View.VISIBLE);
            findViewById(R.id.tenK).setVisibility(View.VISIBLE);
            findViewById(R.id.twentyK).setVisibility(View.VISIBLE);
        }
        if (db.getAchievement()==3){
            findViewById(R.id.achievement).setVisibility(View.VISIBLE);
            findViewById(R.id.achievements).setVisibility(View.VISIBLE);
            findViewById(R.id.tenK).setVisibility(View.VISIBLE);
            findViewById(R.id.twentyK).setVisibility(View.VISIBLE);
            findViewById(R.id.thirtyK).setVisibility(View.VISIBLE);
        }
        db.close();
    }
}