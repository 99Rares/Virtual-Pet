package com.example.virtualpetpompi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Map;

/**
 * @author andrei.vasiu and rares.dan
 * - Holds backgrounds
 * - lets the user buy backgrounds
 */
public class BackgroundActivity extends AppCompatActivity {

    private FloatingActionButton goBackBtn;

    private TextView coinText;
    private CoinRepository coinRepository;
    private BackgroundRepository backgroundRepository;
    private int coins;

    private CardView bg0, bg1, bg2, bg3;
    private TextView bg0Text, bg1Text, bg2Text, bg3Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);

        init();
        displayCoins();
        displayBoughtBackgrounds();
        buyBackground(bg0, "bg0", 0);
        buyBackground(bg1, "bg1", 1);
        buyBackground(bg2, "bg2", 1);
        buyBackground(bg3, "bg3", 1);
        goBack();
    }

    /**
     * Init data
     */
    private void init() {
        goBackBtn = findViewById(R.id.goBackFromBackgroundToShop);
        coinText = findViewById(R.id.backgorundShopCoinTextView);

        bg0 = findViewById(R.id.bg0);
        bg0Text = findViewById(R.id.bg0Text);

        bg1 = findViewById(R.id.bg1);
        bg1Text = findViewById(R.id.bg1Text);

        bg2 = findViewById(R.id.bg2);
        bg2Text = findViewById(R.id.bg2Text);

        bg3 = findViewById(R.id.bg3);
        bg3Text = findViewById(R.id.bg3Text);

        coinRepository = new CoinRepository(this);
        coins = coinRepository.getTotalCoins();
        backgroundRepository = new BackgroundRepository(this);
    }

    /**
     * Displays current amount of coins to a textView
     */
    private void displayCoins() {
        coinText.setText(String.valueOf(coins));
    }

    @SuppressLint("SetTextI18n")
    private void displayBoughtBackgrounds() {
        for (Map.Entry<String, ?> entry : backgroundRepository.getAll().entrySet()) {
            String value = entry.getValue().toString();
            String key = entry.getKey();

            if (!key.equals("currentBackground")) {
                if (value.equals("bg0")) {
                    bg0Text.setText("owned");
                }
                if (value.equals("bg1")) {
                    bg1Text.setText("owned");
                }
                if (value.equals("bg2")) {
                    bg2Text.setText("owned");
                }
                if (value.equals("bg3")) {
                    bg3Text.setText("owned");
                }
            }
        }

        String currentBackground = backgroundRepository.getCurrentBackground();
        Toast.makeText(BackgroundActivity.this, currentBackground, Toast.LENGTH_SHORT).show();
        if(currentBackground.equals("bg0")){
            bg0Text.setText("selected");
        }
        if(currentBackground.equals("bg1")){
            bg1Text.setText("selected");
        }
        if(currentBackground.equals("bg2")){
            bg2Text.setText("selected");
        }
        if(currentBackground.equals("bg3")){
            bg3Text.setText("selected");
        }
    }

    private void buyBackground(CardView bg, String imageName, int cost) {
        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backgroundRepository.isBought(imageName)) {
                    backgroundRepository.setCurrentBackground(imageName);
                    Toast.makeText(BackgroundActivity.this,
                            "Background selected", Toast.LENGTH_SHORT).show();
                } else {
                    if (coins >= cost) {
                        backgroundRepository.add(imageName);
                        substractAndResetCoins(cost);
                        displayBoughtBackgrounds();
                    } else {
                        Toast.makeText(BackgroundActivity.this,
                                "Not enough money, walk more :)", Toast.LENGTH_SHORT).show();
                    }
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
     * Sets up the button that goes back to the previous activity
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