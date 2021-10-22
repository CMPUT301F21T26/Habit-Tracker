package com.cmput301f21t26.habittracker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;


public class SignupFragment extends Fragment implements  View.OnClickListener{
    final private String TAG = "signupAuthentication";
    private EditText firstNameET;
    private EditText lastNameET;
    private EditText emailET;
    private EditText usernameET;
    private EditText passwordET;
    private EditText confirmPassET;
    private Button signupConfirmButton;
    private CircleImageView setProfilePic;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private Boolean creatingUser = false;
    // Make it so the default profile pic is...the default profile pic. So user doesn't have to necessarily choose one.
    private Uri imageUri = Uri.parse("android.resource://com.cmput301f21t26.habittracker/drawable/default_profile_pic");
    private String picturePath = "image/" + imageUri.hashCode() + ".jpeg";


    private NavController navController = null;

    public SignupFragment() {
        // Required empty public constructor
    }

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


    /**
     * Initialize the sign up fragment and get instances
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();


    }

    /**
     * inflates view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    /**
     * Once views are created, sets the views to their corresponding variables
     *
     * @param view
     * @param savedInstanceState
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
        signupConfirmButton.setOnClickListener(this);
        setProfilePic = view.findViewById(R.id.circleImageView);
        setProfilePic.setOnClickListener(this);

        navController = Navigation.findNavController(view);
    }

    /**
     * Whenever user clicks an element that have OnClickListener, this checks which
     * element has been clicked and does its corresponding function.
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        if(view != null) {
            // User clicks the signupConfirmButton
            if (view.getId() == R.id.signUpConfirmButton) {
                // Make sure there currently isn't a user being created..prevents user from spamming signup button
                if (!creatingUser) {
                    // Check if the fields are filled in
                    if (checkFieldsFilled()) {
                        final String firstName = firstNameET.getText().toString();
                        final String lastName = lastNameET.getText().toString();
                        final String email = emailET.getText().toString();
                        final String username = usernameET.getText().toString();
                        final String password = passwordET.getText().toString();

                        // Check if username already exists
                        DocumentReference ref = mStore.collection("users").document(username);

                        ref.get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                // Username exists
                                                usernameET.setError("Username already exists");
                                            } else {
                                                // Username does not exist.
                                                createUserFirebaseAuth(firstName, lastName, email, username, password);
                                                creatingUser = true;
                                            }
                                        } else {
                                            Log.d(TAG, "Failed with: ", task.getException());
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getActivity(), "Currently creating user...", Toast.LENGTH_LONG).show();
                }

            }
            // User clicks circle image view
            else if (view.getId() == R.id.circleImageView) {          // If the circle image view is clicked
                mGetContent.launch("image/*");                    // Launch file explorer
            }
        }
    }

    /**
     * Once user has entered all the fields (which has already been checked) and signup button is clicked,
     * creates a new user in Firebase Auth.
     *
     * @param firstName
     * @param lastName
     * @param email
     * @param username
     * @param password
     */
    public void createUserFirebaseAuth(final String firstName, final String lastName, final String email, final String username, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Creation of User with email successful.");
                            createUserFirebaseFirestore(firstName, lastName, email, username);
                        } else {
                            Log.w(TAG, "Creation of User with email failed. " + task.getException().getMessage());
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            creatingUser = false;
                        }
                    }
                });


    }

    /**
     * Creates a Firestore document for the user with the
     * all userdata in it.
     *
     * @param firstName
     * @param lastName
     * @param email
     * @param username
     */
    public void createUserFirebaseFirestore(String firstName, String lastName, String email, String username) {

        Map<String, Object> userData = new HashMap<>();

        userData.put(username, new User(username, firstName, lastName, email));

        final CollectionReference collectionReference = mStore.collection("users");

        collectionReference
                .document(username)
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Data added succesfully");
                        // Go to login fragment once data has been added
                        navController.navigate(R.id.action_signupFragment_to_loginFragment);
                        creatingUser = false;

                        mStorageReference = mStorage.getReference(picturePath);
                        mStorageReference
                                .putFile(imageUri)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "Default profile pic was not stored");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Data failed to add.");
                        Toast.makeText(getActivity(), "There was an error with your user account", Toast.LENGTH_LONG).show();
                    }
                });
    }


    /**
     * Checks all the EditText fields and makes sure they are filled and
     * that the password and confirmPassword matches.
     *
     * @return
     */
    private Boolean checkFieldsFilled() {
        Boolean filled = true;

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
            passwordET.setError("Cannot be empty");
            filled = false;
        }
        if (confirmPass.isEmpty()) {
            confirmPassET.setError("Cannot be empty");
            filled = false;
        }
        if (!confirmPass.equals(password)){
            confirmPassET.setError("Passwords must match");
            filled = false;
        }

        return filled;
    }


}