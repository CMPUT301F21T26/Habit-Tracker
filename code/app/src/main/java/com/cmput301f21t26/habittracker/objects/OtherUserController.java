package com.cmput301f21t26.habittracker.objects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cmput301f21t26.habittracker.interfaces.HabitsListCallback;
import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.cmput301f21t26.habittracker.interfaces.UserListCallback;
import com.cmput301f21t26.habittracker.ui.profile.ProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

/**
 * This class handles users that are not the current user
 */
public class OtherUserController {
    private static DocumentReference userReference;
    private static FirebaseFirestore mStore;
    private static CollectionReference collectionReference;
    private static User otherUser;

//    /**
//     * Initializes the OtherUserController with the given username String
//     * @param otherUsername
//     *  The username of the other user (not the current one) {@link String}
//     */
//    public OtherUserController(String otherUsername) {
//        mStore = FirebaseFirestore.getInstance();
//        collectionReference = mStore.collection("users");
//        userReference = collectionReference.document(otherUsername);
//        readUserDataFromDb(new UserCallback() {
//            @Override
//            public void onCallback(User user) {
//                otherUser = user;
//            }
//        });
//    }

    /**
     * Empty constructor
     */
    public OtherUserController() {
        mStore = FirebaseFirestore.getInstance();
        collectionReference = mStore.collection("users");
    }

    /**
     * Given a text, getUsersList will return a string list
     * containing the usernames that closely match with
     * the given text.
     * @param newText
     *  The query text, type {@link String}
     */
    public static void getUsersList(String newText, UserListCallback callback) {
        ArrayList<User> usersList = new ArrayList<>();
        if (!newText.isEmpty()) {
            Query query = collectionReference.orderBy("username").startAt(newText).endAt(newText+ "\uf8ff");
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot user : task.getResult()) {
                            User tempUser = user.toObject(User.class);
                            // Add the user's habits
                            getHabitList(tempUser.getUsername(), new HabitsListCallback() {
                                @Override
                                public void onCallback(ArrayList<Habit> listOfHabits) {
                                    for (Habit habit : listOfHabits) {
                                        tempUser.addHabit(habit);
                                    }
                                }
                            });
                            usersList.add(tempUser);
                        }
                        callback.onCallback(usersList);
                    }
                }
            });
        } else {
            // When the newText is empty, we don't want all users in the database to be shown
            // So we send back an empty list
            callback.onCallback(new ArrayList<>());
        }

    }

//    /**
//     * Gets the user specified in the initialization
//     * @return
//     *  The user that was specified in the initialization {@link User}
//     */
//    public User getOtherUser() {return otherUser;}
//
//    /**
//     * Get user data from the database.
//     * The callback function is then called.
//     *
//     * @param callback callback function to be called after reading data from db
//     */
//    private static void readUserDataFromDb(UserCallback callback) {
//
//        userReference.get().addOnSuccessListener(documentSnapshot -> {
//            Log.d("OtherUserController", "read data from user");
//            String username = documentSnapshot.getString("username");
//            String firstName = documentSnapshot.getString("firstName");
//            String lastName = documentSnapshot.getString("lastName");
//            String email = documentSnapshot.getString("email");
//            String pictureURL = documentSnapshot.getString("pictureURL");
//            Date dateLastAccessed = documentSnapshot.getDate("dateLastAccessed");
//
//            otherUser = new User(username, firstName, lastName, email, pictureURL, dateLastAccessed);
//
//            callback.onCallback(otherUser);
//
//        }).addOnFailureListener(e -> Log.w("OtherUserController", "Reading other user data failed" + e.toString()));
//    }

    /**
     * Creates a list of private habits from the given user
     * @param username
     *  The username of the user that we want to get the habits from, {@link String}
     * @param callback
     *  The callback needed due to asynchronous calls, {@link HabitsListCallback}
     */
    private static void getHabitList(String username, HabitsListCallback callback) {
        ArrayList<Habit> habitsList = new ArrayList<>();
        userReference = collectionReference.document(username);
        userReference.collection("habits").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot habit: task.getResult()) {
                        Habit tempHabit = habit.toObject(Habit.class);
                        if (!tempHabit.isPrivate()) {
                            habitsList.add(habit.toObject(Habit.class));
                        }
                    }
                    callback.onCallback(habitsList);
                }
            }
        });
    }
}
