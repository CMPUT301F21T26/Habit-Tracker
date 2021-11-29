package com.cmput301f21t26.habittracker.objects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * HabitController provides methods that access the database.
 * It manages Habit objects the currently signed in user has and associated snapshot listener.
 */
public class HabitController {

    private static HabitController instance = new HabitController();

    private UserController userController;
    private HabitEventController habitEventController;

    private ListenerRegistration habitsSnapshotListener;

    private final FirebaseFirestore mStore;

    /**
     * Private constructor
     */
    private HabitController() {
        mStore = FirebaseFirestore.getInstance();

        userController = UserController.getInstance();
        habitEventController = HabitEventController.getInstance();
    }

    /**
     * Return instance of HabitController with lazy construction
     *
     * @return instance of HabitController
     */
    public static HabitController getInstance() {
        return instance;
    }

    /**
     * Initialize snapshot listener for user's habits collection.
     * The listener notifies the observers when a habit is added, deleted, or edited
     * When a habit is added, a snapshot listener for its habit events is added to the map.
     * When a habit is removed, the snapshot listener for its habit events is removed from the map.
     *
     * @return snapshot listener for user's habits collection
     */
    public void initHabitsSnapshotListener() {

        User user = userController.getCurrentUser();
        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(user.getUid());

        habitsSnapshotListener = userRef.collection("habits")
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

                            Habit habit = dc.getDocument().toObject(Habit.class);       // habit object does not contain any habit events

                            Date dateNow = Calendar.getInstance().getTime();
                            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

                            switch (dc.getType()) {
                                case ADDED:
                                    user.addHabit(habit);

                                    if (habit.getStartDate().before(dateNow) && habit.getDaysList().contains(today)) {
                                        user.addTodayHabit(habit);
                                    }
                                    habitEventController.initHabitEventsSnapshotListener(habit.getHabitId());
                                    Log.d("habitAdded", "habit was added " + habit.getTitle());
                                    break;
                                case MODIFIED:
                                    Log.d("updateHabit", habit.getDaysList().toString());
                                    // modified habit obj does not contain any habit events
                                    user.updateHabit(habit);
                                    if (habit.getDaysList().contains(today)) {
                                        Log.d("updateHabit", "added to the today habits");
                                        user.updateTodayHabit(habit);
                                    } else {
                                        user.removeTodayHabit(habit);
                                    }
                                    habitEventController.detachHabitEventsSnapshotListener(habit.getHabitId());
                                    // initializing habit events snapshot listener will add back all the habit events in db
                                    habitEventController.initHabitEventsSnapshotListener(habit.getHabitId());
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

                        user.notifyAllObservers();
                        }
                    }
                });
    }

    /**
     * Detach the snapshot listener for follow requests collection
     */
    public void detachHabitsSnapshotListener() {
        habitsSnapshotListener.remove();
    }

    /**
     * Store habit in db.
     * Call callback function after storing
     *
     * @param habit habit to store
     * @param callback callback function to be called after storing habit in db
     */
    public void storeHabitInDb(Habit habit, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        mStore.collection("users").document(user.getUid()).collection("habits")
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
     * Update an existing habit to a given habit in the database then call the callback function
     *
     * @param habit updated habit to store
     * @param callback callback function to be called after the update
     */
    public void updateHabitInDb(Habit habit, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        mStore.collection("users").document(user.getUid()).collection("habits")
                .document(habit.getHabitId())
                .set(habit, SetOptions.merge())        // update the document, instead of overwriting it
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.w("updateHabit", "Updating habit failed", e));
    }

    /**
     * Remove the given habit and all associated habit events from db.
     * Call callback function after the removal.
     *
     * @param habit habit to remove from db
     * @param callback callback function to be called after removal
     * @throws IllegalArgumentException if habit does not exist in the snapshot map
     */
    public void removeHabitFromDb(Habit habit, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        habitEventController.removeAllHabitEventsOfHabitFromDb(habit, cbUser -> {

            // remove habit from db
            mStore.collection("users").document(user.getUid()).collection("habits")
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
     * Gets the highest index and adds 1, then sets
     * that index to the given habit's habitPosition.
     * @param habit
     *  The habit to set the new habit position to, {@link Habit}
     * @param callback
     *  call back function to be called after setting the new position
     */
    public void setNewHabitPosition(Habit habit, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        Query highestIndexQuery = mStore.collection("users").document(user.getUid()).collection("habits")
                .orderBy("habitPosition", Query.Direction.DESCENDING).limit(1);

        highestIndexQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int highestPosition = -1;
                    for (DocumentSnapshot habit : task.getResult()) {
                        // this loop iterates only once
                        highestPosition = ((Long) habit.get("habitPosition")).intValue();
                        Log.d("HabitController", "the highest position index is: "+highestPosition);
                    }

                    habit.setHabitPosition(highestPosition + 1);
                    callback.onCallback(user);
                }
            }
        });
    }

    /**
     * Updates the habitPositions of each habit
     */
    public void updateHabitPositions() {

        User user = userController.getCurrentUser();
        assert user != null;

        for (Habit habit : user.getHabits()) {
            habit.setHabitPosition(user.getHabits().indexOf(habit));
            updateHabitInDb(habit, cbUser -> {});
        }
    }

    /**
     * Reset doneForToday of all habits to false if the current date is
     * at least one day larger than user's dateLastAccessed
     *
     * @param callback callback function to be called after the update
     */
    public void resetHabitsInDb(UserCallback callback) {

        User user = userController.getCurrentUser();
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

        final DocumentReference userRef = mStore.collection("users").document(user.getUid());
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
     * Loops through all user habits and updates their denominators for visual indicators
     */
    public void updateVisualIndicator() {
        User user = userController.getCurrentUser();
        assert user != null;
        Date lastAccessed = user.getDateLastAccessed();
        Calendar now = Calendar.getInstance();
        List<Habit> habits = user.getHabits();
        for (int i=0; i<habits.size(); i++) {
            updateVisualDenominator(habits.get(i), lastAccessed, now);
        }
    }

    /**
     * Updates supposedHE to keep track of how many habit events were supposed to have happened
     * Used in UserController in the process of setting up the user class when logging in/authenticating
     * Grabs dayLastAccessed before it can update to today from the user, then loops through to today
     * Checks for days after current day
     * @param dayLastAccessed
     * The last time the user logged in prior to today
     * @param today
     * the date for today, only passed so we don't have to get todays date every time within the function
     */
    public void updateVisualDenominator(Habit habit, Date dayLastAccessed, Calendar today) {
        //loop through days until we get to current day, translate this.daysList into which days
        //of the week we need to update the denominator for
        //first things first convert dayLastAccessed to a calendar object to make comparisons easier
        Calendar start = Calendar.getInstance();
        start.setTime(dayLastAccessed);
        start.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        //now check today>start
        while (start.before(today)) {
            //now we know at least one day has passed, loop through all days until we get to today
            start.add(Calendar.DAY_OF_WEEK, 1);
            if (habit.getDaysList().contains(start.get(Calendar.DAY_OF_WEEK)-1)) {
                habit.setSupposedHE(habit.getSupposedHE()+1);
            }
            // loops up to exact date
        }
        updateHabitInDb(habit, user -> {});
    }


}
