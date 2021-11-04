package com.cmput301f21t26.habittracker.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.google.firebase.storage.UploadTask;

/**
 * Class that interacts with the database as well as other User objects.
 */
public class User extends Observable implements Serializable {

    private transient FirebaseAuth mAuth;
    private transient FirebaseFirestore mStore;
    private transient FirebaseStorage mStorage;

    private String username;    // user id
    private String firstName;
    private String lastName;
    private String email;
    private String pictureURL;
    private final Date creationDate;
    private Date dateLastAccessed;

    private List<String> followings;
    private List<String> followers;
    private List<Habit> habits;
    private List<Habit> todayHabits;
    private List<Permission> permissions;
    private Map<String, List<HabitEvent>> habitEventsMap;

    public User(String username, String firstName, String lastName, String email, String pictureURL) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.pictureURL = pictureURL;
        this.creationDate = Calendar.getInstance().getTime();
        this.dateLastAccessed = Calendar.getInstance().getTime();

        this.followings = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.habits = new ArrayList<>();
        this.todayHabits = new ArrayList<>();
        this.permissions = new ArrayList<>();
        this.habitEventsMap = new HashMap<String, List<HabitEvent>>();

        this.mAuth = FirebaseAuth.getInstance();
        this.mStore = FirebaseFirestore.getInstance();
        this.mStorage = FirebaseStorage.getInstance();
    }

    public User(String username) {
        this(username, "", "", "", "");
    }

    public User() {
        this("", "", "", "", "");
    }

    public String getUid() {
        return this.username;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getDateLastAccessed() {
        return dateLastAccessed;
    }

    /**
     * Update the date the user accessed to the app to now
     */
    public void updateDateLastAccessedToNow() {
        dateLastAccessed = Calendar.getInstance().getTime();
    }

    public List<String> getFollowing() {
        return followings;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<Habit> getHabits() {
        return habits;
    }

    /**
     * Given a habit id, return the habit object from the habits list.
     * Return null if no such habit is in the list.
     *
     * @param habitId String habit id
     * @return Habit if habit exists. Otherwise, return null
     */
    public Habit getHabit(String habitId) {
        for (int i=0; i<habits.size(); i++) {
            if (habitId.equals(habits.get(i).getHabitId())) {
                return habits.get(i);
            }
        }
        return null;
    }

    public List<Habit> getTodayHabits() {
        return todayHabits;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public Map<String, List<HabitEvent>> getHabitEventsMap() {
        return habitEventsMap;
    }

    public void addFollowing(String uid) {
        followings.add(uid);
    }

    public void addFollower(String uid) {
        followers.add(uid);
    }

    public void addHabit(Habit habit) {
        habits.add(habit);
    }

    public void addTodayHabit(Habit habit) {
        todayHabits.add(habit);
    }

    /**
     * Add habit event associated with the given parent habit in a map
     *
     * @param parentHabitId String id of parent habit associated with the habit event
     * @param hEvent habit event to add
     */
    private void addHabitEvent(String parentHabitId, HabitEvent hEvent) {
        if (!habitEventsMap.containsKey(parentHabitId)) {
            habitEventsMap.put(parentHabitId, new ArrayList<HabitEvent>());
        }
        List<HabitEvent> habitEventsList = habitEventsMap.get(parentHabitId);
        if (habitEventsList == null) {
            habitEventsList = new ArrayList<>();
        }
        habitEventsList.add(hEvent);
    }

    /**
     * Delete habit event from the associated parent habit
     *
     * @param parentHabitId String id of habit parent to habit event
     * @param hEvent habit event to delete
     * @throws IllegalArgumentException if parent habit id does not exist
     * @throws NullPointerException when attempt to access null habit event list
     * @throws RuntimeException when the habit event list is empty
     */
    private void removeHabitEvent(String parentHabitId, HabitEvent hEvent) {
        if (!habitEventsMap.containsKey(parentHabitId)) {
            throw new IllegalArgumentException("Parent habit id does not exist");
        }
        List<HabitEvent> habitEventsList = habitEventsMap.get(parentHabitId);
        if (habitEventsList == null) {
            throw new NullPointerException("Cannot delete habit event from a null habit events list");
        }
        if (habitEventsList.size() <= 0) {
            throw new RuntimeException("Cannot delete habit event from an empty list");
        }
        for (int i=0; i<habitEventsList.size(); i++) {
            if (hEvent.getHabitEventId().equals(habitEventsList.get(i).getHabitEventId())) {
                habitEventsList.remove(i);
                return;
            }
        }
    }

    /**
     * Update habit event from the associated parent habit
     *
     * @param parentHabitId String id of habit parent to habit event
     * @param hEvent habit event to update
     * @throws IllegalArgumentException if parent habit id does not exist
     * @throws NullPointerException when attempt to access null habit event list
     * @throws RuntimeException when the habit event list is empty
     */
    public void updateHabitEvent(String parentHabitId, HabitEvent hEvent) {
        if (!habitEventsMap.containsKey(parentHabitId)) {
            throw new IllegalArgumentException("Parent habit id does not exist");
        }
        List<HabitEvent> habitEventsList = habitEventsMap.get(parentHabitId);
        if (habitEventsList == null) {
            throw new NullPointerException("Cannot delete habit event from a null habit events list");
        }
        if (habitEventsList.size() <= 0) {
            throw new RuntimeException("Cannot delete habit event from an empty list");
        }
        for (int i=0; i<habitEventsList.size(); i++) {
            if (hEvent.getHabitEventId().equals(habitEventsList.get(i).getHabitEventId())) {
                habitEventsList.set(i, hEvent);
                return;
            }
        }
    }

    /**
     * Gets user data and all data inside the user's subcollections.
     * The callback function is then called.
     *
     * @param callback callback function to be called after reading data from db
     */
    protected void readUserDataFromDb(UserCallback callback) {

        final DocumentReference userRef = mStore.collection("users").document(getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {

            username = documentSnapshot.getString("username");
            firstName = documentSnapshot.getString("firstName");
            lastName = documentSnapshot.getString("lastName");
            email = documentSnapshot.getString("email");
            pictureURL = documentSnapshot.getString("pictureURL");
            dateLastAccessed = documentSnapshot.getDate("dateLastAccessed");

            this.habits = new ArrayList<>();
            this.todayHabits = new ArrayList<>();

            readHabitsFromDb(callback);


            // followings = (List<String>) documentSnapshot.get("followings");
            // followers = (List<String>) documentSnapshot.get("followers");
            // TODO permissions


        }).addOnFailureListener(e -> Log.w("readUserData", "Reading user data failed" + e.toString()));
    }

    protected void readHabitsFromDb(UserCallback callback) {

        final DocumentReference userRef = mStore.collection("users").document(getUid());
        final CollectionReference habitsRef = userRef.collection("habits");

        habitsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                    Habit habit = document.toObject(Habit.class);
                    addHabit(habit);

                    int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
                    if (habit.getDaysList().contains(today)) {
                        addTodayHabit(habit);
                    }
                }
                callback.onCallback(User.this);
                setChanged();
                notifyObservers();
            }
        }).addOnFailureListener(e -> Log.w("readHabitData", "Reading user habits failed" + e.toString()));
    }

    /**
     * Return the user document snapshot listener
     *
     * @return snapshot listener for user doc
     */
    protected ListenerRegistration getUserSnapshotListener() {

        final DocumentReference userRef = mStore.collection("users").document(getUid());

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

                username = snapshot.getString("username");
                firstName = snapshot.getString("firstName");
                lastName = snapshot.getString("lastName");
                email = snapshot.getString("email");
                pictureURL = snapshot.getString("pictureURL");
                dateLastAccessed = snapshot.getDate("dateLastAccessed");


                // TODO is it necessary to make new lists?
                // habits = new ArrayList<>();
                // todayHabits = new ArrayList<>();
                // followings = (List<String>) snapshot.get("followings");
                // followers = (List<String>) snapshot.get("followers");
                // TODO permissions

                setChanged();
                notifyObservers();

            }
        });
    }

    /**
     * Returns snapshot listener for user's habits collection.
     * The listener notifies the observers when a habit is added, deleted, or editted.
     *
     * @return snapshot listener for user's habits collection
     */
    protected ListenerRegistration getHabitsSnapshotListener() {

        final DocumentReference userRef = mStore.collection("users").document(getUid());

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
                                    addHabit(habit);

                                    if (habit.getDaysList().contains(today)) {
                                        addTodayHabit(habit);
                                    }
                                    Log.d("habitAdded", "habit was added " + habit.getTitle());
                                    break;
                                case MODIFIED:
                                    updateHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        updateTodayHabit(habit);
                                    }
                                    break;
                                case REMOVED:
                                    removeHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        removeTodayHabit(habit);
                                    }
                                    break;
                                default:
                                    Log.d("habitAdded", "Unexpected type: " + dc.getType());
                            }
                        }
                        setChanged();
                        notifyObservers();
                    }
                });
    }

    /**
     * Returns a snapshot listener for habitEvents collection in the parent habit
     *
     * @param parentHabitId parent habit id
     * @return  a snapshot listener for habitEvents collection
     */
    protected ListenerRegistration getHabitEventsSnapshotListener(String parentHabitId) {

        final DocumentReference userRef = mStore.collection("users").document(getUid());
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
                                    addHabitEvent(parentHabitId, hEvent);
                                    break;
                                case MODIFIED:
                                    updateHabitEvent(parentHabitId, hEvent);
                                    break;
                                case REMOVED:
                                    removeHabitEvent(parentHabitId, hEvent);
                                    break;
                                default:
                                    Log.d("habitAdded", "Unexpected type: " + dc.getType());
                            }

                            setChanged();
                            notifyObservers();
                        }
                    }
                });
    }

    /**
     * Remove a habit from all habits list
     *
     * @param habit habit to remove
     */
    private void removeHabit(Habit habit) {
        for (int i=0; i<habits.size(); i++) {
            if (habits.get(i).getHabitId().equals(habit.getHabitId())) {
                habits.remove(i);
                return;
            }
        }
    }

    /**
     * Remove a habit from today habits list
     *
     * @param habit today habit to remove
     */
    private void removeTodayHabit(Habit habit) {
        for (int i=0; i<todayHabits.size(); i++) {
            if (todayHabits.get(i).getHabitId().equals(habit.getHabitId())) {
                todayHabits.remove(i);
                return;
            }
        }
    }

    /**
     * Update a habit in all habits list
     *
     * @param habit habit to update
     */
    private void updateHabit(Habit habit) {
        for (int i=0; i<habits.size(); i++) {
            if (habits.get(i).getHabitId().equals(habit.getHabitId())) {
                // update with new habit
                habits.set(i, habit);
                return;
            }
        }
    }

    /**
     * Update a habit in today habits list
     *
     * @param habit today habit to update
     */
    private void updateTodayHabit(Habit habit) {
        for (int i=0; i<todayHabits.size(); i++) {
            if (todayHabits.get(i).getHabitId().equals(habit.getHabitId())) {
                // update with new habit
                todayHabits.set(i, habit);
                return;
            }
        }
    }

    /**
     * Store a given habit in the database then call the callback function
     *
     * @param habit habit to store in database
     * @param callback callback function to be called after storing habit in db
     */
    protected void storeHabitInDb(Habit habit, UserCallback callback) {
        mStore.collection("users").document(getUid()).collection("habits")
                .document(habit.getHabitId())
                .set(habit)
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.w("addHabit", "Adding habit failed", e));
    }

    /**
     * Remove a given habit from the database then call the callback function
     *
     * @param habit habit to be removed from db
     * @param callback callback function to be called after the removal
     */
    protected void removeHabitFromDb(Habit habit, UserCallback callback) {
        mStore.collection("users").document(getUid()).collection("habits")
                .document(habit.getHabitId())
                .delete()
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.w("removeHabit", "Removing habit failed", e));
    }

    /**
     * Given a habit, remove all of its associated habit events from the database.
     * Call callback function after the removal
     *
     * @param habit target habit
     * @param callback callback function to be called after the removal
     */
    protected void removeAllHabitEventsOfHabitFromDb(Habit habit, UserCallback callback) {
        final DocumentReference userRef = mStore.collection("users").document(getUid());
        final DocumentReference habitRef = userRef.collection("habits").document(habit.getHabitId());

        WriteBatch batch = mStore.batch();

        List<HabitEvent> habitEvents = habitEventsMap.get(habit.getHabitId());

        for (HabitEvent hEvent : habitEvents) {
            DocumentReference habitEventRef = habitRef.collection("habitEvents").document(hEvent.getHabitEventId());
            batch.delete(habitEventRef);
        }

        batch.commit().addOnSuccessListener(unused -> callback.onCallback(User.this));
    }
    /**
     * Update an existing habit to a given habit in the database then call the callback function
     *
     * @param habit updated habit to store
     * @param callback callback function to be called after the update
     */
    protected void updateHabitInDb(Habit habit, UserCallback callback) {
        mStore.collection("users").document(getUid()).collection("habits")
                .document(habit.getHabitId())
                .set(habit, SetOptions.merge())        // update the document, instead of overwriting it
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.w("updateHabit", "Updating habit failed", e));
    }

    /**
     * Store a given habit event in an input parent habit's collection.
     * Call callback function after successful storing.
     *
     * @param hEvent    habit event to be stored
     * @param callback  callback function to be called after storing habit event
     */
    protected void storeHabitEventInDb(HabitEvent hEvent, UserCallback callback) {
        final DocumentReference userRef = mStore.collection("users").document(getUid());
        final DocumentReference habitsRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitsRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .set(hEvent)
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.d("addHabitEvent", "Adding habit event failed " + e.toString()));
    }

    /**
     * Remove the given habit event associated to the parent habit.
     * Call callback function after the removal.
     *
     * @param hEvent habit event to be removed
     * @param callback callback function to be called after the removal
     */
    protected void removeHabitEventFromDb(HabitEvent hEvent, UserCallback callback) {

        final DocumentReference userRef = mStore.collection("users").document(username);
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .delete()
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.w("deleteHabitEvent", "Error deleting habit event", e));
    }

    /**
     * Update an existing habit event with a given habit event in db.
     * Call callback function after the update.
     *
     * @param hEvent habit event to update
     * @param callback callback function to be called after the update
     */
    protected void updateHabitEventInDb(HabitEvent hEvent, UserCallback callback) {
        final DocumentReference userRef = mStore.collection("users").document(username);
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .set(hEvent, SetOptions.merge())
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.w("updateHabitEvent", "Updating habit event failed", e));
    }

    /**
     * Update user's dateLastAccessed to now.
     *
     * @param callback callback function to be called after the update
     */
    protected void updateUserLastAccessedDateInDb(UserCallback callback) {

        mStore.collection("users").document(getUid())
                .update("dateLastAccessed", Calendar.getInstance().getTime())
                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                .addOnFailureListener(e -> Log.w("updateUser", "Error updating user dateLastAccessed", e));
    }

    /**
     * Reset doneForToday of all habits to false if the current date is
     * at least one day larger than user's dateLastAccessed
     *
     * @param callback callback function to be called after the update
     */
    protected void resetTodayHabitsInDb(UserCallback callback) {

        Calendar calNow = Calendar.getInstance();
        int yearNow = calNow.get(Calendar.YEAR);
        int monthNow = calNow.get(Calendar.MONTH);
        int weekNow = calNow.get(Calendar.WEEK_OF_MONTH);
        int dayNow = calNow.get(Calendar.DAY_OF_WEEK);

        Calendar calLastAccessed = Calendar.getInstance();
        calLastAccessed.setTime(dateLastAccessed);
        int yearLastAccessed = calLastAccessed.get(Calendar.YEAR);
        int monthLastAccessed = calLastAccessed.get(Calendar.MONTH);
        int weekLastAccessed = calLastAccessed.get(Calendar.WEEK_OF_MONTH);
        int dayLastAccessed = calLastAccessed.get(Calendar.DAY_OF_WEEK);


        final DocumentReference userRef = mStore.collection("users").document(getUid());
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
                    batch.commit().addOnCompleteListener(task1 -> callback.onCallback(User.this));

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
    protected void updateProfilePicInDb(String picturePath, Uri imageUri, UserCallback callback) {

        final StorageReference storageRef = mStorage.getReference(picturePath);

        storageRef
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // If successful, get the download url and store it in pictureURL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        mStore.collection("users").document(getUid())
                                .update("pictureURL", uri.toString())
                                .addOnSuccessListener(unused -> callback.onCallback(User.this))
                                .addOnFailureListener(e -> Log.d("updateUser", "Updating user failed"));
                    });
                })
                .addOnFailureListener(e -> Log.d("storeProfilePicture", "Default profile pic was not stored"));
    }
}
