package com.cmput301f21t26.habittracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

/**
 * File contains the logic for the splash screen.
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   // Make screen fullscreen so it looks cleaner
        setContentView(R.layout.activity_splash_screen);

        // Delays the splash screen and then goes to the main activity with intent.
        // ** Should go to login/signup page, will change once that is created **
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_TIME);

    }
}