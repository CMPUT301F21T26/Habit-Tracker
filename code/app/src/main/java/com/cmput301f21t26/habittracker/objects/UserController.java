package com.cmput301f21t26.habittracker.objects;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Observer;

public class UserController {

    private static User user;
    private static ListenerRegistration userSnapshotListener;
    private static ListenerRegistration habitsSnapshotListener;
    private static Map<String, ListenerRegistration> habitEventsSnapshotListenerMap;

    /**
     * Initialize current user
     */
    public static void initCurrentUser(UserCallback callback) {

        String uid = getCurrentUserId();

        user = new User(uid);
        userSnapshotListener = user.getUserSnapshotListener();
        habitsSnapshotListener = user.getHabitsSnapshotListener();
        habitEventsSnapshotListenerMap = new HashMap<>();

        user.readUserDataFromDb(user -> {
            for (Habit habit : user.getHabits()) {
                // add snapshot listeners for habitEvents collection associated to the existing habits
                habitEventsSnapshotListenerMap.put(habit.getHabitId(), user.getHabitEventsSnapshotListener(habit.getHabitId()));
            }
            callback.onCallback(user);
        });
    }

    public static String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
    }

    public static ListenerRegistration getCurrentUserSnapshotListener() {
        return userSnapshotListener;
    }

    public static User getCurrentUser() {
        return user;
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
     * Store habit in db and a snapshot listener for habit events collection associated to it
     * @param habit habit to store
     * @param callback callback function to be called after storing habit in db
     */
    public static void storeHabitInDb(Habit habit, UserCallback callback) {
        assert user != null;

        user.storeHabitInDb(habit, user -> {
            // add snapshot listener for habit events collection associated to the given habit
            habitEventsSnapshotListenerMap.put(habit.getHabitId(), user.getHabitEventsSnapshotListener(habit.getHabitId()));

            callback.onCallback(user);
        });
    }

    /**
     * Remove the given habit from db and corresponding snapshot listener for habit events collection
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

        user.removeHabitFromDb(habit, user -> {
            // remove snapshot listener for habit events collection associated to the given habit
            habitEventsSnapshotListenerMap.get(habit.getHabitId()).remove();
            habitEventsSnapshotListenerMap.remove(habit.getHabitId());

            callback.onCallback(user);
        });
    }

    /**
     * Update the given habit in db
     *
     * @param habit habit to be updated
     * @param callback callback function to be called after the update
     */
    public static void updateHabitInDb(Habit habit, UserCallback callback) {
        assert user != null;

        user.updateHabitInDb(habit, callback);
    }

    /**
     * Given the today habit, update its doneForToday field with isDone and update it in the db.
     * Call callback function after the update.
     *
     * @param todayHabit target habit to be updated
     * @param isDone boolean
     * @param callback callback function to be called after the update
     */
    public static void updateDoneForToday(Habit todayHabit, boolean isDone, UserCallback callback) {
        assert user != null;

        todayHabit.setDoneForToday(isDone);
        user.updateHabitInDb(todayHabit, callback);
    }

    /**
     * Store habit event in a collection inside parent habit document
     *
     * @param parentHabit habit that owns the habit event
     * @param hEvent habit event to store
     * @param callback callback function to be called after storing habit event
     */
    public static void storeHabitEventInDb(Habit parentHabit, HabitEvent hEvent, UserCallback callback) {
        assert user != null;

        user.storeHabitEventInDb(parentHabit, hEvent, callback);
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
}
