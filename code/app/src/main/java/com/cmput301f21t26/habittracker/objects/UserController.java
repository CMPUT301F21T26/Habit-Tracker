package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observer;

/**
 * UserController provides all the methods that access the database.
 * It also manages the currently signed in user and associated snapshot listeners.
 */
public class UserController {

    private User user;
    private ListenerRegistration userSnapshotListener;

    private HabitController habitController;
    private HabitEventController habitEventController;
    private FollowRequestController followRequestController;

    private FirebaseFirestore mStore;
    private FirebaseStorage mStorage;

    private final static UserController instance = new UserController();

    private UserController() {
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    public static UserController getInstance() {
        return instance;
    }

    /**
     * Initialize current user and attach snapshot listeners.
     * If the user accessed the app at least a day after the dateLastAccessed,
     * then isDone field for all habits in database are reset to false.
     * Then update the user' dateLastAccessed to now.
     *
     * @param callback callback function to be called after the initialization
     */
    public void initCurrentUser(UserCallback callback) {

        // initialize the instances of controllers with lazy construction
        habitController = HabitController.getInstance();
        habitEventController = HabitEventController.getInstance();
        followRequestController = FollowRequestController.getInstance();

        readUserDataFromDb(user -> {

            // add snapshot listeners
            initUserSnapshotListener();
            habitController.initHabitsSnapshotListener();
            followRequestController.initFollowRequestSnapshotListener();

            habitController.resetHabitsInDb(cbUser -> {
                updateUserLastAccessedDateInDb(callback);
            });
        });
    }

    public String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
    }

    public User getCurrentUser() {
        return user;
    }

    /**
     * Update user's dateLastAccessed to now.
     *
     * @param callback callback function to be called after the update
     */
    private void updateUserLastAccessedDateInDb(UserCallback callback) {

        assert user != null;

        mStore.collection("users").document(getCurrentUserId())
                .update("dateLastAccessed", Calendar.getInstance().getTime())
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.w("updateUser", "Error updating user dateLastAccessed", e));
    }

    /**
     * Attach an input observer to the current user
     *
     * @param observer observer to attach to the current user
     */
    public void addObserverToCurrentUser(Observer observer) {
        if (user != null) {
            // only add observer when the user exists
            user.addObserver(observer);
        }
    }

    /**
     * Remove the input observer from the current user
     * @param observer observer to remove
     */
    public void deleteObserverFromCurrentUser(Observer observer) {
        if (user != null) {
            // only remove observer when the user exists
            user.deleteObserver(observer);
        }
    }

    /**
     * Remove all observers from the current user
     */
    public void deleteAllObserversFromCurrentUser() {
        if (user != null) {
            // only remove all observers when the user exists
            user.deleteObservers();
        }
    }

    /**
     * Detach all snapshot listeners attached to user
     */
    public void detachSnapshotListeners() {
        if (user != null) {
            // only detach when user exists
            userSnapshotListener.remove();
            habitController.detachHabitsSnapshotListener();
            habitEventController.detachAllHabitEventsSnapshotListener();
            followRequestController.detachFollowRequestsSnapshotListener();
        }
    }

    /**
     * Get user data from the database.
     * The callback function is then called.
     *
     * @param callback callback function to be called after reading data from db
     */
    private void readUserDataFromDb(UserCallback callback) {

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());

        userRef.get().addOnSuccessListener(documentSnapshot -> {

            String username = documentSnapshot.getString("username");
            String firstName = documentSnapshot.getString("firstName");
            String lastName = documentSnapshot.getString("lastName");
            String email = documentSnapshot.getString("email");
            String pictureURL = documentSnapshot.getString("pictureURL");
            Date dateLastAccessed = documentSnapshot.getDate("dateLastAccessed");

            user = new User(username, firstName, lastName, email, pictureURL, dateLastAccessed);

            callback.onCallback(user);

        }).addOnFailureListener(e -> Log.w("readUserData", "Reading user data failed" + e.toString()));
    }

    /**
     * Initialize the user document snapshot listener
     *
     * @return snapshot listener for user doc
     */
    private void initUserSnapshotListener() {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());

        userSnapshotListener = userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("userUpdate", "Listening for user update failed.", error);
                    return;
                }

                if (snapshot == null) {
                    return;
                }

                user.setFirstName(snapshot.getString("firstName"));
                user.setLastName(snapshot.getString("lastName"));
                user.setPictureURL(snapshot.getString("pictureURL"));
                user.setDateLastAccessed(snapshot.getDate("dateLastAccessed"));

                user.setFollowings((List<String>) snapshot.get("followings"));
                user.setFollowers((List<String>) snapshot.get("followers"));

                user.notifyAllObservers();
            }
        });
    }


    /**
     * Given a habit id, return the habit object from the habits list.
     * Return null if no such habit is in the list.
     *
     * @param habitId String habit id
     * @return Habit if habit exists. Otherwise, return null
     */
    public Habit getHabit(String habitId) {
        return user.getHabit(habitId);
    }

    /**
     * Update the profile picture and pictureURL in db.
     * Call callback function after the update.
     * Rather than replacing an image that already exists in the storage,
     * we check if the image already exists and if so, gets the download URL instead of replacing.
     * @see <a href="https://stackoverflow.com/questions/41943860/getting-403-forbidden-error-when-trying-to-load-image-from-firebase-storage">Source</a>
     *
     * @param picturePath String path to the new profile picture
     * @param imageUri uri of the new profile picture
     * @param callback callback function to be called after the update
     */
    public void updateProfilePicInDb(String picturePath, Uri imageUri, UserCallback callback) {

        assert user != null;

        final StorageReference storageRef = mStorage.getReference(picturePath);

        mStorage.getReference()
                .child(picturePath)
                .getDownloadUrl()
                .addOnSuccessListener(uri ->{
                    // if the image file already exists, download that url and store it in user
                    Log.d("UserControllerPP", "The URL after getting is: " + uri.toString());
                    mStore.collection("users").document(getCurrentUserId())
                            .update("pictureURL", uri.toString())
                            .addOnSuccessListener(unused -> callback.onCallback(user))
                            .addOnFailureListener(e -> Log.d("updateUser", "Updating profile pic failed"));
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // If image file doesn't exist, add it and get its url
                        storageRef
                                .putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    storageRef.getDownloadUrl().addOnCompleteListener(task -> {
                                        Log.d("UserControllerPP", "The URL after storing is: " + task.getResult().toString());
                                        mStore.collection("users").document(getCurrentUserId())
                                                .update("pictureURL", task.getResult().toString())
                                                .addOnSuccessListener(unused -> callback.onCallback(user))
                                                .addOnFailureListener(e2 -> Log.d("updateUser", "Updating profile pic failed"));
                                    });

                                });
                    }
                });
    }
}
