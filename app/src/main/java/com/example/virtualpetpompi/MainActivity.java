package com.example.virtualpetpompi;

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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    ImageView stepsPoza;
    // opens the shop
    private Button shopBtn;
    // opens the settings
    private Button settingsBtn;
    // open the menu panel
    private FloatingActionButton menuBtn;
    private boolean menuOpened;
    private CardView menuPanel;
    private TextView nrSteps, noFoodText, hungerTextView, nrLifes;
    // Sensor attributes
    private SensorManager sensorManager = null;
    private boolean running = false;
    private int totalSteps = 0;
    private int previousTotalSteps = 0;

    //SharedPrefs
    private SharedPreferences sharedPreferences;
    private SharedPreferences oneTimePrefs;
    private FoodRepository foodRepository;
    private HungerRepository hungerRepository;
    private SharedPreferences savedLifes;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        requestActivityRecognition();
        //resetSteps();
        //onWakeUpAlarm();
        //onWakeUpReset();
        openMenuPanel();
        openInventory();
        displayOwnedFood();
        displayHunger();
        displayBackground();
        manageLifes();
        openSettings();
        playDanceAnim();
        openShop();
        //Anim
        playWakeUpAnimation();
        playIdleAnimation();

        if (sharedPreferences.getBoolean("steps", false)) {
            nrSteps.setVisibility(View.VISIBLE);
            stepsPoza.setVisibility(View.VISIBLE);
        } else {
            nrSteps.setVisibility(View.GONE);
            stepsPoza.setVisibility(View.GONE);
        }

        menuPanel.setOnClickListener(v -> menuPanel.setVisibility(View.GONE));

    }

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

                Toast.makeText(this, "Reinstall the app", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initializes data
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initData() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

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
        foodRepository = new FoodRepository(this);
        hungerRepository = new HungerRepository(this);
        noFoodText = findViewById(R.id.noFoodText);
        hungerTextView = findViewById(R.id.hungerTextView);

        background = findViewById(R.id.background);
        backgroundRepository = new BackgroundRepository(this);

        petImage = findViewById(R.id.petImage);
        dead = findViewById(R.id.dead);
        dead.setVisibility(View.GONE);

    }

    @SuppressLint("SetTextI18n")
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
     * that when touched is applied to the pets hunger level
     */
    private void displayOwnedFood() {
        if (foodRepository.getAll().size() == 0) {
            noFoodText.setVisibility(View.VISIBLE);
        } else {
            for (Map.Entry<String, ?> entry : foodRepository.getAll().entrySet()) {

                String value = entry.getValue().toString();
                String[] values = value.split("\\|");

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
                text.setText(values[1] + "%");

                // Adding to view
                linearLayout.addView(image);
                linearLayout.addView(text);

                cardView.addView(linearLayout);

                // EATING METHOD!! WHEN TAPPING A FOOD ITEM, IT FEEDS THE PET
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hungerRepository.getHunger() >= 100) {
                            //Toast.makeText(MainActivity.this, "Your pet is full already",
                            //      Toast.LENGTH_SHORT).show();
                            anim.stop();
                            petImage.setBackgroundResource(R.drawable.full_anim);
                            anim = (AnimationDrawable) petImage.getBackground();
                            anim.start();
                            playIdleAnimation();

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
                            anim = (AnimationDrawable) petImage.getBackground();
                            anim.start();
                            playIdleAnimation();
                        }
                    }
                });
                inventoryLayout.addView(cardView);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
//        hungerRepository.hunger();
        //testPrefs = getSharedPreferences("testPrefs",Context.MODE_PRIVATE);
        //testText.setText(testPrefs.getString("test", "empty"));
        //loadData();
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (running) {
            float steps = event.values[0]; // toti pasii facuti de la ultimul reset
            totalSteps = (int) steps;
            if (!oneTimePrefs.contains("firstTime")) {
                savedLifes.edit().putInt("life", 5).apply();
                int startLife = savedLifes.getInt("life", 0);
                String startLifeString = String.valueOf(startLife);
                nrLifes.setText(startLifeString);
                createNotificationChannel();
                createNotification();
                oneTimePrefs.edit().putString("firstTime", "true").apply();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("total", totalSteps);
                editor.putInt("prev", totalSteps);
                editor.apply();

                //previousTotalSteps = totalSteps;
                nrSteps.setText(String.valueOf(0));
            }
            int currentSteps = (totalSteps - sharedPreferences.getInt("prev", 0));
            sharedPreferences.edit().putInt("total", currentSteps).apply();
            int currentStepsString = currentSteps;
            currentStepsString = currentStepsString % 10000;
            nrSteps.setText(String.valueOf(currentStepsString));
        }
    }
/*
    public void resetSteps() {
        nrSteps.setOnLongClickListener(v -> {
            Toast.makeText(MainActivity.this, String.valueOf(sharedPreferences.getInt("total", 0)), Toast.LENGTH_SHORT).show();
            testText.setText(String.valueOf(sharedPreferences.getInt("prev", 0)));
            return true;
        });

    }*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        //sharedPreferences.edit().putInt("total", totalSteps).apply();
    }

/*
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onWakeUpReset() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 41);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        PendingIntent pi = PendingIntent.getService(this, 0,
                new Intent(this, ResetSteps.class), 0); //PendingIntent.FLAG_UPDATE_CURRENT

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), //sau .RTC
                AlarmManager.INTERVAL_DAY, pi);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onWakeUpAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 39);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        PendingIntent pi = PendingIntent.getService(this, 0,
                new Intent(this, Alarm.class), 0); //PendingIntent.FLAG_UPDATE_CURRENT

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }*/

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
     * Requests Permission from user to user Activity Recognition for the step sensor
     */
    private void requestActivityRecognition() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACTIVITY_RECOGNITION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Toast.makeText(MainActivity.this, "ba", Toast.LENGTH_SHORT).show();
                        running = true;
                        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                        if (stepSensor == null) {
                            Toast.makeText(MainActivity.this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();

                        } else {
                            sensorManager.registerListener(MainActivity.this, stepSensor, SensorManager.SENSOR_DELAY_UI);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "Please allow sensor to run", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }

    private void playWakeUpAnimation() {
        petImage.setBackgroundResource(R.drawable.wakeup_anim);
        anim = (AnimationDrawable) petImage.getBackground();
        //anim.addFrame(getDrawable(R.drawable.a1), 110);
        anim.start();
    }

    private void playIdleAnimation() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (hungerRepository.getHunger() >= 50) {
                    petImage.setBackgroundResource(R.drawable.idle_anim);
                } else {
                    petImage.setBackgroundResource(R.drawable.hungry_anim);
                }
                anim = (AnimationDrawable) petImage.getBackground();
                anim.start();
            }
        }, 1300);

    }

    private void playDanceAnim() {
        petImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (hungerRepository.getHunger() >= 50) {
                    anim.stop();
                    petImage.setBackgroundResource(R.drawable.dance_anim);
                    anim = (AnimationDrawable) petImage.getBackground();
                    anim.start();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playIdleAnimation();
                        }
                    }, 200);

                } else {
                    anim.stop();
                    petImage.setBackgroundResource(R.drawable.full_anim);
                    anim = (AnimationDrawable) petImage.getBackground();
                    anim.start();
                    playIdleAnimation();
                }
                return false;
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("hunger", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

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
        //Toast.makeText(MainActivity.this, String.valueOf(calendar.getTimeInMillis()), Toast.LENGTH_SHORT).show();
    }

}