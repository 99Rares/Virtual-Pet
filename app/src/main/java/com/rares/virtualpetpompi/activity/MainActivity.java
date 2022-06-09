package com.rares.virtualpetpompi.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rares.virtualpetpompi.R;
import com.rares.virtualpetpompi.repository.BackgroundRepository;
import com.rares.virtualpetpompi.repository.CoinRepository;
import com.rares.virtualpetpompi.repository.FoodRepository;
import com.rares.virtualpetpompi.repository.HungerRepository;
import com.rares.virtualpetpompi.service.DataBase;
import com.rares.virtualpetpompi.service.HungerNotification;
import com.rares.virtualpetpompi.service.StepsService;
import com.rares.virtualpetpompi.util.OnSwipeTouchListener;
import com.rares.virtualpetpompi.util.Util;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Calendar;
import java.util.Map;

/**
 * @author dan.rares
 * - Holds repositories
 * - Inventoy of food
 * - Animations
 * - notification channel
 */
public class MainActivity extends AppCompatActivity {

    ImageView stepsPoza;
    // opens the shop
    private Button shopBtn;
    // opens the settings
    private Button settingsBtn;
    // open the menu panel
    private FloatingActionButton menuBtn;
    private boolean menuOpened;
    private CardView menuPanel, difficultyPanel;
    private TextView nrSteps, noFoodText, hungerTextView, nrLifes;
    // Sensor attributes

    private Button easyMode;
    private Button normalMode;
    private Button reset;

    //SharedPrefs
    private SharedPreferences sharedPreferences;
    private SharedPreferences oneTimePrefs;
    private SharedPreferences savedLifes;
    private SharedPreferences resetRecover;
    private SharedPreferences coinsSharedPrefs;
    private SharedPreferences foodSharedPrefs;
    private SharedPreferences backgroundSharedPrefs;
    private FoodRepository foodRepository;
    private HungerRepository hungerRepository;
    private CoinRepository coinRepository;

    // Inventory
    private FloatingActionButton openInventoryBtn;
    private boolean inventoryOpened;
    private CardView inventoryCardView;
    private LinearLayout inventoryLayout;

    // Background
    private ImageView background;
    private BackgroundRepository backgroundRepository;

    // Animations
    private ImageView petImage, dead;
    private AnimationDrawable anim;

    //Database
    private DataBase db;
    int nrClicks = 0;

    ConstraintLayout layout;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        if (!oneTimePrefs.getBoolean("firstTimeSetPermission", false)) {
            requestActivityRecognition();
        }
        startService(new Intent(this, StepsService.class));
        openMenuPanel();
        openInventory();
        displayOwnedFood();
        displayHunger();
        displayBackground();
        setDifficulty();
        manageLifes();
        openSettings();
        playDanceAnim();
        openShop();
        //Anim
        playWakeUpAnimation();
        playIdleAnimation();
        setNotification();
        updateSteps();
        openStatistics();

        if (sharedPreferences.getBoolean("steps", false)) {
            nrSteps.setVisibility(View.VISIBLE);
            stepsPoza.setVisibility(View.VISIBLE);
        } else {
            nrSteps.setVisibility(View.GONE);
            stepsPoza.setVisibility(View.GONE);
        }
        menuPanel.setOnClickListener(v -> menuPanel.setVisibility(View.GONE));

    }

    private void updateSteps() {
        petImage.setOnClickListener(v -> {
            nrClicks++;
            Handler handler = new Handler();
            Runnable runnable = () -> {
                nrClicks = 0;
                Toast.makeText(getApplicationContext(), "Double Click to update the number of Steps", Toast.LENGTH_SHORT).show();
            };
            if (nrClicks == 1) {
                handler.postDelayed(runnable, 500);

            } else if (nrClicks == 2) {
//                Toast.makeText(getApplicationContext(), "Double Click", Toast.LENGTH_SHORT).show();
                nrSteps.setText(String.valueOf(Math.max(db.getCurrentSteps() + db.getSteps(Util.getToday()), 0)));
                nrClicks = 0;
            }
        });
    }

    private void setNotification() {
        if (!oneTimePrefs.getBoolean("firstTimeSetNotification", false)) {
            createNotificationChannel();
            createNotification();
            oneTimePrefs.edit().putBoolean("firstTimeSetNotification", true).apply();
        }
    }

    private void setDifficulty() {
        if (!oneTimePrefs.getBoolean("firstTimeSetDifficulty", false)) {
            getSharedPreferences("firstTime", Context.MODE_PRIVATE).edit()
                    .remove("firstTimeSetNotification").apply();
            sharedPreferences.edit().putBoolean("steps", true).apply();
            difficultyPanel.setVisibility(View.VISIBLE);
            easyMode.setOnClickListener(v -> {
                hungerRepository.deleteHunger();
                foodSharedPrefs.edit().clear().apply();
                backgroundSharedPrefs.edit().clear().apply();
                savedLifes.edit().putInt("life", 1000).apply();
                difficultyPanel.setVisibility(View.GONE);
                int lifes = savedLifes.getInt("life", 0);
                nrLifes.setText(String.valueOf(lifes));
                coinsSharedPrefs.edit()
                        .putInt("divide", 10)
                        .putInt("totalCoins", 0)
                        .putInt("spentCoins", 0).apply();
                resetRecover.edit().putInt("prevCoins", 100).apply();
                displayOwnedFood();
                displayBackground();

            });
            normalMode.setOnClickListener(v -> {
                hungerRepository.deleteHunger();
                foodSharedPrefs.edit().clear().apply();
                backgroundSharedPrefs.edit().clear().apply();
                savedLifes.edit().putInt("life", 5).apply();
                difficultyPanel.setVisibility(View.GONE);
                int lifes = savedLifes.getInt("life", 0);
                nrLifes.setText(String.valueOf(lifes));
                coinsSharedPrefs.edit()
                        .putInt("divide", 100)
                        .putInt("totalCoins", 0)
                        .putInt("spentCoins", 0).apply();
                resetRecover.edit().putInt("prevCoins", 5).apply();
                displayOwnedFood();
                displayBackground();
            });
            oneTimePrefs.edit().putBoolean("firstTimeSetDifficulty", true).apply();
        }
    }

    /**
     * Manages pet's lives
     * - Total amount of lives: 5 or 100
     * - every time hunger reaches 0%, 1 life is substracted
     * - Hunger is rested to 100%
     * - If lives reach 0 => pet dies and app becomes useless, as well as everything the user bought
     */
    private void manageLifes() {
        if (savedLifes.contains("life")) {
            int hunger = hungerRepository.getHunger();
            int lifes = savedLifes.getInt("life", 0);
            if (lifes > 0) {
                if (hunger == 0) {
                    lifes--;
                    savedLifes.edit().putInt("life", lifes).apply();
                    hungerRepository.feed(100);
                    displayHunger();
                }
            }
            String lifeString = String.valueOf(lifes);
            nrLifes.setText(lifeString);
            if (lifes == 0 && hunger == 0) {
                Toast.makeText(this, "Your pet died :(", Toast.LENGTH_SHORT).show();
                menuBtn.setVisibility(View.GONE);
                openInventoryBtn.setVisibility(View.GONE);
                petImage.setVisibility(View.GONE);
                dead.setVisibility(View.VISIBLE);
                reset.setOnClickListener(v -> {
                    deleteEverithing();
                    recreate();
                });
                Toast.makeText(this, "Reset the app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteEverithing() {
        sharedPreferences.edit().clear().apply();
        oneTimePrefs.edit().clear().apply();
        savedLifes.edit().clear().apply();
        resetRecover.edit().clear().apply();
        foodSharedPrefs.edit().clear().apply();
        backgroundSharedPrefs.edit().clear().apply();
        hungerRepository.deleteHunger();
        foodRepository.clearFood();
        coinRepository.clearCoins();
    }

    /**
     * Initializes data
     */
    private void initData() {

        layout = findViewById(R.id.ConstraintLayout);
        db = DataBase.getInstance(this);
        menuBtn = findViewById(R.id.menuBtn);
        shopBtn = findViewById(R.id.shopBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        nrSteps = findViewById(R.id.nrSteps);
        stepsPoza = findViewById(R.id.stepsPoza);
        nrLifes = findViewById(R.id.lives);

        menuPanel = findViewById(R.id.insideMenuPanel);

        openInventoryBtn = findViewById(R.id.openInventoryBtn);
        inventoryCardView = findViewById(R.id.inventoryCardView);
        inventoryLayout = findViewById(R.id.inventoryLayout);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        oneTimePrefs = getSharedPreferences("firstTime", Context.MODE_PRIVATE);
        savedLifes = getSharedPreferences("savedLifes", Context.MODE_PRIVATE);
        resetRecover = getSharedPreferences("recover", Context.MODE_PRIVATE);
        coinsSharedPrefs = getSharedPreferences("coins", Context.MODE_PRIVATE);
        foodSharedPrefs = getSharedPreferences("food", Context.MODE_PRIVATE);
        backgroundSharedPrefs = getSharedPreferences("backgrounds", Context.MODE_PRIVATE);

        foodRepository = new FoodRepository(this);
        hungerRepository = new HungerRepository(this);
        coinRepository = new CoinRepository(this);
        noFoodText = findViewById(R.id.noFoodText);
        hungerTextView = findViewById(R.id.hungerTextView);

        background = findViewById(R.id.background);
        backgroundRepository = new BackgroundRepository(this);

        petImage = findViewById(R.id.petImage);
        dead = findViewById(R.id.dead);
        dead.setVisibility(View.GONE);

        easyMode = findViewById(R.id.easyBtn);
        normalMode = findViewById(R.id.normalBtn);
        difficultyPanel = findViewById(R.id.difficulty);
        reset = findViewById(R.id.button5);

    }


    private void displayHunger() {
        hungerTextView.setText(hungerRepository.getHunger() + "%");
    }

    private void displayBackground() {
        String backgroundName = backgroundRepository.getCurrentBackground();
        int id = this.getResources().
                getIdentifier(backgroundName, "drawable", getPackageName());
        Drawable drawable = getResources().getDrawable(id);
        background.setImageDrawable(drawable);
    }

    /**
     * Iterates the foodRepository and displays in inventory all available food
     * that when touched is applied to the pet's hunger level
     */
    private void displayOwnedFood() {
        if (foodRepository.getAll().size() == 0) {
            noFoodText.setVisibility(View.VISIBLE);
        } else {
            for (Map.Entry<String, ?> entry : foodRepository.getAll().entrySet()) {
                String value = entry.getValue().toString();
                String[] values = value.split("\\|");
                CardView cardView = getCardView(values);
                // EATING METHOD!! WHEN TAPPING A FOOD ITEM, IT FEEDS THE PET
                cardView.setOnClickListener(v -> {
                    if (hungerRepository.getHunger() >= 100) {
                        anim.stop();
                        petImage.setBackgroundResource(R.drawable.full_anim);

                    } else {
                        cardView.setVisibility(View.GONE);
                        foodRepository.remove(entry.getValue().toString());
                        hungerRepository.feed(Integer.parseInt(values[1]));
                        displayHunger();
                        if (foodRepository.getAll().size() == 0) {
                            noFoodText.setVisibility(View.VISIBLE);
                            inventoryCardView.setVisibility(View.GONE);
                            inventoryOpened = false;
                        }
                        anim.stop();
                        petImage.setBackgroundResource(R.drawable.feed_anim);
                    }
                    anim = (AnimationDrawable) petImage.getBackground();
                    anim.start();
                    playIdleAnimation();
                });
                inventoryLayout.addView(cardView);
            }
        }
    }

    @NonNull
    private CardView getCardView(String[] values) {
        // CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams paramsCard = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsCard.leftMargin = 20;
        paramsCard.topMargin = 20;
        paramsCard.bottomMargin = 20;
        cardView.setLayoutParams(paramsCard);
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setContentPadding(5, 5, 5, 5);

        // Layout
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams paramsLayout = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsLayout.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(paramsLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        // Image
        ImageView image = new ImageView(this);
        LinearLayout.LayoutParams paramsImage = new LinearLayout.LayoutParams(
                140,
                140
        );
        image.setLayoutParams(paramsImage);
        int id = this.getResources().
                getIdentifier(values[0], "drawable", getPackageName());
        Drawable drawable = getResources().getDrawable(id);
        image.setImageDrawable(drawable);

        // Text
        TextView text = new TextView(this);
        LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        paramsText.gravity = Gravity.CENTER;
        text.setLayoutParams(paramsText);
        text.setTextSize(16);
        text.setTextColor(Color.BLACK);
        text.setText(String.format("%s%%", values[1]));

        // Adding to view
        linearLayout.addView(image);
        linearLayout.addView(text);

        cardView.addView(linearLayout);
        return cardView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        nrSteps.setText(String.valueOf(Math.max(db.getCurrentSteps() + db.getSteps(Util.getToday()), 0)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        //sharedPreferences.edit().putInt("total", totalSteps).apply();
    }


    /**
     * Opens the menu panel
     */
    private void openMenuPanel() {
        menuBtn.setOnClickListener(v -> {
            if (!menuOpened) {
                menuPanel.setVisibility(View.VISIBLE);
                openInventoryBtn.setEnabled(false);
                menuOpened = true;
            } else {
                menuPanel.setVisibility(View.GONE);
                openInventoryBtn.setEnabled(true);
                menuOpened = false;
            }

        });
    }

    /**
     * Sets up the button that opens the shop
     */
    private void openShop() {
        shopBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ShopActivity.class)));
    }

    /**
     * Sets up the button that opens the settings
     */
    private void openSettings() {
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    /**
     * Sets up the button that opens the Statistics
     */
    @SuppressLint("ClickableViewAccessibility")
    private void openStatistics() {
        findViewById(R.id.linearLayout3).setOnLongClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            return false;
        });
        layout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Toast.makeText(MainActivity.this, "Swipe Right gesture detected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * sets up the button that opens and closes the inventory
     */
    private void openInventory() {
        openInventoryBtn.setOnClickListener(v -> {
            if (!inventoryOpened) {
                inventoryCardView.setVisibility(View.VISIBLE);
                inventoryOpened = true;
            } else {
                inventoryCardView.setVisibility(View.GONE);
                inventoryOpened = false;
            }
        });
    }


    /**
     * Is called on wake up
     */
    private void playWakeUpAnimation() {
        petImage.setBackgroundResource(R.drawable.wakeup_anim);
        anim = (AnimationDrawable) petImage.getBackground();
        //anim.addFrame(getDrawable(R.drawable.a1), 110);
        anim.start();
    }

    /**
     * Switches between sad and happy idle animations
     */
    private void playIdleAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (hungerRepository.getHunger() >= 50) {
                petImage.setBackgroundResource(R.drawable.idle_anim);
            } else {
                petImage.setBackgroundResource(R.drawable.hungry_anim);
            }
            anim = (AnimationDrawable) petImage.getBackground();
            anim.start();
        }, 1300);

    }

    /**
     * Plays the dance animation only when the pet is happy (>= 50% hunger)
     */
    private void playDanceAnim() {
        petImage.setOnLongClickListener(v -> {
            if (hungerRepository.getHunger() >= 50) {
                anim.stop();
                petImage.setBackgroundResource(R.drawable.dance_anim);
                anim = (AnimationDrawable) petImage.getBackground();
                anim.start();

                Handler handler = new Handler();
                handler.postDelayed(this::playIdleAnimation, 200);

            } else {
                anim.stop();
                petImage.setBackgroundResource(R.drawable.full_anim);
                anim = (AnimationDrawable) petImage.getBackground();
                anim.start();
                playIdleAnimation();
            }
            return false;
        });
    }

    /**
     * Creates the channel notification
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Creates the time when a notification should be recieved
     */
    private void createNotification() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);

        Intent intent = new Intent(getApplicationContext(), HungerNotification.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (getApplicationContext(), 666, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        Toast.makeText(MainActivity.this, String.valueOf(calendar.getTimeInMillis()), Toast.LENGTH_SHORT).show();
    }


    /**
     * Requests Permission from user to user Activity Recognition for the step sensor
     */
    private void requestActivityRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            Toast.makeText(MainActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                            oneTimePrefs.edit().putBoolean("firstTimeSetPermission", true).apply();

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(MainActivity.this, "Please allow sensor to run", Toast.LENGTH_SHORT).show();
                            oneTimePrefs.edit().putBoolean("firstTimeSetPermission", false).apply();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    })
                    .check();
        }
    }
}