package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observer;

import androidx.annotation.RequiresApi;

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
            resetHabitsIsDone(callback);
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
     * Remove the given habit and all associated habit events from db.
     * Then remove corresponding snapshot listener for habit events collection
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

        user.removeAllHabitEventsOfHabitFromDb(habit, user1 -> {

            // remove habit document after removing all associated habit events
            user.removeHabitFromDb(habit, user2 -> {
                // remove snapshot listener for habit events collection associated to the given habit
                habitEventsSnapshotListenerMap.get(habit.getHabitId()).remove();
                habitEventsSnapshotListenerMap.remove(habit.getHabitId());

                callback.onCallback(user);
            });
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
    public static void updateDoneForTodayInDb(Habit todayHabit, boolean isDone, UserCallback callback) {
        assert user != null;

        todayHabit.setDoneForToday(isDone);
        user.updateHabitInDb(todayHabit, callback);
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
     * Update user's dateLastAccessed to now.
     *
     * @param callback callback function to be called after the update
     */
    private static void resetHabitsIsDone(UserCallback callback) {
        assert user != null;
        user.resetTodayHabitsInDb(user -> user.updateUserLastAccessedDateInDb(callback));
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

        user.removeHabitEventFromDb(hEvent, callback);
    }

    /**
     * Store habit event in a collection inside parent habit document
     * Call callback function after storing
     *
     * @param hEvent habit event to store
     * @param callback callback function to be called after storing habit event
     */
    public static void storeHabitEventInDb(HabitEvent hEvent, UserCallback callback) {
        assert user != null;

        user.storeHabitEventInDb(hEvent, callback);
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

        user.updateHabitEventInDb(hEvent, callback);
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

        user.updateProfilePicInDb(picturePath, imageUri, callback);
    }
}
