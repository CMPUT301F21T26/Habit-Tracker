package com.cmput301f21t26.habittracker.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Class that interacts with the database as well as other User objects.
 */
public class User extends Observable implements Serializable {

    private transient FirebaseAuth mAuth;
    private transient FirebaseFirestore mStore;

    private final String username;          // User cannot change username once created
    private String firstName;
    private String lastName;
    private String email;
    private String pictureURL;
    private final Date creationDate;
    private Date dateLastAccessed;

    private List<String> followings;
    private List<String> followers;
    private List<Habit> habits;
    private List<Permission> permissions;

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
        this.permissions = new ArrayList<>();

        this.mAuth = FirebaseAuth.getInstance();
        this.mStore = FirebaseFirestore.getInstance();
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

    /**
     * Return the user document snapshot listener
     *
     * @return snapshot listener for user doc
     */
    public ListenerRegistration getUserSnapshotListener() {

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

                firstName = snapshot.getString("firstName");
                lastName = snapshot.getString("lastName");
                email = snapshot.getString("email");
                pictureURL = snapshot.getString("pictureURL");
                dateLastAccessed = snapshot.getDate("dateLastAccessed");

                habits = (List<Habit>) snapshot.get("habits");
                followings = (List<String>) snapshot.get("followings");
                followers = (List<String>) snapshot.get("followers");
                // TODO permissions

                setChanged();
                notifyObservers();

            }
        });

    }
}
