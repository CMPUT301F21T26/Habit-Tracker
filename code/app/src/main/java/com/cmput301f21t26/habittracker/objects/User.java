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
    private String pictureURL;
    private final Date creationDate;
    private Date dateLastAccessed;

    public User(String username, String firstName, String lastName, String email, String pictureURL) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.pictureURL = pictureURL;
        this.creationDate = Calendar.getInstance().getTime();
        this.dateLastAccessed = Calendar.getInstance().getTime();
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

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }
}
