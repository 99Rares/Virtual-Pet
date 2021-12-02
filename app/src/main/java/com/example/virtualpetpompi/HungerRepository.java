package com.example.virtualpetpompi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author anrei.vasiu and rares.dan
 */
public class HungerRepository {

    private Context context;

    private SharedPreferences timeSharedPrefs;
    private SharedPreferences hungerSharedPrefs;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public HungerRepository(Context context) {
        this.context = context;
        hungerSharedPrefs = context.getSharedPreferences("hunger", Context.MODE_PRIVATE);
        timeSharedPrefs = context.getSharedPreferences("time", Context.MODE_PRIVATE);
        initData();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void hunger() {
        // Iau timpul vechi si il compar cu timpul curent
        LocalDateTime oldDate = LocalDateTime.parse(timeSharedPrefs.getString("currentDate", "empty"));
        long minutes = ChronoUnit.MINUTES.between(oldDate, LocalDateTime.now());

        //Toast.makeText(context, minutes + ": Minutes", Toast.LENGTH_SHORT).show();

        // Daca exista vreo diferenta, scad din hunger Levelul curent
        if ((int) minutes > 0) {
            calculateHunger((int) minutes);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void calculateHunger(int minutes) {
        int hunger = hungerSharedPrefs.getInt("hungerLevel", 0);
        hunger -= minutes / 30; //------------------------------------------------ HUNGER ALGORITHM
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
}
