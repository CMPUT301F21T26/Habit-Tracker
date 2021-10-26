package com.cmput301f21t26.habittracker.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.HabitAdapter;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentTodayHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TodayHabitFragment extends Fragment {

    private final String TAG = "TodayHabitFragment";
    private String username;
    private User user;
    private Habit habit;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    private FragmentTodayHabitBinding binding;
    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTodayHabitBinding.inflate(inflater, container, false);

        mRecyclerView = binding.todayHabitRV;
        mLayoutManager = new LinearLayoutManager(getActivity());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();
        mStore = FirebaseFirestore.getInstance();

        // TODO refactor this later
        // get todayHabits subcollection, retrieving all habit documents
        // add each to a list of today habits
        // display in a list
        mStore.collection("users").document(username).collection("todayHabits")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()){
                            // each document is retrieved, and if its not the placeholder it converted to a habit
                            // then it is added to habitList
                            if (!document.getId().equals("placeholder")){
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                habit = document.toObject(Habit.class);
                                habitList.add(habit);

                            }
                        }
                        //feed habitList to adapter
                        habitAdapter = new HabitAdapter(habitList);

                        //display today habits
                        mRecyclerView.setLayoutManager(mLayoutManager);
                        mRecyclerView.setAdapter(habitAdapter);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });

        /*
        // get user object (deprecated)
        DocumentReference userRef = mStore.collection("users").document(username);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);

                Log.d(TAG, "Got user: " + user.getUsername());

                habitAdapter = new HabitAdapter((ArrayList<Habit>) user.getTodayHabits());

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(habitAdapter);
            }
        });
        */



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}