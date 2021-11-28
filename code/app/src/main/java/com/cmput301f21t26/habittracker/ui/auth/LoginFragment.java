package com.cmput301f21t26.habittracker.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.AuthController;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Contains the logic for the Login Fragment
 * Validates inputs, and logs in the user if their username matches their password
 */
public class LoginFragment extends Fragment {

    final private String TAG = "loginAuthentication";
    private String username;
    private String password;

    private EditText usernameET;
    private EditText passwordET;
    private Button loginConfirmButton;

    private AuthController authController;
    private UserController userController;

    private final View.OnClickListener loginConfirmOnClickListener = view -> {
        username = usernameET.getText().toString();
        password = passwordET.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(getActivity(), "Must enter a username", Toast.LENGTH_LONG).show();
        } else if (password.isEmpty()){
            Toast.makeText(getActivity(), "Must enter a password", Toast.LENGTH_LONG).show();
        } else {
            authController.login(username, password, user -> {

                Toast.makeText(getActivity(), "Logging in", Toast.LENGTH_LONG).show();

                // initialize user before moving to MainActivity
                userController.initCurrentUser(currUser -> {        // get user data then start activity
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        // Close existing activity stack and create new root activity
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

            }, errorMsg -> Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show());
        }
    };

    /**
     * Empty constructor
     */
    public LoginFragment() { }

    /**
     * Initialize the login fragment and get instances
     * @param savedInstanceState a reference to Bundle object
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authController = AuthController.getInstance();
        userController = UserController.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usernameET = view.findViewById(R.id.usernameET);
        passwordET = view.findViewById(R.id.passwordET);
        loginConfirmButton = view.findViewById(R.id.loginConfirmButton);
        loginConfirmButton.setOnClickListener(loginConfirmOnClickListener);
    }
}