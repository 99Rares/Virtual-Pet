package com.rares.virtualpetpompi.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author rares.dan
 * - contains the hunger algorithm
 * - since the app is first opened, it sets up the 'getting hungry' method
 * - holds data about hunger
 */
public class HungerRepository {

    private SharedPreferences timeSharedPrefs;
    private SharedPreferences hungerSharedPrefs;

    public HungerRepository(Context context) {
        hungerSharedPrefs = context.getSharedPreferences("hunger", Context.MODE_PRIVATE);
        timeSharedPrefs = context.getSharedPreferences("time", Context.MODE_PRIVATE);
        initData();
    }

    /**
     * Inits the data that is used to save hunger
     */
    private void initData() {
        if (!hungerSharedPrefs.contains("hungerLevel")) {
            hungerSharedPrefs.edit().putInt("hungerLevel", 100).apply();
        }
        // Iau timpul curent si la prima utilizare il salvez
        LocalDateTime currentDate = LocalDateTime.now();
        if (!timeSharedPrefs.contains("currentDate")) {
            timeSharedPrefs.edit().putString("currentDate", currentDate.toString()).apply();
        }
        hunger();
    }

    /**
     * Substract 1% from hunger for every 20 min of inactivity
     */
    public void hunger() {
        // Iau timpul vechi si il compar cu timpul curent
        LocalDateTime oldDate = LocalDateTime.parse(timeSharedPrefs.getString("currentDate", "empty"));
        long minutes = ChronoUnit.MINUTES.between(oldDate, LocalDateTime.now());
//        Toast.makeText(context, ""+minutes, Toast.LENGTH_SHORT).show();

        //Toast.makeText(context, minutes + ": Minutes", Toast.LENGTH_SHORT).show();

        // Daca exista vreo diferenta, scad din hunger Levelul curent
        if ((int) minutes >= 20) {
            calculateHunger((int) minutes);
        }
    }

    private void calculateHunger(int minutes) {
        int hunger = hungerSharedPrefs.getInt("hungerLevel", 0);
        // ca in 48h sa moara de foame, ar trebui sa setam aprox: hunger -= minutes/25
        hunger -= minutes / 20; //------------------------------------------------ HUNGER ALGORITHM
        if (hunger <= 0) {
            hunger = 0;
        }
        hungerSharedPrefs.edit().putInt("hungerLevel", hunger).apply();

        // Reset timer
        timeSharedPrefs.edit().putString("currentDate", LocalDateTime.now().toString()).apply();
    }

    public void feed(int amount) {
        int hunger = hungerSharedPrefs.getInt("hungerLevel", 0);
        hunger += amount;
        if (hunger >= 100) {
            hunger = 100;
        }
        hungerSharedPrefs.edit().putInt("hungerLevel", hunger).apply();
    }

    public int getHunger() {
        return hungerSharedPrefs.getInt("hungerLevel", 0);
    }

    public void deleteHunger(){
        hungerSharedPrefs.edit().clear().apply();
        timeSharedPrefs.edit().clear().apply();
    }
}
