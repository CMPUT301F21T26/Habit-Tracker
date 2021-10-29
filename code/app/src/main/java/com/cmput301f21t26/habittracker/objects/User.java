package com.cmput301f21t26.habittracker.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.net.Uri;

/**
 * Class that interacts with the database as well as other User objects.
 */
public class User implements Serializable {

    private final String username;          // User cannot change username once created
    private String firstName;
    private String lastName;
    private String email;
    private String picturePath;
    private final Date creationDate;
    private Date dateLastAccessed;

    private final List<String> followings;
    private final List<String> followers;
    private final List<Habit> habits;
    private final List<Habit> todayHabits;
    private final List<Permission> permissions;

    public User(String username, String firstName, String lastName, String email, String picturePath) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.picturePath = picturePath;
        this.creationDate = Calendar.getInstance().getTime();
        this.dateLastAccessed = Calendar.getInstance().getTime();

        this.followings = new ArrayList<>();
        this.followers = new ArrayList<>();
        this.habits = new ArrayList<>();
        this.todayHabits = new ArrayList<>();
        this.permissions = new ArrayList<>();
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

    public void setEmail(String email) {
        this.email = email;
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

    public List<Habit> getTodayHabits() {
        return todayHabits;
    }

    public List<Permission> getPermissions() {
        return permissions;
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

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public void changeProfilePic(Uri uri) {
        // To be implemented
    }



}
