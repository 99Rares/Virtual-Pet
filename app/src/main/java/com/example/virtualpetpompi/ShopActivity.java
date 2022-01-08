package com.example.virtualpetpompi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * @author andrei.vasiu and dan.rares
 * - Handles the activity that leads to the real shops
 * - background button that leads to another activity
 * - food btn that leads to another actitivty
 */
public class ShopActivity extends AppCompatActivity {

    // displays the coins
    private TextView coinTextView;

    // buttons that lead to other activities
    private CardView openFoodShopCardView, openBackgroundsCardView;

    // goes back to main
    private FloatingActionButton goBackBtn;

    // SharedPreferences
    private CoinRepository coinRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        init();
        displayCoins();
        openFoodShop();
        openBackgroundShop();
        goBack();
    }

    /**
     * Initializes data
     */
    private void init() {
        coinTextView = findViewById(R.id.shopCoinTextView);

        openFoodShopCardView = findViewById(R.id.openFoodShopCardView);
        openBackgroundsCardView = findViewById(R.id.openBackgroundShopCardView);

        goBackBtn = findViewById(R.id.goBackFromShopToMain);

        coinRepository = new CoinRepository(this);
    }

    /**
     * Displays the current amount of coins
     */
    private void displayCoins() {
        coinTextView.setText(String.valueOf(coinRepository.getTotalCoins()));
    }

    /**
     * sets up the cardView that opens the food shop
     */
    private void openFoodShop() {
        openFoodShopCardView.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), FoodActivity.class)));
    }

    private void openBackgroundShop() {
        openBackgroundsCardView.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), BackgroundActivity.class)));
    }

    /**
     * Sets up the button that goes back to main activity
     */
    private void goBack() {
        goBackBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }
}