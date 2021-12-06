package com.example.virtualpetpompi;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class BackgroundRepository {

    private SharedPreferences repository;
    private Context context;

    public BackgroundRepository(Context context) {
        this.context = context;
        repository = context.getSharedPreferences("backgrounds", Context.MODE_PRIVATE);

        initFirstBackground();
    }

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

    public boolean isBought(String name){
        for (Map.Entry<String, ?> entry : repository.getAll().entrySet()){
            if(entry.getValue().toString().equals(name)){
                return true;
            }
        }
        return false;
    }

    public String getCurrentBackground() {
        return repository.getString("currentBackground", "bg0");
    }

    public void setCurrentBackground(String bg) {
        repository.edit().putString("currentBackground", bg).apply();
    }

}
