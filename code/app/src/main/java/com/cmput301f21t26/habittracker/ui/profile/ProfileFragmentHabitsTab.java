package com.cmput301f21t26.habittracker.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.ui.habit.HabitAdapter;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;

import java.util.ArrayList;


public class ProfileFragmentHabitsTab extends Fragment {
    private String TAG = "ProfileFragmentHabitsTab";

    private NavController navController;

    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;
    private RecyclerView mRecyclerView;
    private HabitAdapter.RecyclerViewClickListener rvlistener;
    private User userObject;

    public ProfileFragmentHabitsTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileFragment parentProfileFrag = (ProfileFragment) getParentFragment();
        userObject = parentProfileFrag.getOtherUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_habits_tab, container, false);

        mRecyclerView = view.findViewById(R.id.profileHabitsRecyclerView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        habitList = (ArrayList<Habit>) userObject.getHabits();
        // TODO If currentUser is not following otherUser, don't allow clicking or showing of habits
        rvlistener = new HabitAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "Clicked on item at position: " + String.valueOf(position));
                NavDirections action = MobileNavigationDirections.actionGlobalViewHabitFragment(habitList.get(position), userObject);
                navController.navigate(action);
            }
        };

        habitAdapter = new HabitAdapter(habitList, getActivity(), rvlistener);

        // display today habits
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(habitAdapter);

        // Hide checkbox
        habitAdapter.checkBoxVisibility(View.GONE);
    }
}