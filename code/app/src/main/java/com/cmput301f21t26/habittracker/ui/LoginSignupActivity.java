package com.cmput301f21t26.habittracker.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.cmput301f21t26.habittracker.databinding.ActivityLoginSignupBinding;

/**
 * LoginSignupActivity contains the logic for the page that initially shows up after the
 * splash screen; The page contains a button to login or to signup.
 * Clicking one of them will send the user to the corresponding page.
 */
public class LoginSignupActivity extends AppCompatActivity {

    private ActivityLoginSignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}