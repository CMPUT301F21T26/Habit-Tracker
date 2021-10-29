package com.cmput301f21t26.habittracker.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmput301f21t26.habittracker.HabitAdapter;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ProfileFragmentHabitsTab extends Fragment {
    private String TAG = "ProfileFragmentHabitsTab";
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private String username;
    private Habit habit;
    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private HabitAdapter.RecyclerViewClickListener listener;
    private NavController navController;

    public ProfileFragmentHabitsTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        habitList = new ArrayList<>();

        username = mAuth.getCurrentUser().getDisplayName();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_habits_tab, container, false);

        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();
        mRecyclerView = view.findViewById(R.id.profileHabitsRecyclerView);

        // Get user's habits and put them into the recycler view
        mStore.collection("users").document(username).collection("habits")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Clear list before getting habits ******** meaning everytime we go back to this fragment
                            // we get the habits from the database every time....is this ideal????
                            habitList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()){
                                // each document is retrieved, and if its not the placeholder it converted to a habit
                                // then it is added to habitList
                                if (!document.getId().equals("placeholder")){
                                    habit = document.toObject(Habit.class);
                                    habitList.add(habit);
                                }
                            }
                            // feed todayHabitList to the adapter
                            setOnClickListener();
                            habitAdapter = new HabitAdapter(habitList, listener, username);
                            // display today habits
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            mRecyclerView.setAdapter(habitAdapter);

                            // Hide checkbox
                            habitAdapter.checkBoxVisibility(View.GONE);


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return view;
    }

    // When user clicks on item in recycler view, send them to view habit fragment
    private void setOnClickListener() {
        listener = new HabitAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "Clicked on item at position: " + String.valueOf(position));
                String habitId = habitList.get(position).getHabitId();
                Bundle bundle = new Bundle();
                bundle.putString("habitId", habitId);
                navController.navigate(R.id.viewHabitFragment, bundle);
            }
        };
    }
}