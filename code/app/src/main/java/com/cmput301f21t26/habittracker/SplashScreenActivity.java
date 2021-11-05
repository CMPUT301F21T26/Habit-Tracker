package com.cmput301f21t26.habittracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * File contains the logic for the splash screen.
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIME = 1500;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set layout to be splash screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   // Make screen fullscreen so it looks cleaner
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();

        // we want user information to be retrievable even if offline, which is default setting for
        // our database



    }

    /**
     * Check if the client has a valid authentication token. If this is the case
     * send them to the Main Activity Page
     * otherwise send them to the Login/Signup page
     * This is in onStart as opposed to onCreate because if we are redirected to the splash screen
     * for any reason, we should execute this code again to get away from the splash screen.
     */
    @Override
    protected void onStart(){
        super.onStart();
        // Delays the splash screen and then goes to either the main activity or login/signup page
        // based on auth tokens
        new Handler().postDelayed(() -> {
            if (mAuth.getCurrentUser() != null) {
                // initialize user before moving to MainActivity
                UserController.initCurrentUser(user -> {        // get user data then start activity
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                });

            } else {
                Intent intent = new Intent(this, LoginSignupActivity.class);
                startActivity(intent);
            }
        }, SPLASH_SCREEN_TIME);
    }
}