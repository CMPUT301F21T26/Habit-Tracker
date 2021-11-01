package com.cmput301f21t26.habittracker.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    private final View.OnClickListener loginConfirmOnClickListener = view -> {
        username = usernameET.getText().toString();
        password = passwordET.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(getActivity(), "Must enter a username", Toast.LENGTH_LONG).show();
        } else if (password.isEmpty()){
            Toast.makeText(getActivity(), "Must enter a password", Toast.LENGTH_LONG).show();
        } else {
            login(username, password);
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

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
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

    public void login(String username, String password){
        // check if user exists
        DocumentReference ref = mStore.collection("users").document(username);
        // if the user exists, pull their email and try to sign them in
        ref.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();

                    assert document != null;
                    if (document.exists()){

                        // username exists, get the email and attempt to login
                        User user = document.toObject(User.class);
                        assert user != null;

                        mAuth.signInWithEmailAndPassword(user.getEmail(), password)
                            .addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()){
                                    Toast.makeText(getActivity(), "Logging in", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    // Close existing activity stack and create new root activity
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(getActivity(), "Wrong password", Toast.LENGTH_LONG).show();
                                }
                            });
                    } else {
                        // username does not exist
                        Toast.makeText(getActivity(), "Invalid username", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "Fetching user info failed: ", task.getException());
                }
            });
    }
}