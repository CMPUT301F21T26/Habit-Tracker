package com.cmput301f21t26.habittracker.objects;

import java.util.List;

/**
 * Class that interacts with the database as well as other User objects.
 */
public class User {
    private String uid;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> following;         // possibly storing uids? storing User objects would prob not be efficient space wise
    private List<String> followers;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

}
