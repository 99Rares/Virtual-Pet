package com.example.virtualpetpompi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * @author andrei.vasiu and rares.dan
 * - contains food types
 * - user can buy food from here!
 */
public class FoodActivity extends AppCompatActivity {

    private TextView coinTextView;
    private FloatingActionButton goBackBtn;

    private CoinRepository coinRepository;
    private int coins;
    private FoodRepository foodRepository;

    /**
     * Food
     */
    private CardView rice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        init();
        displayCoins();
        buyFood(rice, "food1", 5, 5);
        goBack();
    }

    /**
     * Initialize data
     */
    private void init() {
        coinTextView = findViewById(R.id.foodShopCoinTextView);
        goBackBtn = findViewById(R.id.goBackFromFoodToShop);

        coinRepository = new CoinRepository(this);
        coins = coinRepository.getTotalCoins();
        foodRepository = new FoodRepository(this);

        // Food
        rice = findViewById(R.id.rice);
    }

    /**
     * Displays current amount of coins to a textView
     */
    private void displayCoins() {
        coinTextView.setText(String.valueOf(coins));
    }

    /**
     * Use this to define a new food item
     *
     * @param foodCardView the existing food cardview
     * @param imageName    the image name, so that it can be seen in inventory
     * @param hunger       how much it helps with the hunger
     * @param cost         how much it costs
     */
    private void buyFood(CardView foodCardView, String imageName, int hunger, int cost) {
        foodCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins >= cost) {
                    StringBuilder foodItem = new StringBuilder();
                    foodItem.append(imageName).append("|");
                    foodItem.append(hunger).append("|");
                    foodItem.append(cost).append("|");
                    foodRepository.add(foodItem.toString());

                    substractAndResetCoins(cost);

                    Toast.makeText(FoodActivity.this, "Food bought!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FoodActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * After an item is bought, this refreshes amount of coins
     *
     * @param cost
     */
    private void substractAndResetCoins(int cost) {
        coinRepository.removeAmount(cost);
        coins = coinRepository.getTotalCoins();
        displayCoins();
    }

    /**
     * Sets up the button that goes back to the shop menu
     */
    private void goBack() {
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ShopActivity.class));
            }
        });
    }
}