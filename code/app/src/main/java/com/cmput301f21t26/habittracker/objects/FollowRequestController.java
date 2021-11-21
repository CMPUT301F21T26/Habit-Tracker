package com.cmput301f21t26.habittracker.objects;

import android.util.Log;

import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FollowRequestController {

    private static FollowRequestController instance = null;

    // private final UserController userController;
    private ListenerRegistration followRequestSnapshotListener;

    private final FirebaseFirestore mStore;
    private final CollectionReference usersRef;

    /**
     * Private constructor
     */
    private FollowRequestController() {
        mStore = FirebaseFirestore.getInstance();
        usersRef = mStore.collection("users");
    }

    /**
     * Return instance of FollowRequestController with lazy construction
     *
     * @return instance of FollowRequestController
     */
    public static FollowRequestController getInstance() {
        if (instance == null) {
            instance = new FollowRequestController();
        }
        return instance;
    }

    /**
     * Initialize a snapshot listener for follow requests collection
     */
    public void initFollowRequestSnapshotListener() {

        User user = UserController.getCurrentUser();
        assert user != null;

        followRequestSnapshotListener = usersRef.document(user.getUid()).collection("followRequests")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("followRequestsUpdate", "listen: error", error);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            FollowRequest followRequest = dc.getDocument().toObject(FollowRequest.class);

                            switch(dc.getType()) {
                                case ADDED:
                                    Log.d("addFollowRequest", "follow request with id: " + followRequest.getId() + " was added");
                                    user.addFollowRequest(followRequest);
                                    break;
                                case REMOVED:
                                    Log.d("removeFollowRequest", "follow request with id: " + followRequest.getId() + " was removed");
                                    user.removeFollowRequest(followRequest);
                                    break;
                                default:
                                    Log.d("followRequestsUpdate", "Unexpected type: " + dc.getType());
                            }
                            user.notifyAllObservers();
                        }
                    }
                });
    }

    /**
     * Detach the snapshot listener for follow requests collection
     */
    public void detachFollowRequestsSnapshotListener() {
        followRequestSnapshotListener.remove();
    }

    /**
     * Instantiate a FollowRequest object and store it in db.
     * Call callback function after storing.
     *
     * @param fromUser User that requests to follow the other user
     * @param toUser User who receives the follow request
     * @param callback callback function to be called after storing
     */
    public void storeFollowRequestInDb(User fromUser, User toUser, UserCallback callback) {

        User currentUser = UserController.getCurrentUser();
        assert currentUser.getUid().equals(fromUser.getUid());

        FollowRequest fr = new FollowRequest(fromUser, toUser);

        usersRef.document(fr.getToUid()).collection("followRequests")
                .document(fr.getId())
                .set(fr)
                .addOnSuccessListener(unused -> {
                    // TODO change callback param
                    callback.onCallback(fromUser);
                });
    }
}
