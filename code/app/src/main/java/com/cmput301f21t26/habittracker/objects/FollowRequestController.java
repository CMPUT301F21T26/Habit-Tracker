package com.cmput301f21t26.habittracker.objects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.FollowRequestCallback;
import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
     * Send a FollowRequest to the given user and store it in db.
     * Call callback function after storing.
     *
     * @param toUser User who receives the follow request
     * @param callback callback function to be called after storing
     */
    public void sendFollowRequest(User toUser, UserCallback callback) {

        User currentUser = UserController.getCurrentUser();

        FollowRequest fr = new FollowRequest(currentUser, toUser);

        usersRef.document(fr.getToUid()).collection("followRequests")
                .document(fr.getId())
                .set(fr)
                .addOnSuccessListener(unused -> {
                    // TODO change callback param
                    callback.onCallback(currentUser);
                });
    }

    /**
     * Retrieve follow request send from fromUser to toUser.
     * Call callback function with the follow request
     *
     * @param fromUser user who sent the follow request
     * @param toUser user who received the follow request
     * @param callback callback function to be called after the retrieval
     */
    public void getFollowRequestSentBy(User fromUser, User toUser, FollowRequestCallback callback) {

        usersRef.document(toUser.getUid())
                .collection("followRequests")
                .whereEqualTo("fromUid", fromUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // looped only once
                                callback.onCallback(document.toObject(FollowRequest.class));
                                return;
                            }
                        } else {
                            Log.d("getFollowRequest", "Error getting follow request", task.getException());
                        }
                    }
                });
    }

    /**
     * Remove the already sent follow request from followrequests collection of the toUser.
     * Call callback function after removal
     *
     * @param fr FollowRequest to remove
     * @param callback callback function to be called after removal
     */
    public void undoFollowRequest(FollowRequest fr, UserCallback callback) {

        User currentUser = UserController.getCurrentUser();

        final CollectionReference followRequestsRef =
                usersRef.document(fr.getToUid()).collection("followRequests");

        followRequestsRef.document(fr.getId())
                .delete()
                .addOnSuccessListener(unused -> callback.onCallback(currentUser))
                .addOnFailureListener(error -> Log.d("undoFollowRequest", "Error removing follow request", error));
    }

    /**
     * Follow the targetUser
     *
     * @param fr FollowRequest
     * @param callback callback function to be called after the update
     */
    public void follow(FollowRequest fr, FollowRequestCallback callback) {

        // TODO could use transaction

        usersRef.document(fr.getToUid())
                .update("followers", FieldValue.arrayUnion(fr.getFromUid()))
                .addOnSuccessListener(unused -> {
                    usersRef.document(fr.getFromUid())
                            .update("followings", FieldValue.arrayUnion(fr.getToUid()))
                            .addOnSuccessListener(unused1 -> callback.onCallback(fr))
                            .addOnFailureListener(error -> Log.d("follow", "Error following user", error));
                })
                .addOnFailureListener(error -> Log.d("follow", "Error following user", error));

    }

    /**
     * Unfollow the targetUser
     *
     * @param targetUserId id of user to unfollow
     * @param callback callback function to be called after the update
     */
    public void unfollow(String targetUserId, UserCallback callback) {

        User currentUser = UserController.getCurrentUser();

        // TODO could use transaction

        usersRef.document(targetUserId)
                .update("followers", FieldValue.arrayRemove(currentUser.getUid()))
                .addOnSuccessListener(unused -> {
                    usersRef.document(currentUser.getUid())
                            .update("followings", FieldValue.arrayRemove(targetUserId))
                            .addOnSuccessListener(unused1 -> callback.onCallback(currentUser))
                            .addOnFailureListener(error -> Log.d("unfollow", "Error unfollowing user", error));
                })
                .addOnFailureListener(error -> Log.d("unfollow", "Error unfollowing user", error));
    }
}
