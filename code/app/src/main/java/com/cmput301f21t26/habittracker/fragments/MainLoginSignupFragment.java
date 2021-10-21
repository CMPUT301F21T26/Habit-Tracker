package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmput301f21t26.habittracker.R;

public class MainLoginSignupFragment extends Fragment implements View.OnClickListener {

    NavController navController = null;

    public MainLoginSignupFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_login_signup, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Set the on click listeners for the buttons
        view.findViewById(R.id.loginButton).setOnClickListener(this);
        view.findViewById(R.id.signUpButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view != null) {
            // Get the ID of whatever was clicked, switch to corresponding case
            if (view.getId() == R.id.loginButton) {
                navController.navigate(R.id.action_mainLoginSignupFragment_to_loginFragment);
            } else if (view.getId() == R.id.signUpButton) {
                navController.navigate(R.id.action_mainLoginSignupFragment_to_signupFragment);
            }
        }
    }
}