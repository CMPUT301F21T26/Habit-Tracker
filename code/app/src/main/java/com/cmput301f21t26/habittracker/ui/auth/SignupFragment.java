package com.cmput301f21t26.habittracker.ui.auth;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.AuthController;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Contains the logic for the Signup Fragment.
 * Validates input and attempts to create account with the entered fields.
 */
public class SignupFragment extends Fragment {

    private final String TAG = "signupAuthentication";

    private EditText firstNameET;
    private EditText lastNameET;
    private EditText emailET;
    private EditText usernameET;
    private EditText passwordET;
    private EditText confirmPassET;
    private TextInputLayout confirmPassLayout;
    private TextInputLayout passwordLayout;
    private Button signupConfirmButton;
    private CircleImageView setProfilePic;

    // Make it so the default profile pic is...the default profile pic. So user doesn't have to necessarily choose one.
    private Uri imageUri = Uri.parse("android.resource://com.cmput301f21t26.habittracker/drawable/default_profile_pic");
    private String picturePath = "image/" + "default_profile_pic" + ".jpeg";

    private NavController navController = null;

    private AuthController authController;
    private Boolean creatingUser;

    /**
     * Required empty public constructor
     */
    public SignupFragment() {}

    /**
     * inflates view
     *
     * @param inflater
     *  The inflater
     * @param container
     *  The container of the fragment
     * @param savedInstanceState
     *  The instance that was saved before.
     * @return
     *  Returns essentially the inflated iew.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        authController = AuthController.getInstance();
        creatingUser = false;

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    /**
     * Once views are created, sets the views to their corresponding variables
     *
     * @param view
     *  The current view.
     * @param savedInstanceState
     *  The instance that was saved before.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firstNameET = view.findViewById(R.id.firstNameET);
        lastNameET = view.findViewById(R.id.lastNameET);
        emailET = view.findViewById(R.id.emailET);
        usernameET = view.findViewById(R.id.usernameET);
        passwordET = view.findViewById(R.id.passwordET);
        confirmPassET = view.findViewById(R.id.confirmPassET);
        signupConfirmButton = view.findViewById(R.id.signUpConfirmButton);
        signupConfirmButton.setOnClickListener(signupConfirmOnClickListener);
        setProfilePic = view.findViewById(R.id.circleImageView);
        setProfilePic.setOnClickListener(setProfileOnClickListener);
        confirmPassLayout = view.findViewById(R.id.confirmPassLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);

        navController = Navigation.findNavController(view);
    }

    private final View.OnClickListener signupConfirmOnClickListener = view -> {
        if (!creatingUser && checkFieldsFilled()) {
            final String firstName = firstNameET.getText().toString();
            final String lastName = lastNameET.getText().toString();
            final String email = emailET.getText().toString();
            final String username = usernameET.getText().toString();
            final String password = passwordET.getText().toString();

            authController.isUserUnique(username, unused -> {

                creatingUser = true;
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Creating user, please wait a moment", Toast.LENGTH_LONG).show();
                }

                authController.createUserFirebaseAuth(firstName, lastName, email, username, password, unused1 -> {

                    authController.createUserFirebaseFirestore(username, firstName, lastName, email, picturePath, imageUri, unused2 -> {
                        // Notify user that account was created successfully
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "User created successfully!", Toast.LENGTH_LONG).show();
                        }
                        // Go to login fragment once data has been added
                        navController.navigate(R.id.action_signupFragment_to_loginFragment);
                        creatingUser = false;
                    }, errorMsg -> {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "There was an error with your user account", Toast.LENGTH_LONG).show();
                        }
                    });

                }, errorMsg -> {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                    creatingUser = false;
                });

            }, errorMsg -> usernameET.setError(errorMsg));
        }
    };

    /**
     * Functions essentially like the now deprecated onActivityResult.
     * When User clicks on the circle image view, their default
     * file explorer pops up, and once they select an image,
     * onActivityResult here notices, and sets the image
     * of the circle image view to the selected image.
     * As well, the picture path is saved to be later used when
     * the user clicks the signupConfirmButton.
     */
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Make sure uri not null; uri null can occur when we click to go to file explorer and press the back button without choosing an image
                    if (uri != null) {
                        picturePath = "image/" + uri.hashCode() + ".jpeg";
                        imageUri = uri;
                        setProfilePic.setImageURI(uri);
                    }
                }
            });

    private final View.OnClickListener setProfileOnClickListener = view -> {
        mGetContent.launch("image/*");      // Launch file explorer
    };

    /**
     * Checks all the EditText fields and makes sure they are filled and
     * that the password and confirmPassword matches. If they aren't,
     * the user is notified and must change it before proceeding again.
     *
     * @return
     *  Returns true if the fields are filled and the passwords match, false otherwise. Type {@link boolean}
     */
    private boolean checkFieldsFilled() {
        boolean filled = true;

        final String firstName = firstNameET.getText().toString();
        final String lastName = lastNameET.getText().toString();
        final String email = emailET.getText().toString();
        final String username = usernameET.getText().toString();
        final String password = passwordET.getText().toString();
        final String confirmPass = confirmPassET.getText().toString();

        if (firstName.isEmpty()) {
            firstNameET.setError("Cannot be empty");
            filled = false;
        }
        if (lastName.isEmpty()) {
            lastNameET.setError("Cannot be empty");
            filled = false;
        }
        if (email.isEmpty()) {
            emailET.setError("Cannot be empty");
            filled = false;
        }
        if (username.isEmpty()) {
            usernameET.setError("Cannot be empty");
            filled = false;
        }
        if (password.isEmpty()) {
            passwordLayout.setError("Cannot be empty");
            passwordLayout.setErrorIconDrawable(null);
            filled = false;
        } else {
            passwordLayout.setErrorEnabled(false);
        }
        if (confirmPass.isEmpty()) {
            confirmPassLayout.setError("Cannot be empty");
            confirmPassLayout.setErrorIconDrawable(null);
            filled = false;
        } else {
            confirmPassLayout.setErrorEnabled(false);
        }
        if (!confirmPass.equals(password)){
            confirmPassLayout.setError("Passwords must match");
            confirmPassLayout.setErrorIconDrawable(null);
            filled = false;
        } else {
            confirmPassLayout.setErrorEnabled(false);
        }

        return filled;
    }
}