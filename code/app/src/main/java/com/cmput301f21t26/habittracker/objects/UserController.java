package com.cmput301f21t26.habittracker.objects;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;
import java.util.Observer;

public class UserController {

    private static User user;
    private static ListenerRegistration userSnapshotListener;

    public static void registerCurrentUser() {

        String uid = getCurrentUserId();

        user = new User(uid);
        userSnapshotListener = user.getUserSnapshotListener();

        user.readUserDataFromDb(new User.Callback() {
            @Override
            public void onCallback(User user) {
                return;
            }
        });
    }

    public static String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
    }

    public static User getCurrentUser() {
        return user;
    }

    public static ListenerRegistration getCurrentUserSnapshotListener() {
        return userSnapshotListener;
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
}
