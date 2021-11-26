package com.cmput301f21t26.habittracker.ui;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * File contains the logic for the splash screen.
 */
public class SplashScreenActivity extends AppCompatActivity {

    private static int SPLASH_SCREEN_TIME = 1500;
    private AnimatedVectorDrawableCompat avd;
    private AnimatedVectorDrawable avd2;
    private AnimationDrawable ad;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set layout to be splash screen
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();

        ImageView checkmark = findViewById(R.id.checkMark);
        ImageView gradient = findViewById(R.id.Gradient);

        Drawable drawable = checkmark.getDrawable();

        // Animate checkmark, different AVD for compatibility between devices
        if (drawable instanceof AnimatedVectorDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (drawable instanceof AnimatedVectorDrawable) {
            avd2 = (AnimatedVectorDrawable) drawable;
            avd2.start();
        }

        // Animate gradient
        Drawable gradientDrawable = gradient.getDrawable();
        if (gradientDrawable instanceof AnimationDrawable) {
            ad = (AnimationDrawable) gradientDrawable;
            ad.setEnterFadeDuration(10);
            ad.setExitFadeDuration(500);
            ad.start();
        }

        // TODO we want user information to be retrievable even if offline, which is default setting for
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mAuth.getCurrentUser() != null) {
                // initialize user before moving to MainActivity
                UserController.initCurrentUser(user -> {        // get user data then start activity
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                });

            } else {
                Intent intent = new Intent(this, LoginSignupActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            }
        }, SPLASH_SCREEN_TIME);
    }
}