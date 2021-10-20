package com.cmput301f21t26.habittracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

/**
 * LoginActivity contains the logic for the login page.
 * i.e Once details are entered and login button is clicked,
 * checks for the user in Firebase and if the details match
 * and are authenticated, then the user is sent to the
 * main page of the app.
 */
public class LoginActivity extends AppCompatActivity {

    private Button loginConfirmButton;
    private EditText usernameET;
    private EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect our objects to the right views
        loginConfirmButton = findViewById(R.id.loginConfirmButton);
        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
    }

}