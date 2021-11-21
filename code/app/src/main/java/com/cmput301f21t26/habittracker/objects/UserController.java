package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
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

    private static User user;
    private static ListenerRegistration userSnapshotListener;
    private static ListenerRegistration habitsSnapshotListener;

    private static HabitEventController habitEventController;
    private static FollowRequestController followRequestController;

    private static final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private static final FirebaseStorage mStorage = FirebaseStorage.getInstance();

    /**
     * Initialize current user and attach snapshot listeners.
     * If the user accessed the app at least a day after the dateLastAccessed,
     * then isDone field for all habits in database are reset to false.
     * Then update the user' dateLastAccessed to now.
     *
     * @param callback callback function to be called after the initialization
     */
    public static void initCurrentUser(UserCallback callback) {

        // initialize the instances of controllers with lazy construction
        habitEventController = HabitEventController.getInstance();
        followRequestController = FollowRequestController.getInstance();

        readUserDataFromDb(user -> {

            // add snapshot listeners
            userSnapshotListener = getUserSnapshotListener();
            habitsSnapshotListener = getHabitsSnapshotListener();
            followRequestController.initFollowRequestSnapshotListener();

            resetHabitsInDb(user1 -> {
                updateUserLastAccessedDateInDb(callback);
            });
        });
    }

    public static String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
    }

    public static User getCurrentUser() {
        return user;
    }

    /**
     * Update user's dateLastAccessed to now.
     *
     * @param callback callback function to be called after the update
     */
    private static void updateUserLastAccessedDateInDb(UserCallback callback) {

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
    public static void addObserverToCurrentUser(Observer observer) {
        if (user != null) {
            // only add observer when the user exists
            user.addObserver(observer);
        }
    }

    /**
     * Remove the input observer from the current user
     * @param observer observer to remove
     */
    public static void deleteObserverFromCurrentUser(Observer observer) {
        if (user != null) {
            // only remove observer when the user exists
            user.deleteObserver(observer);
        }
    }

    /**
     * Remove all observers from the current user
     */
    public static void deleteAllObserversFromCurrentUser() {
        if (user != null) {
            // only remove all observers when the user exists
            user.deleteObservers();
        }
    }

    /**
     * Detach all snapshot listeners attached to user
     */
    public static void detachSnapshotListeners() {
        if (user != null) {
            // only detach when user exists
            userSnapshotListener.remove();
            habitsSnapshotListener.remove();
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
    private static void readUserDataFromDb(UserCallback callback) {

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
     * Return the user document snapshot listener
     *
     * @return snapshot listener for user doc
     */
    private static ListenerRegistration getUserSnapshotListener() {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());

        return userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
     * Return snapshot listener for user's habits collection.
     * The listener notifies the observers when a habit is added, deleted, or edited
     * When a habit is added, a snapshot listener for its habit events is added to the map.
     * When a habit is removed, the snapshot listener for its habit events is removed from the map.
     *
     * @return snapshot listener for user's habits collection
     */
    private static ListenerRegistration getHabitsSnapshotListener() {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());

        return userRef.collection("habits")
                .orderBy("habitPosition", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("habitUpdate", "Listening for habit collection update failed.", error);
                            return;
                        }
                        if (snapshots == null) {
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            Habit habit = dc.getDocument().toObject(Habit.class);
                            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

                            switch (dc.getType()) {
                                case ADDED:
                                    user.addHabit(habit);

                                    if (habit.getDaysList().contains(today)) {
                                        user.addTodayHabit(habit);
                                    }
                                    habitEventController.initHabitEventsSnapshotListener(habit.getHabitId());
                                    Log.d("habitAdded", "habit was added " + habit.getTitle());
                                    break;
                                case MODIFIED:
                                    user.updateHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        user.updateTodayHabit(habit);
                                    } else {
                                        user.removeTodayHabit(habit);
                                    }
                                    break;
                                case REMOVED:
                                    user.removeHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        user.removeTodayHabit(habit);
                                    }
                                    // remove snapshot listener for habit events collection associated to the given habit
                                    habitEventController.detachHabitEventsSnapshotListener(habit.getHabitId());
                                    break;
                                default:
                                    Log.d("habitAdded", "Unexpected type: " + dc.getType());
                            }
                        }
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
    public static Habit getHabit(String habitId) {
        return user.getHabit(habitId);
    }

    /**
     * Store habit in db.
     * Call callback function after storing
     *
     * @param habit habit to store
     * @param callback callback function to be called after storing habit in db
     */
    public static void storeHabitInDb(Habit habit, UserCallback callback) {
        assert user != null;

        // TODO exclude habitevents array
        mStore.collection("users").document(getCurrentUserId()).collection("habits")
                .document(habit.getHabitId())
                .set(habit)
                .addOnSuccessListener(unused -> {
                    // add snapshot listener for habit events collection associated to the given habit
                    String parentHabitId = habit.getHabitId();

                    callback.onCallback(user);
                })
                .addOnFailureListener(e -> Log.w("addHabit", "Adding habit failed", e));
    }

    /**
     * Gets the highest index and adds 1, then sets
     * that index to the given habit's habitPosition.
     * @param habit
     *  The habit to set the new habit position to, {@link Habit}
     * @param callback
     *  call back function to be called after setting the new position
     */
    public static void setNewHabitPosition(Habit habit, UserCallback callback) {
        assert user != null;
        Query highestIndexQuery = mStore.collection("users").document(getCurrentUserId()).collection("habits")
                .orderBy("habitPosition", Query.Direction.DESCENDING).limit(1);

        highestIndexQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int highestPosition = -1;
                    for (DocumentSnapshot habit : task.getResult()) {
                        highestPosition = ((Long) habit.get("habitPosition")).intValue();
                        Log.d("UserController", "the highest position index is: "+highestPosition);
                    }

                    habit.setHabitPosition(highestPosition + 1);
                    callback.onCallback(user);
                }
            }
        });
    }

    /**
     * Remove the given habit and all associated habit events from db.
     * Call callback function after the removal.
     *
     * @param habit habit to remove from db
     * @param callback callback function to be called after removal
     * @throws IllegalArgumentException if habit does not exist in the snapshot map
     */
    public static void removeHabitFromDb(Habit habit, UserCallback callback) {

        assert user != null;

        habitEventController.removeAllHabitEventsOfHabitFromDb(habit, cbUser -> {

            // remove habit from db
            mStore.collection("users").document(getCurrentUserId()).collection("habits")
                    .document(habit.getHabitId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        // Remove the habit from the user's habit list and update the habitPosition of each habit in db
                        updateHabitPositions();
                        callback.onCallback(cbUser);
                    })
                    .addOnFailureListener(e -> Log.w("removeHabit", "Removing habit failed", e));
        });
    }

    /**
     * Update an existing habit to a given habit in the database then call the callback function
     *
     * @param habit updated habit to store
     * @param callback callback function to be called after the update
     */
    public static void updateHabitInDb(Habit habit, UserCallback callback) {

        assert user != null;

        mStore.collection("users").document(getCurrentUserId()).collection("habits")
                .document(habit.getHabitId())
                .set(habit, SetOptions.merge())        // update the document, instead of overwriting it
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.w("updateHabit", "Updating habit failed", e));
    }

    /**
     * Reset doneForToday of all habits to false if the current date is
     * at least one day larger than user's dateLastAccessed
     *
     * @param callback callback function to be called after the update
     */
    public static void resetHabitsInDb(UserCallback callback) {

        assert user != null;

        Calendar calNow = Calendar.getInstance();
        int yearNow = calNow.get(Calendar.YEAR);
        int monthNow = calNow.get(Calendar.MONTH);
        int weekNow = calNow.get(Calendar.WEEK_OF_MONTH);
        int dayNow = calNow.get(Calendar.DAY_OF_WEEK);

        Calendar calLastAccessed = Calendar.getInstance();
        calLastAccessed.setTime(user.getDateLastAccessed());
        int yearLastAccessed = calLastAccessed.get(Calendar.YEAR);
        int monthLastAccessed = calLastAccessed.get(Calendar.MONTH);
        int weekLastAccessed = calLastAccessed.get(Calendar.WEEK_OF_MONTH);
        int dayLastAccessed = calLastAccessed.get(Calendar.DAY_OF_WEEK);

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final CollectionReference habitsRef = userRef.collection("habits");

        // get a new batch write
        WriteBatch batch = mStore.batch();
        habitsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    if ( dayNow > dayLastAccessed
                            || weekNow > weekLastAccessed
                            || monthNow > monthLastAccessed
                            || yearNow > yearLastAccessed ) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // reset doneForHabit of all habits to false
                            DocumentReference habitRef = habitsRef.document(document.getId());
                            batch.update(habitRef, "doneForToday", false);
                        }
                    }

                    // commit batch then call callback function
                    batch.commit().addOnCompleteListener(task1 -> callback.onCallback(user));

                } else {
                    Log.d("updateTodayHabits", "Error getting today habits: ", task.getException());
                }
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
    public static void updateProfilePicInDb(String picturePath, Uri imageUri, UserCallback callback) {

        assert user != null;

        final StorageReference storageRef = mStorage.getReference(picturePath);

        storageRef
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // If successful, get the download url and store it in pictureURL
                    storageRef.getDownloadUrl().addOnSuccessListener(url -> {
                        mStore.collection("users").document(getCurrentUserId())
                                .update("pictureURL", url.toString())
                                .addOnSuccessListener(unused -> callback.onCallback(user))
                                .addOnFailureListener(e -> Log.d("updateUser", "Updating user failed"));
                    });
                })
                .addOnFailureListener(e -> Log.d("storeProfilePicture", "Default profile pic was not stored"));
    }

    /**
     * Updates the habitPositions of each habit
     */
    public static void updateHabitPositions() {
        for (Habit habit : user.getHabits()) {
            habit.setHabitPosition(user.getHabits().indexOf(habit));
            UserController.updateHabitInDb(habit, cbUser -> {});
        }
    }
}
