package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AuthController {

    private final static AuthController instance = new AuthController();
    private final String TAG = "AuthController";

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStorage;

    private AuthController() {
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    public static AuthController getInstance() {
        return instance;
    }

    /**
     * Logs in the user into Firebase Authentication with the given input.
     * If successful, call onSuccessCallback with the signed in user.
     * Otherwise, call onFailureCallback.
     *
     * @param username
     *  The username of the account of type {@link String}
     * @param password
     *  The password of the account of type {@link String}
     * @param onSuccessCallback
     *  callback function to be called if the login is successful
     * @param onFailureCallback
     *  callback function to be called if the login is unsuccessfuly
     */
    public void login(String username, String password, UserCallback onSuccessCallback, OnFailureCallback onFailureCallback) {
        // check if user exists
        DocumentReference ref = mStore.collection("users").document(username);
        // if the user exists, pull their email and try to sign them in
        ref.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();

                        assert document != null;
                        if (document.exists()) {

                            // username exists, get the email and attempt to login
                            User user = document.toObject(User.class);
                            assert user != null;

                            mAuth.signInWithEmailAndPassword(user.getEmail(), password)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            onSuccessCallback.onCallback(user);
                                        } else {
                                            onFailureCallback.onCallback("Wrong password");
                                        }
                                    });
                        } else {
                            // username does not exist
                            onFailureCallback.onCallback("Invalid username");
                        }
                    } else {
                        Log.e(TAG, "Fetching user info failed: ", task.getException());
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    /**
     * Check whether the username is unique in database.
     * If it is, call onSuccessCallback.
     * Otherwise, call onFailureCallback.
     *
     * @param username String user id
     * @param onSuccessCallback callback to be called if the username is unique
     * @param onFailureCallback callback to be called if the username is not unique
     */
    public void isUserUnique(String username, OnSuccessCallback onSuccessCallback, OnFailureCallback onFailureCallback) {
        // Check if username already exists
        DocumentReference ref = mStore.collection("users").document(username);

        ref.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        assert document != null;
                        if (document.exists()) {
                            // Username exists
                            onFailureCallback.onCallback("Username already exists");
                        } else {
                            // Username does not exist
                            onSuccessCallback.onCallback(null);
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
    public void createUserFirebaseAuth(final String firstName, final String lastName, final String email, final String username, final String password,
                                       OnSuccessCallback onSuccessCallback, OnFailureCallback onFailureCallback) {
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

                        onSuccessCallback.onCallback(null);
                    } else {
                        Log.w(TAG, "Creation of User with email failed. " + Objects.requireNonNull(task.getException()).getMessage());
                        onFailureCallback.onCallback(task.getException().getMessage());
                    }
                });
    }

    /**
     * Creates a Firestore document for the newly signed up user with
     * all userdata in it.
     * @param picturePath
     *  The path to the picture, type {@link String}
     * @param firstName
     *  The name entered in the firstNameET EditText, type {@link String}
     * @param lastName
     *  The name entered in the lastNameET EditText, type {@link String}
     * @param email
     *  The email entered in the emailET EditText, type {@link String}
     * @param username
     *  The username entered in the usernameET EditText, type {@link String}
     */
     public void createUserFirebaseFirestore(String username, String firstName, String lastName, String email, String picturePath, Uri imageUri,
                                             OnSuccessCallback onSuccessCallback, OnFailureCallback onFailureCallback) {

         storeOrGetProfilePicture(picturePath, imageUri, profileImageUrl -> {

             Map<String, Object> user = new HashMap<>();

             user.put("firstName", firstName);
             user.put("lastName", lastName);
             user.put("email", email);
             user.put("username", username);
             user.put("pictureURL", profileImageUrl);
             user.put("dateLastAccessed", Calendar.getInstance().getTime());
             user.put("followings", new ArrayList<String>());
             user.put("followers", new ArrayList<String>());

             mStore.collection("users")
                     .document(username)     // user id: username
                     .set(user)
                     .addOnSuccessListener(unused -> {
                         Log.d(TAG, "User data added successfully");
                         onSuccessCallback.onCallback(null);
                     })
                     .addOnFailureListener(e -> {
                         Log.e(TAG, "Failed to store user data", e);
                         onFailureCallback.onCallback(e.getMessage());
                     });
         });
    }

    /**
     * stores or gets the profile picture in Firebase Storage. If the given picture path
     * already exists in Firebase Storage, it will retrieve that download URL and store it
     * into the user's pictureURL field. This is so then we aren't replacing the old image
     * with the new image and thus changing the tokens, which caused loading errors when
     * using Glide
     * @see <a href="https://stackoverflow.com/questions/41943860/getting-403-forbidden-error-when-trying-to-load-image-from-firebase-storage">Source</a>
     *
     * After storing or getting the picture url, we send the downloaded URL to createUserFirebaseFirestore
     * to store the rest of the data into Firestore.
     *
     * @param picturePath
     *  The path to the picture, type {@link String}
     */
    private void storeOrGetProfilePicture(String picturePath, Uri imageUri, ProfileImageUrlCallback callback) {

        final CollectionReference usersRef = mStore.collection("users");

        mStorage.getReference()
                .child(picturePath)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // if the image file already exists, download that url and store it in user
                    callback.onCallback(uri.toString());
                })
                .addOnFailureListener(e ->  {
                    // If image file doesn't exist, add it and get its url
                    StorageReference mStorageReference = mStorage.getReference(picturePath);
                    mStorageReference
                            .putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                mStorageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        String profileImageUrl = task.getResult().toString();
                                        callback.onCallback(profileImageUrl);
                                    }
                                });
                            });
                });
    }

    public interface OnSuccessCallback {
        void onCallback(Void unused);
    }

    public interface OnFailureCallback {
        void onCallback(String errorMsg);
    }

    public interface ProfileImageUrlCallback {
        void onCallback(String url);
    }
}
