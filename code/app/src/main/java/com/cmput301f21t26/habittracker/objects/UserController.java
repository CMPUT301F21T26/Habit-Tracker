package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
                habitController.updateVisualIndicator();
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
     * Update the profile picture and pictureURL in db.
     * Call callback function after the update.
     *
     * @param picturePath String path to the new profile picture
     * @param imageUri uri of the new profile picture
     * @param callback callback function to be called after the update
     */
    public void updateProfilePicInDb(String picturePath, Uri imageUri, UserCallback callback) {

        assert user != null;
        final StorageReference storageRef = mStorage.getReference(picturePath);

        // Add image file to storage and get it's URL to store in user
        storageRef
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnCompleteListener(task -> {
                        mStore.collection("users").document(getCurrentUserId())
                                .update("pictureURL", task.getResult().toString())
                                .addOnSuccessListener(unused -> callback.onCallback(user))
                                .addOnFailureListener(e2 -> Log.d("updateUser", "Updating profile pic failed"));
                    });

                });


    }
}
