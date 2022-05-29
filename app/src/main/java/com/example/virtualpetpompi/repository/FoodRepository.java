package com.example.virtualpetpompi.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.virtualpetpompi.model.Food;

import java.util.Map;

/**
 * @author andrei.vasiu and rares.dan
 * - Holds data about the food that the user has bought
 */
public class FoodRepository {

    // actual repository
    private SharedPreferences sharedPreferences;

    // holds current value that is incremented to save values
    private SharedPreferences currentValue;

    private Food food;

    public FoodRepository(Context context) {
        sharedPreferences = context.getSharedPreferences("food", Context.MODE_PRIVATE);
        currentValue = context.getSharedPreferences("currentValue", Context.MODE_PRIVATE);
        initFirstValue();
    }

    /**
     * Sets the current counter to 1
     */
    private void initFirstValue() {
        if (!currentValue.contains("current")) {
            currentValue.edit().putInt("current", 1).apply();
        }
    }

    /**
     * @return all repository values
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    /**
     * Adds a value to the repo
     *
     * @param value value to add
     */
    public void add(String value) {
        String key = "food" + currentValue.getInt("current", 0);
        sharedPreferences.edit().putString(key, value).apply();
        currentValue.edit().putInt("current", currentValue.getInt("current", 0) + 1).apply();
    }

    /**
     * removes a value form the repo
     *
     * @param value value to remove
     */
    public void remove(String value) {
        for (Map.Entry<String, ?> entry : getAll().entrySet()) {
            if (entry.getValue().equals(value)) {
                sharedPreferences.edit().remove(entry.getKey()).apply();
                return;
            }
        }
    }

    /**
     * Changes value in the repository
     *
     * @param oldValue value to search and edit
     * @param newValue new value
     */
    public void edit(String oldValue, String newValue) {
        for (Map.Entry<String, ?> entry : getAll().entrySet()) {
            if (entry.getValue().equals(oldValue)) {
                sharedPreferences.edit().putString(entry.getKey(), newValue).apply();
                return;
            }
        }
    }
}
