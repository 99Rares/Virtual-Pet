package com.rares.virtualpetpompi.repository;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class BackgroundRepository {

    private SharedPreferences repository;

    public BackgroundRepository(Context context) {
        repository = context.getSharedPreferences("backgrounds", Context.MODE_PRIVATE);

        initFirstBackground();
    }

    /**
     * Initialises the first background as bought and starts the repo index
     */
    private void initFirstBackground() {
        if (!repository.contains("background")) {
            repository.edit().putString("background", "bg0").apply();
        }

        if (!repository.contains("currentBackground")) {
            repository.edit().putString("currentBackground", "bg0").apply();
        }
    }
    /**
     * @return all repository values
     */
    public Map<String, ?> getAll() {
        return repository.getAll();
    }

    /**
     * Adds a value to the repo
     *
     * @param value value to add
     */
    public void add(String value) {
        String key = "background" + repository.getAll().size();
        repository.edit().putString(key, value).apply();
    }

    /**
     * Checks if a background is already bought
     * @param name the name of the bg to check
     * @return
     */
    public boolean isBought(String name){
        for (Map.Entry<String, ?> entry : repository.getAll().entrySet()){
            if(entry.getValue().toString().equals(name)){
                return true;
            }
        }
        return false;
    }

    /**
     * Gets current selected backgrounds
     * @return a string with background name
     */
    public String getCurrentBackground() {
        return repository.getString("currentBackground", "bg0");
    }

    /**
     * Sets the current background
     * @param bg the name of the bg to select
     */
    public void setCurrentBackground(String bg) {
        repository.edit().putString("currentBackground", bg).apply();
    }

}
