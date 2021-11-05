package com.cmput301f21t26.habittracker.objects;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Class that interacts with the database as well as other User objects.
 */
public class User extends Observable implements Serializable {

    private final String username;    // user id
    private String firstName;
    private String lastName;
    private String email;
    private String pictureURL;
    private Date dateLastAccessed;

    private List<String> followings;
    private List<String> followers;
    private List<Habit> habits;
    private List<Habit> todayHabits;
    private List<Permission> permissions;
    private Map<String, List<HabitEvent>> habitEventsMap;       // TODO refactor

    public User(String username, String firstName, String lastName, String email, String pictureURL, Date dateLastAccessed) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.pictureURL = pictureURL;
        this.dateLastAccessed = dateLastAccessed;

        this.followings = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.habits = new ArrayList<>();
        this.todayHabits = new ArrayList<>();
        this.permissions = new ArrayList<>();
        this.habitEventsMap = new HashMap<String, List<HabitEvent>>();
    }

    /**
     * Empty no-argument constructor required for deserialization
     */
    public User() {
        this("", "", "", "", "", Calendar.getInstance().getTime());
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

    public Date getDateLastAccessed() {
        return dateLastAccessed;
    }

    public Instant getDateLastAccessedDay(){ return dateLastAccessed.toInstant().truncatedTo(ChronoUnit.DAYS); }

    public void setDateLastAccessed(Date dateLastAccessed) {
        this.dateLastAccessed = dateLastAccessed;
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

    /**
     * Add id of a user whom this user is following
     * @param uid id of user this user is following
     */
    public void addFollowing(String uid) {
        followings.add(uid);
    }

    /**
     * Add id of a user who follows this user
     * @param uid id of user this user is following
     */
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
    public void addHabitEvent(String parentHabitId, HabitEvent hEvent) {
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void removeHabitEvent(String parentHabitId, HabitEvent hEvent) {
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
                // if habit event to be removed is dated to today, set done for today of parent habit to false
                if (hEvent.getHabitEventDateDay().equals(this.getDateLastAccessedDay())){
                    Habit habit = UserController.getHabit(parentHabitId);
                    habit.setDoneForToday(false);
                    UserController.updateHabitInDb(habit, user -> { });
                }
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
     * Remove a habit from all habits list
     *
     * @param habit habit to remove
     */
    public void removeHabit(Habit habit) {
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
    public void removeTodayHabit(Habit habit) {
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
    public void updateHabit(Habit habit) {
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
    public void updateTodayHabit(Habit habit) {
        for (int i=0; i<todayHabits.size(); i++) {
            if (todayHabits.get(i).getHabitId().equals(habit.getHabitId())) {
                // update with new habit
                todayHabits.set(i, habit);
                return;
            }
        }
    }

    /**
     * Notify all observers that observers the user
     */
    protected void notifyAllObservers() {
        setChanged();
        notifyObservers();
    }
}
