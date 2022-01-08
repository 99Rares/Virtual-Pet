package com.example.virtualpetpompi;

import android.content.Intent;
import android.os.Bundle;
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
        buyFood(rice, "food1", 5, 5);
        buyFood(cirese, "cirese", 1, 2);
        buyFood(clatitecucafea, "clatitecucafea", 28, 16); //16
        buyFood(englishbreakfast, "englishbreakfast", 26, 16); //16
        buyFood(fruits1, "fruits1", 15, 13); //13
        buyFood(cafea, "cafea", 4, 4); //4

        buyFood(carbonara, "carbonara", 15, 25); //25
        buyFood(salad, "salad", 8, 15); //15
        buyFood(soup, "soup", 10, 12); //12
        buyFood(steak1, "steak1", 30, 35); //35
        buyFood(steak2, "steak2", 20, 22); //22

        buyFood(cartofipai, "cartofipai", 7, 10); //10
        buyFood(burger, "burger", 20, 27); //27
        buyFood(cola, "cola", 4, 3); //3
        buyFood(hotdog, "hotdog", 16, 20); //20
        buyFood(nuggets, "nuggets", 10, 10); //10
        buyFood(onionrings, "onionrings", 9, 8); //8
        buyFood(gogosi, "gogosi", 12, 10); //10
        buyFood(pizza, "pizza", 10, 9); //9

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
     * @param imageName    the image name, so that it can be seen in inventory
     * @param hunger       how much it helps with the hunger
     * @param cost         how much it costs
     */
    private void buyFood(CardView foodCardView, String imageName, int hunger, int cost) {
        foodCardView.setOnClickListener(v -> {
            if (coins >= cost) {
                StringBuilder foodItem = new StringBuilder();
                foodItem.append(imageName).append("|");
                foodItem.append(hunger).append("|");
                foodItem.append(cost).append("|");
                foodRepository.add(foodItem.toString());

                subtractAndResetCoins(cost);

                Toast.makeText(FoodActivity.this, "Food bought!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FoodActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
            }
        });
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