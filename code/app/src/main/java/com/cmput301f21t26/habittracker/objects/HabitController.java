package com.cmput301f21t26.habittracker.objects;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class HabitController {

    private static HabitController instance = null;

    // private final UserController userController;
    private ListenerRegistration habitSnapshotListener;

    private final FirebaseFirestore mStore;
    private final CollectionReference usersRef;

    /**
     * Private constructor
     */
    private HabitController() {
        mStore = FirebaseFirestore.getInstance();
        usersRef = mStore.collection("users");
    }

    /**
     * Return instance of HabitController with lazy construction
     *
     * @return instance of HabitController
     */
    public static HabitController getInstance() {
        if (instance == null) {
            instance = new HabitController();
        }
        return instance;
    }

    public void initHabitSnapshotListener() {

        User user = UserController.getCurrentUser();
        assert user != null;

    }


}
