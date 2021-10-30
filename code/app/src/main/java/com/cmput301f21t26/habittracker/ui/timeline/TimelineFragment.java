package com.cmput301f21t26.habittracker.ui.timeline;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentTimelineBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TimelineFragment extends Fragment {
    private String TAG = "TimelineFragment";

    private TimelineViewModel timelineViewModel;
    private FragmentTimelineBinding binding;
    private ListView timelineListView;
    private TimelineListAdapter timelineListAdapter;
    private ArrayList<HabitEvent> habitEventsList;
    private Map<String, Habit> habitEventHabitMap;
    private NavController navController;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String username;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        timelineViewModel =
                new ViewModelProvider(this).get(TimelineViewModel.class);

        binding = FragmentTimelineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();
        mStore = FirebaseFirestore.getInstance();

        habitEventHabitMap = new HashMap<>();

        return root;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);
        habitEventsList = new ArrayList<>();
        timelineListView = view.findViewById(R.id.timelineListView);
        readData(new FirestoreCallback() {
            @Override
            public void onCallback(ArrayList<HabitEvent> list) {
                // Updates the list view with each call back
                // that adds in a new habit event to the list
                Log.d(TAG, habitEventsList.toString());
                timelineListAdapter = new TimelineListAdapter(getActivity(), habitEventsList);
                timelineListView.setAdapter(timelineListAdapter);
            }
        });

        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the habit event from clicked, and its associated habit
                // to prepare it to be sent to view habit event fragment
                HabitEvent hEvent = (HabitEvent) adapterView.getItemAtPosition(i);
                Habit habit = habitEventHabitMap.get(hEvent.getHabitEventId());

                // Navigate to view habit event fragment
                NavDirections action = MobileNavigationDirections.actionGlobalViewHabitEventFragment(hEvent, habit);
                navController.navigate(action);

            }
        });

    }

    /**
     * Reads the data from the user and its habit collection and habit
     * events collection. It then adds each habit's habit events
     * into the habit events list.
     *
     * @param firestoreCallback
     *  The callback for when readData finishes; This is to deal with asynchronous behaviour.
     */
    // Ref: https://stackoverflow.com/questions/50650224/wait-until-firestore-data-is-retrieved-to-launch-an-activity/50680352
    // Ref: https://stackoverflow.com/questions/53851955/querying-data-from-firestore-to-arraylist-but-got-nothing
    // https://www.youtube.com/watch?v=0ofkvm97i0s
    private void readData(FirestoreCallback firestoreCallback) {
        mStore.collection("users").document(username).collection("habits")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot habit : task.getResult()) {
                                Habit tempHabit = habit.toObject(Habit.class);
                                if (tempHabit.getHabitId() != null) {          // for when we inevitably loop to the place holder
                                    Log.d(TAG, "habitID is " + tempHabit.getHabitId());
                                    mStore.collection("users").document(username).collection("habits").document(tempHabit.getHabitId()).collection("habitEvents")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot habitEvent : task.getResult()) {
                                                            HabitEvent tempHabitEvent = habitEvent.toObject(HabitEvent.class);
                                                            habitEventsList.add(tempHabitEvent);
                                                            // Put the habit event's id in a map along with it's associated habit.
                                                            habitEventHabitMap.put(tempHabitEvent.getHabitEventId(), tempHabit);
                                                        }
                                                        firestoreCallback.onCallback(habitEventsList);
                                                    } else {
                                                        Log.d(TAG, "Error getting habit events: ", task.getException());
                                                    }
                                                }
                                            });
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting habit: ", task.getException());
                        }
                    }
                });

        Log.d(TAG, "HabitEventsList AFTER: " + habitEventsList);
    }

    /**
     * Callback function;
     */
    private interface FirestoreCallback {
        void onCallback(ArrayList<HabitEvent> list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}