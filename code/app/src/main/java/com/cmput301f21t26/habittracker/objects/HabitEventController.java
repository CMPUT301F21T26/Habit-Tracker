package com.cmput301f21t26.habittracker.objects;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HabitEventController provides methods that access the database.
 * It manages HabitEvent objects the currently signed in user has and associated snapshot listener.
 */
public class HabitEventController {

    private static HabitEventController instance = new HabitEventController();

    private UserController userController;
    private HabitController habitController;

    private Map<String, ListenerRegistration> habitEventsSnapshotListenerMap;

    private final FirebaseFirestore mStore;
    private final FirebaseStorage mStorage;
    private final CollectionReference usersRef;

    /**
     * Private constructor
     */
    private HabitEventController() {
        userController = UserController.getInstance();
        habitController = HabitController.getInstance();
        habitEventsSnapshotListenerMap = new HashMap<>();

        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        usersRef = mStore.collection("users");
    }

    /**
     * Return instance of HabitEventController with lazy construction
     *
     * @return instance of HabitEventController
     */
    public static HabitEventController getInstance() {
        return instance;
    }

    /**
     * Initialize a snapshot listener for habitEvents collection in the parent habit
     * Store the listener in habitEventsSnapshotListenerMap
     *
     * @param parentHabitId parent habit id
     */
    public void initHabitEventsSnapshotListener(String parentHabitId) {

        User user = userController.getCurrentUser();
        assert user != null;

        final DocumentReference userRef = usersRef.document(user.getUid());
        final DocumentReference parentHabitRef = userRef.collection("habits").document(parentHabitId);

        ListenerRegistration listener = parentHabitRef.collection("habitEvents")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("habitEventUpdate", "listen:error", error);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            // updated habit event
                            HabitEvent hEvent = dc.getDocument().toObject(HabitEvent.class);

                            switch (dc.getType()) {
                                case ADDED:
                                    user.getHabit(parentHabitId).addHabitEvent(hEvent);
                                    Log.d("AddHabitEvent", "HabitEventFragment: " + user.getHabit(parentHabitId).toString());
                                    Log.d("AddHabitEvent", user.getHabit(parentHabitId).getHabitEvents().toString());
                                    break;
                                case MODIFIED:
                                    user.getHabit(parentHabitId).updateHabitEvent(hEvent);
                                    break;
                                case REMOVED:
                                    user.getHabit(parentHabitId).deleteHabitEvent(hEvent);
                                    break;
                                default:
                                    Log.d("habitAdded", "Unexpected type: " + dc.getType());
                            }

                            user.notifyAllObservers();
                        }
                    }
                });
        habitEventsSnapshotListenerMap.put(parentHabitId, listener);
    }

    public void detachHabitEventsSnapshotListener(String parentHabitId) {
        habitEventsSnapshotListenerMap.get(parentHabitId).remove();     // remove snapshot listener
        habitEventsSnapshotListenerMap.remove(parentHabitId);           // remove the entry
    }

    public void detachAllHabitEventsSnapshotListener() {
        for (String habitId : habitEventsSnapshotListenerMap.keySet()) {
            habitEventsSnapshotListenerMap.get(habitId).remove();
        }
    }

    /**
     * Store a given habit event in an input parent habit's collection.
     * Call callback function after successful storing.
     *
     * @param hEvent    habit event to be stored
     * @param callback  callback function to be called after storing habit event
     */
    public void storeHabitEventInDb(HabitEvent hEvent, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(user.getUid());
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .set(hEvent)
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.d("addHabitEvent", "Adding habit event failed " + e.toString()));
    }

    /**
     * Update an existing habit event with a given habit event in db.
     * Call callback function after the update.
     *
     * @param hEvent updated habit event object
     * @param callback callback function to be called after the update
     */
    public void updateHabitEventInDb(HabitEvent hEvent, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(user.getUid());
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .set(hEvent, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    callback.onCallback(user);
                })
                .addOnFailureListener(e -> Log.w("updateHabitEvent", "Updating habit event failed", e));
    }

    /**
     * Remove the given habit event associated to the parent habit.
     * Call callback function after the removal.
     *
     * @param hEvent habit event to be removed
     * @param callback callback function to be called after the removal
     */
    public void removeHabitEventFromDb(HabitEvent hEvent, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(user.getUid());
        final DocumentReference habitRef = userRef.collection("habits").document(hEvent.getParentHabitId());

        habitRef.collection("habitEvents")
                .document(hEvent.getHabitEventId())
                .delete()
                .addOnSuccessListener(unused -> callback.onCallback(user))
                .addOnFailureListener(e -> Log.w("deleteHabitEvent", "Error deleting habit event", e));
    }


    /**
     * Given a habit, remove all of its associated habit events from the database.
     * Call callback function after the removal
     *
     * @param habit target habit
     * @param callback callback function to be called after the removal
     */
    public void removeAllHabitEventsOfHabitFromDb(Habit habit, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        final DocumentReference userRef = mStore.collection("users").document(user.getUid());
        final DocumentReference habitRef = userRef.collection("habits").document(habit.getHabitId());

        WriteBatch batch = mStore.batch();

        List<HabitEvent> habitEvents = habit.getHabitEvents();

        for (HabitEvent hEvent : habitEvents) {
            DocumentReference habitEventRef = habitRef.collection("habitEvents").document(hEvent.getHabitEventId());
            batch.delete(habitEventRef);
        }

        batch.commit().addOnSuccessListener(unused -> callback.onCallback(user));
    }

    /**
     * Get download url from Firestore Storage and update the habit event image url.
     * Call callback function with the signed in user after the update
     *
     * @param hEvent target habit event
     * @param picturePath path to the picture
     * @param imageUri uri of the image
     * @param callback callback function to be called after the update
     */
    public void updateHabitEventImageInDb(HabitEvent hEvent, String picturePath, Uri imageUri, UserCallback callback) {

        User user = userController.getCurrentUser();
        assert user != null;

        final StorageReference storageRef = mStorage.getReference(picturePath);

        storageRef
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // If successful, get the download url and store it in pictureURL
                    storageRef.getDownloadUrl().addOnSuccessListener(url -> {
                        hEvent.setPhotoUrl(url.toString());
                        updateHabitEventInDb(hEvent, callback);
                    });
                })
                .addOnFailureListener(e -> Log.d("storeHabitEventImage", "Habit Event Image was not stored"));
    }
}
