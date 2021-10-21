package com.cmput301f21t26.habittracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * LoginSignupActivity contains the logic for the page that initially shows up after the
 * splash screen; The page contains a button to login or to signup.
 * Clicking one of them will send the user to the corresponding page.
 */
public class LoginSignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);

    }


}