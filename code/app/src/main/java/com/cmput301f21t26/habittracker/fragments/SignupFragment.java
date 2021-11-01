package com.cmput301f21t26.habittracker.fragments;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private String profileImageUrl;

    private NavController navController = null;

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

    private final View.OnClickListener signupConfirmOnClickListener = view -> {
        if (!creatingUser && checkFieldsFilled()) {
            createUser();
        }
    };

    private final View.OnClickListener setProfileOnClickListener = view -> {
        mGetContent.launch("image/*");      // Launch file explorer
    };

    /**
     * Required empty public constructor
     */
    public SignupFragment() {}

    /**
     * Initialize the sign up fragment and get instances
     * @param savedInstanceState
     *  The instance that was saved before.
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

        navController = Navigation.findNavController(view);
    }

    /**
     * Function gets the text entered in the fields once it has been filled and the signup button
     * is clicked. It then checks if the username entered exists, and if not, starts creating
     * the user in Firebase Authentication. Otherwise, if the username exists, the user
     * is notified and they must enter a new one before clicking signup again.
     *
     *
     */
    public void createUser() {
        final String firstName = firstNameET.getText().toString();
        final String lastName = lastNameET.getText().toString();
        final String email = emailET.getText().toString();
        final String username = usernameET.getText().toString();
        final String password = passwordET.getText().toString();

        // Check if username already exists
        DocumentReference ref = mStore.collection("users").document(username);

        ref.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            // Username exists
                            usernameET.setError("Username already exists");
                        } else {
                            // Username does not exist.
                            createUserFirebaseAuth(firstName, lastName, email, username, password);
                            creatingUser = true;
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), "Creating user, please wait a moment", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Log.d(TAG, "Fetching username failed with: ", task.getException());
                    }
                });
    }

    /**
     * Once user has entered all the fields (which has already been checked) and signup button is clicked,
     * this function creates a new User within the Firebase Authentication section.
     *
     * @param firstName
     *  The name entered in the firstNameET EditText, type {@link String}
     * @param lastName
     *  The name entered in the lastNameET EditText, type {@link String}
     * @param email
     *  The email entered in the emailET EditText, type {@link String}
     * @param username
     *  The username entered in the usernameET EditText, type {@link String}
     * @param password
     *  The password entered in the passwordET EditText, type {@link String}
     */
    public void createUserFirebaseAuth(final String firstName, final String lastName, final String email, final String username, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Creation of User with email successful.");

                        // Set Display Name of user in Firebase Authentication so we can get it in MainActivity
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username).build();
                            user.updateProfile(profileUpdate);
                        }

                        // User created in Firebase Authentication, now to add its data into Firestore database
                        createUserFirebaseFirestore(firstName, lastName, email, username);
                    } else {
                        Log.w(TAG, "Creation of User with email failed. " + Objects.requireNonNull(task.getException()).getMessage());
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        creatingUser = false;
                    }
                });
    }

    /**
     * Creates a Firestore document for the newly signed up user with
     * all userdata in it.
     *
     * @param firstName
     *  The name entered in the firstNameET EditText, type {@link String}
     * @param lastName
     *  The name entered in the lastNameET EditText, type {@link String}
     * @param email
     *  The email entered in the emailET EditText, type {@link String}
     * @param username
     *  The username entered in the usernameET EditText, type {@link String}
     */
    public void createUserFirebaseFirestore(String firstName, String lastName, String email, String username) {

        final CollectionReference usersRef = mStore.collection("users");
        mStorageReference = mStorage.getReference(picturePath);

        mStorageReference
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // If successful, get the download url and store it in pictureURL
                    mStorageReference.getDownloadUrl().addOnCompleteListener(task -> {

                        profileImageUrl = Objects.requireNonNull(task.getResult()).toString();
                        Map<String, Object> user = new HashMap<>();

                        user.put("firstName", firstName);
                        user.put("lastName", lastName);
                        user.put("email", email);
                        user.put("username", username);
                        user.put("pictureURL", profileImageUrl);
                        user.put("dateLastAccessed", Calendar.getInstance().getTime());

                        usersRef
                                .document(username)     // user id: username
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "Data added succesfully");
                                    // Notify user that account was created successfully
                                    if (getActivity() != null) {
                                        Toast.makeText(getActivity(), "User created successfully!", Toast.LENGTH_LONG).show();
                                    }
                                    // Go to login fragment once data has been added
                                    navController.navigate(R.id.action_signupFragment_to_loginFragment);
                                    creatingUser = false;
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "Data failed to add.");
                                    if (getActivity() != null) {
                                        Toast.makeText(getActivity(), "There was an error with your user account", Toast.LENGTH_LONG).show();
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> Log.d(TAG, "Default profile picture was not stored"));
    }

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