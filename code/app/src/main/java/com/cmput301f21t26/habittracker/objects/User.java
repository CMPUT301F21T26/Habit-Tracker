package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Class that interacts with the database as well as other User objects.
 */
public class User implements Serializable {

    private final UserID uid;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private final Date creationDate;

    private final List<UserID> followings;
    private final List<UserID> followers;
    private final List<Habit> habits;
    private final List<Habit> todayHabits;
    private final List<Permission> permissions;

    public User(String username, String firstName, String lastName, String email) {
        this.uid = new UserID(username);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.creationDate = Calendar.getInstance().getTime();

        this.followings = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.habits = new ArrayList<>();
        this.todayHabits = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    public UserID getUid() {
        return this.uid;
    }

    public String getStringUid() {
        return this.uid.toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UserID> getFollowing() {
        return followings;
    }

    public List<UserID> getFollowers() {
        return followers;
    }

    public List<Habit> getHabits() {
        return habits;
    }

    public List<Habit> getTodayHabits() {
        return todayHabits;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void addFollowing(UserID uid) {
        followings.add(uid);
    }

    public void addFollower(UserID uid) {
        followers.add(uid);
    }

    public void addHabit(Habit habit) {
        habits.add(habit);
    }

    public void addTodayHabit(Habit habit) {
        habits.add(habit);
    }

    public void changeProfilePic(Uri uri) {
        // To be implemented
    }



}
