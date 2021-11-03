package com.cmput301f21t26.habittracker.objects;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Objects;

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
}
