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
    private SharedPreferences resetRecover;

    public CoinRepository(Context context) {
        coinsSharedPrefs = context.getSharedPreferences("coins", Context.MODE_PRIVATE);
        stepsSharedPrefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        resetRecover = context.getSharedPreferences("recover", Context.MODE_PRIVATE);
        initData();
        calculateCoins();
    }

    /**
     * Init the repo index
     */
    private void initData() {
        if (!coinsSharedPrefs.contains("totalCoins")) {
            coinsSharedPrefs.edit().putInt("totalCoins", 0).apply();
        }
        if (!coinsSharedPrefs.contains("spentCoins")) {
            coinsSharedPrefs.edit().putInt("spentCoins", 0).apply();
        }
    }

    /**
     * Get total amount of coins
     *
     * @return
     */
    public int getTotalCoins() {
        return this.coinsSharedPrefs.getInt("totalCoins", 0);
    }

    /**
     * Sets the step - coin algorithm and calculates the total amount of coins
     */
    private void calculateCoins() {
        int coins = stepsSharedPrefs.getInt("total", 0);
        int recoveredCoins = resetRecover.getInt("prevCoins", 0);
        coins = coins / 100; // ----------------------------------------------- COIN ALGORITHM
        coins += recoveredCoins;
        coins -= coinsSharedPrefs.getInt("spentCoins", 0);
        if (coins <= 0) {
            coins = 0;
        }

        coinsSharedPrefs.edit().putInt("totalCoins", coins).apply();
    }

    /**
     * When user buys smth, this is called to remove from the total
     *
     * @param amountToRemove how much to remove
     */
    public void removeAmount(int amountToRemove) {
        coinsSharedPrefs.edit().putInt("spentCoins",
                coinsSharedPrefs.getInt("spentCoins", 0) + amountToRemove).apply();
        calculateCoins();
    }
}
