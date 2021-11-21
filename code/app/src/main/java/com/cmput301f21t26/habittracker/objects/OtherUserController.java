package com.cmput301f21t26.habittracker.objects;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cmput301f21t26.habittracker.interfaces.FollowStatusCallback;
import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.cmput301f21t26.habittracker.interfaces.UserListCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * This class handles users that are not the current user
 */
public class OtherUserController {

    private final FirebaseFirestore mStore;
    private final CollectionReference usersRef;
    private User otherUser;

    // Singleton
    private final static OtherUserController instance = new OtherUserController();

    /**
     * Private constructor
     */
    private OtherUserController() {
        mStore = FirebaseFirestore.getInstance();
        usersRef = mStore.collection("users");
    }

    public static OtherUserController getInstance() {
        return instance;
    }

    /**
     * Given a text, getUsersList will return a string list
     * containing the usernames that closely match with
     * the given text.
     * @param newText
     *  The query text, type {@link String}
     */
    public void getUsersList(String newText, UserListCallback callback) {

        User currentUser = UserController.getCurrentUser();

        ArrayList<User> usersList = new ArrayList<>();
        if (!newText.isEmpty()) {
            Query query = usersRef.orderBy("username").startAt(newText).endAt(newText+ "\uf8ff");
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot user : task.getResult()) {
                        if (!user.getId().equals(currentUser.getUid())) {
                            // only add the users not equal to current user
                            usersList.add(user.toObject(User.class));
                        }
                    }
                    callback.onCallback(usersList);
                }
            });
        } else {
            // When the newText is empty, we don't want all users in the database to be shown
            // So we send back an empty list
            callback.onCallback(usersList);
        }
    }

    /**
     * Push all public habits of the user into its habits list
     *
     * @param user User that we want to get the habits from, {@link String}
     * @param callback callback function to be called after storing
     */
    public void getHabitList(User user, UserCallback callback) {
        final DocumentReference userReference = usersRef.document(user.getUid());
        userReference.collection("habits")
                .whereEqualTo("private", false)       // query public habits
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot habit: task.getResult()) {
                            Log.d("otherUserHabits", "User: " + user.getUsername() + " habit: " + habit.getId());
                            user.addHabit(habit.toObject(Habit.class));
                        }
                        callback.onCallback(user);
                    }
                }
            });
    }

    /**
     * Gets the user object from the database from the given username
     * @param username username that we want to get the {@link User} object from
     * @param callback callback function to be called after retrieving user
     */
    public void getUser(String username, UserCallback callback) {
        final DocumentReference userReference = usersRef.document(username);
        userReference
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User otherUser = task.getResult().toObject(User.class);
                            callback.onCallback(otherUser);
                        }
                    }
                });
    }

    /**
     * Gets the follow status of current user to the given other user.
     * Then call callback function with FollowStatus as input.
     *
     * @param otherUser User
     * @param callback callback function to be called after retrieving status
     */
    public void getFollowStatusOfCurrentUserTo(User otherUser, FollowStatusCallback callback) {

        User currentUser = UserController.getCurrentUser();

        final DocumentReference otherUserReference = usersRef.document(otherUser.getUid());
        otherUserReference.collection("followRequests")
                .whereEqualTo("fromUid", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            FollowStatus status;

                            if (currentUser.isFollowing(otherUser)) {
                                status = FollowStatus.FOLLOWING;
                            } else {
                                if (task.getResult().size() <= 0) {
                                    // current user did not send follow request to this other user
                                    status = FollowStatus.NOT_FOLLOWING;
                                } else {
                                    // current user sent follow request
                                    status = FollowStatus.PENDING;
                                }
                            }

                            callback.onCallback(status);

                        } else {
                            Log.d("followStatus", "Error getting follow requests", task.getException());
                        }
                    }
                });
    }
}
