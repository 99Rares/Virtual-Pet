package com.example.virtualpetpompi;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author andrei.vasiu and rares.dan
 * - Contains the algorithm to calculate the coins amount
 * - Contains the coins!
 * - remove coins when item bought
 */
public class CoinRepository {

    private SharedPreferences stepsSharedPrefs;
    private SharedPreferences coinsSharedPrefs;

    public CoinRepository(Context context) {
        coinsSharedPrefs = context.getSharedPreferences("coins", Context.MODE_PRIVATE);
        stepsSharedPrefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        initData();

        calculateCoins();
    }

    private void initData() {
        if (!coinsSharedPrefs.contains("totalCoins")) {
            coinsSharedPrefs.edit().putInt("totalCoins", 0).apply();
        }
        if (!coinsSharedPrefs.contains("spentCoins")) {
            coinsSharedPrefs.edit().putInt("spentCoins", 0).apply();
        }
    }

    public int getTotalCoins() {
        return this.coinsSharedPrefs.getInt("totalCoins", 0);
    }

    private void calculateCoins() {
        int coins = stepsSharedPrefs.getInt("total", 0);
        coins /= 10;
        coins -= coinsSharedPrefs.getInt("spentCoins", 0);
        coinsSharedPrefs.edit().putInt("totalCoins", coins).apply();
    }

    public void removeAmount(int amountToRemove) {
        coinsSharedPrefs.edit().putInt("spentCoins",
                coinsSharedPrefs.getInt("spentCoins", 0) + amountToRemove).apply();
        calculateCoins();
    }
}
