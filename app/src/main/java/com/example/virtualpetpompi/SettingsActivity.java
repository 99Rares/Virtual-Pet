package com.example.virtualpetpompi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * @author andrei.vasiu & rares.dan
 * - About
 * - Help and Support
 * - Privacy
 * - Credits
 * - show money in main Activity
 */
public class SettingsActivity extends AppCompatActivity {

    // Switches to show their respective panels
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch aboutSwitch, creditsSwitch, privacySwitch, contactSwitch, helpSwitch, stepSwitch;

    // panels
    private CardView aboutPanel, creditsPanel, privacyPanel, contactPanel, helpPanel;

    // buttons
    private FloatingActionButton goBackBtn;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        init();
        openPanel(aboutSwitch, aboutPanel);
        openPanel(creditsSwitch, creditsPanel);
        openPanel(helpSwitch, helpPanel);
        openPanel(privacySwitch, privacyPanel);
        openPanel(contactSwitch, contactPanel);
        setSwitch();
        turnOnSteps();
        goBack();
    }

    /**
     * Initialize the attributes
     */
    private void init() {
        aboutPanel = findViewById(R.id.aboutPanel);
        creditsPanel = findViewById(R.id.creditsPanel);
        privacyPanel = findViewById(R.id.privacyPanel);
        contactPanel = findViewById(R.id.contactPanel);
        helpPanel = findViewById(R.id.helpPanel);

        aboutSwitch = findViewById(R.id.aboutSwitch);
        creditsSwitch = findViewById(R.id.creditsSwitch);
        privacySwitch = findViewById(R.id.privacySwitch);
        contactSwitch = findViewById(R.id.contactSwitch);
        helpSwitch = findViewById(R.id.helpSwitch);
        stepSwitch =findViewById(R.id.stepSwitch);

        goBackBtn = findViewById(R.id.goBackToMainActivity);
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        if(!sharedPreferences.contains("steps")){
            sharedPreferences.edit().putBoolean("steps",false).apply();
        }
    }

    private void setSwitch(){
        stepSwitch.setChecked(sharedPreferences.getBoolean("steps", true));
    }

    private void turnOnSteps(){
        stepSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sharedPreferences.edit().putBoolean("steps",true).apply();
                }else{
                    sharedPreferences.edit().putBoolean("steps",false).apply();
                }
            }
        });
        setSwitch();
    }

    /**
     * A method used in a generic way to set up a switch to open a panel
     *
     * @param switchPanel the switch that opens a panel
     * @param panel       the panel to open
     */
    private void openPanel(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchPanel, CardView panel) {
        switchPanel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    panel.setVisibility(View.VISIBLE);
                } else {
                    panel.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Sets up the go back button
     */
    private void goBack() {
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}