package com.rares.virtualpetpompi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.rares.virtualpetpompi.R;
import com.rares.virtualpetpompi.model.Food;
import com.rares.virtualpetpompi.repository.CoinRepository;
import com.rares.virtualpetpompi.repository.FoodRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * @author rares.dan
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
    private CardView rice, cirese, clatitecucafea, englishbreakfast, fruits1, cafea;
    private CardView steak1, carbonara, salad, soup, steak2;
    private CardView cartofipai, burger, hotdog, nuggets, gogosi, cola, onionrings, pizza;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        init();
        displayCoins();

        // Set up the food
        setUpFooD();

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
        // Breakfast
        rice = findViewById(R.id.rice);
        cirese = findViewById(R.id.cirese);
        clatitecucafea = findViewById(R.id.clatitecucafea);
        englishbreakfast = findViewById(R.id.englishbreakfast);
        fruits1 = findViewById(R.id.fruits1);
        cafea = findViewById(R.id.cafea);

        //Dinner
        carbonara = findViewById(R.id.carbonara);
        steak1 = findViewById(R.id.steak1);
        salad = findViewById(R.id.salad);
        soup = findViewById(R.id.soup);
        steak2 = findViewById(R.id.steak2);

        // Junk burger, hotdog, nuggets, gogosi, cola, onionrings, pizza
        cartofipai = findViewById(R.id.cartofipai);
        burger = findViewById(R.id.burger);
        hotdog = findViewById(R.id.hotdog);
        nuggets = findViewById(R.id.nuggets);
        gogosi = findViewById(R.id.gogosi);
        onionrings = findViewById(R.id.onionrings);
        pizza = findViewById(R.id.pizza);
        cola = findViewById(R.id.cola);
    }

    /**
     * Displays current amount of coins to a textView
     */
    private void displayCoins() {
        coinTextView.setText(String.valueOf(coins));
    }

    /**
     * Use this to define a new food item
     * Method made so that is can define any kind of food
     *
     * @param foodCardView the existing food cardview
     * @param food         properties of the food
     */
    private void buyFood(CardView foodCardView, Food food) {
        foodCardView.setOnClickListener(v -> {
            if (coins >= food.getPrice()) {
                StringBuilder foodItem = new StringBuilder();
                foodItem.append(food.getFotoName()).append("|");
                foodItem.append(food.getFullness()).append("|");
                foodItem.append(food.getPrice()).append("|");
                foodRepository.add(foodItem.toString());

                subtractAndResetCoins(food.getPrice());

                Toast.makeText(FoodActivity.this, "Food bought!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FoodActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set up the food
     */
    private void setUpFooD() {
        buyFood(rice, new Food("rice", 5, 5));
        buyFood(cirese, new Food("cirese", 1, 2));//"cirese", 1, 2
        buyFood(clatitecucafea, new Food("clatitecucafea", 20, 16)); //16 "clatitecucafea", 28, 16
        buyFood(englishbreakfast, new Food("englishbreakfast", 26, 16)); //"englishbreakfast", 26, 16
        buyFood(fruits1, new Food("fruits1", 15, 13)); //13 "fruits1", 15, 13
        buyFood(cafea, new Food("cafea", 4, 4)); //4 "cafea", 4, 4

        buyFood(carbonara, new Food("carbonara", 15, 25)); //25 "carbonara", 15, 25
        buyFood(salad, new Food("salad", 8, 15)); //15 "salad", 8, 15
        buyFood(soup, new Food("soup", 10, 12)); //12
        buyFood(steak1, new Food("steak1", 30, 35)); //35
        buyFood(steak2, new Food("steak2", 20, 22)); //22

        buyFood(cartofipai, new Food("cartofipai", 7, 10)); //10
        buyFood(burger, new Food("burger", 20, 27)); //27
        buyFood(cola, new Food("cola", 4, 3)); //3
        buyFood(hotdog, new Food("hotdog", 16, 20)); //20
        buyFood(nuggets, new Food("nuggets", 10, 10)); //10
        buyFood(onionrings, new Food("onionrings", 9, 8)); //8
        buyFood(gogosi, new Food("gogosi", 12, 10)); //10
        buyFood(pizza, new Food("pizza", 10, 9)); //9
    }

    /**
     * After an item is bought, this refreshes amount of coins
     *
     * @param cost the cost of an object
     */
    private void subtractAndResetCoins(int cost) {
        coinRepository.removeAmount(cost);
        coins = coinRepository.getTotalCoins();
        displayCoins();
    }

    /**
     * Sets up the button that goes back to the shop menu
     */
    private void goBack() {
        goBackBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ShopActivity.class)));
    }
}