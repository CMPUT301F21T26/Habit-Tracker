package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class UserController {

    private static User user;
    private static ListenerRegistration userSnapshotListener;
    private static ListenerRegistration habitsSnapshotListener;
    private static Map<String, ListenerRegistration> habitEventsSnapshotListenerMap;

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

        readUserDataFromDb(user -> {
            // add snapshot listeners
            userSnapshotListener = getUserSnapshotListener();
            habitEventsSnapshotListenerMap = new HashMap<>();
            for (Habit habit : user.getHabits()) {
                // add snapshot listeners for habitEvents collection associated to the existing habits
                // habit events are loaded when the snapshot listeners are first called
                habitEventsSnapshotListenerMap.put(habit.getHabitId(), getHabitEventsSnapshotListener(habit.getHabitId()));
            }

            // habitsSnapshotListener will add the same habits again when it's first called.
            // Clear already added habits.
            user.getHabits().clear();
            user.getTodayHabits().clear();
            habitsSnapshotListener = getHabitsSnapshotListener();

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
            for (String habitId : habitEventsSnapshotListenerMap.keySet()) {
                habitEventsSnapshotListenerMap.get(habitId).remove();
            }
        }
    }

    /**
     * Get user data and all data inside the user's subcollections.
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

            readHabitsFromDb(callback);

        }).addOnFailureListener(e -> Log.w("readUserData", "Reading user data failed" + e.toString()));
    }

    /**
     * Gets user's habits from the database and store them in the lists.
     * Call Callback function is then called.
     *
     * @param callback callback function is called after the storing
     */
    public static void readHabitsFromDb(UserCallback callback) {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final CollectionReference habitsRef = userRef.collection("habits");

        habitsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    Habit habit = document.toObject(Habit.class);
                    user.addHabit(habit);

                    int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                    if (habit.getDaysList().contains(today)) {
                        Log.d("habitAdded", "readHabitsFromDb");
                        user.addTodayHabit(habit);
                    }
                }
                callback.onCallback(user);
                user.notifyAllObservers();
            }
        }).addOnFailureListener(e -> Log.w("readHabitData", "Reading user habits failed" + e.toString()));
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

                user.notifyAllObservers();
            }
        });
    }

    /**
     * Return snapshot listener for user's habits collection.
     * The listener notifies the observers when a habit is added, deleted, or edited
     *
     * @return snapshot listener for user's habits collection
     */
    private static ListenerRegistration getHabitsSnapshotListener() {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());

        return userRef.collection("habits")
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
                                    Log.d("habitAdded", "habit was added " + habit.getTitle());
                                    break;
                                case MODIFIED:
                                    user.updateHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        user.updateTodayHabit(habit);
                                    }
                                    break;
                                case REMOVED:
                                    user.removeHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        user.removeTodayHabit(habit);
                                    }
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
     * Return a snapshot listener for habitEvents collection in the parent habit
     *
     * @param parentHabitId parent habit id
     * @return  a snapshot listener for habitEvents collection
     */
    private static ListenerRegistration getHabitEventsSnapshotListener(String parentHabitId) {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final DocumentReference parentHabitRef = userRef.collection("habits").document(parentHabitId);

        return parentHabitRef.collection("habitEvents")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("habitEventUpdate", "listen:error", error);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            HabitEvent hEvent = dc.getDocument().toObject(HabitEvent.class);

                            switch(dc.getType()) {
                                case ADDED:
                                    user.addHabitEvent(parentHabitId, hEvent);
                                    break;
                                case MODIFIED:
                                    user.updateHabitEvent(parentHabitId, hEvent);
                                    break;
                                case REMOVED:
                                    user.removeHabitEvent(parentHabitId, hEvent);
                                    break;
                                default:
                                    Log.d("habitAdded", "Unexpected type: " + dc.getType());
                            }

                            user.notifyAllObservers();
                        }
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
     * Store habit in db and a snapshot listener for habit events collection associated to it
     * Call callback function after storing
     *
     * @param habit habit to store
     * @param callback callback function to be called after storing habit in db
     */
    public static void storeHabitInDb(Habit habit, UserCallback callback) {
        assert user != null;

        mStore.collection("users").document(getCurrentUserId()).collection("habits")
                .document(habit.getHabitId())
                .set(habit)
                .addOnSuccessListener(unused -> {
                    // add snapshot listener for habit events collection associated to the given habit
                    String parentHabitId = habit.getHabitId();
                    habitEventsSnapshotListenerMap.put(parentHabitId, getHabitEventsSnapshotListener(parentHabitId));

                    callback.onCallback(user);
                })
                .addOnFailureListener(e -> Log.w("addHabit", "Adding habit failed", e));
    }

    /**
     * Remove the given habit and all associated habit events from db.
     * Then remove corresponding snapshot listener for habit events collection>
     * Call callback function after the removal.
     *
     * @param habit habit to remove from db
     * @param callback callback function to be called after removal
     * @throws IllegalArgumentException if habit does not exist in the snapshot map
     */
    public static void removeHabitFromDb(Habit habit, UserCallback callback) {

        assert user != null;

        if (!habitEventsSnapshotListenerMap.containsKey(habit.getHabitId())) {
            throw new IllegalArgumentException("Habit does not exist");
        }

        removeAllHabitEventsOfHabitFromDb(habit, cbUser -> {

            // remove snapshot listener for habit events collection associated to the given habit
            habitEventsSnapshotListenerMap.get(habit.getHabitId()).remove();
            habitEventsSnapshotListenerMap.remove(habit.getHabitId());

            // remove habit from db
            mStore.collection("users").document(getCurrentUserId()).collection("habits")
                    .document(habit.getHabitId())
                    .delete()
                    .addOnSuccessListener(unused -> callback.onCallback(cbUser))
                    .addOnFailureListener(e -> Log.w("removeHabit", "Removing habit failed", e));
        });
    }

    /**
     * Given a habit, remove all of its associated habit events from the database.
     * Call callback function after the removal
     *
     * @param habit target habit
     * @param callback callback function to be called after the removal
     */
    public static void removeAllHabitEventsOfHabitFromDb(Habit habit, UserCallback callback) {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final DocumentReference habitRef = userRef.collection("habits").document(habit.getHabitId());

        WriteBatch batch = mStore.batch();

        List<HabitEvent> habitEvents = user.getHabitEventsMap().get(habit.getHabitId());

        if (habitEvents != null){
            for (HabitEvent hEvent : habitEvents) {
                DocumentReference habitEventRef = habitRef.collection("habitEvents").document(hEvent.getHabitEventId());
                batch.delete(habitEventRef);
            }
        }

        batch.commit().addOnSuccessListener(unused -> callback.onCallback(user));
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
     * Given a date, and habit, return a habit event object at that date
     * Return null if no such object is found
     *
     * @param habit habit to search
     * @param date the date to search for
     * @return Habit event on given date, if it exists
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static HabitEvent getHabitEventByDate(Habit habit, Date date){
        assert user != null;
        List<HabitEvent> habitEventsList = user.getHabitEventsMap().get(habit.getHabitId());
        for (int i=0; i<habitEventsList.size(); i++) {
            if (habitEventsList.get(i).getHabitEventDateDay().equals(date.toInstant().truncatedTo(ChronoUnit.DAYS))){
                return habitEventsList.get(i);
            }
        }
        return null;
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
     * Store a given habit event in an input parent habit's collection.
     * Call callback function after successful storing.
     *
     * @param hEvent    habit event to be stored
     * @param callback  callback function to be called after storing habit event
     */
    public static void storeHabitEventInDb(HabitEvent hEvent, UserCallback callback) {
        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final DocumentReference habitsRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitsRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .set(hEvent)
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.d("addHabitEvent", "Adding habit event failed " + e.toString()));
    }

    /**
     * Remove the given habit event associated to the parent habit.
     * Call callback function after the removal.
     *
     * @param hEvent habit event to be removed
     * @param callback callback function to be called after the removal
     */
    public static void removeHabitEventFromDb(HabitEvent hEvent, UserCallback callback) {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .delete()
                .addOnSuccessListener(unused -> {
                    callback.onCallback(user);
                })
                .addOnFailureListener(e -> Log.w("deleteHabitEvent", "Error deleting habit event", e));
    }

    /**
     * Update an existing habit event with a given habit event in db.
     * Call callback function after the update.
     *
     * @param hEvent habit event to update
     * @param callback callback function to be called after the update
     */
    public static void updateHabitEventInDb(HabitEvent hEvent, UserCallback callback) {

        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(getCurrentUserId());
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .set(hEvent, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.w("updateHabitEvent", "Updating habit event failed", e));
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
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        mStore.collection("users").document(getCurrentUserId())
                                .update("pictureURL", uri.toString())
                                .addOnSuccessListener(unused -> callback.onCallback(user))
                                .addOnFailureListener(e -> Log.d("updateUser", "Updating user failed"));
                    });
                })
                .addOnFailureListener(e -> Log.d("storeProfilePicture", "Default profile pic was not stored"));
    }
}
