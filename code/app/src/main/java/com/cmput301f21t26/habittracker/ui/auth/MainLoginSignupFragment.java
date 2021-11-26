package com.cmput301f21t26.habittracker.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.databinding.FragmentMainLoginSignupBinding;

/**
 * MainLoginSignupFragment directs the user to either sign up for a new account
 * or log in to an existing account.
 */
public class MainLoginSignupFragment extends Fragment {

    private NavController navController;
    private FragmentMainLoginSignupBinding binding;

    private final View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            NavDirections direction = MainLoginSignupFragmentDirections.actionMainLoginSignupFragmentToLoginFragment();
            navController.navigate(direction);
        }
    };

    private final View.OnClickListener signUpOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            NavDirections direction = MainLoginSignupFragmentDirections.actionMainLoginSignupFragmentToSignupFragment();
            navController.navigate(direction);
        }
    };

    public MainLoginSignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMainLoginSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Set the on click listeners for the buttons
        binding.loginButton.setOnClickListener(loginOnClickListener);
        binding.signUpButton.setOnClickListener(signUpOnClickListener);
    }
}