package com.cmput301f21t26.habittracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.HabitAdapter;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentTodayHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;

import java.util.ArrayList;

public class TodayHabitFragment extends Fragment {

    private final String TAG = "TodayHabitFragment";

    private FragmentTodayHabitBinding binding;
    private ArrayList<Habit> todayHabitList;
    private HabitAdapter habitAdapter;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private HabitAdapter.RecyclerViewClickListener rvListener;

    private NavController navController;

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

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        todayHabitList = (ArrayList<Habit>) UserController.getCurrentUser().getTodayHabits();

        rvListener = new HabitAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Habit habit = todayHabitList.get(position);     // retrieve habit in this pos
                Bundle bundle = new Bundle();
                bundle.putString("habitId", habit.getHabitId());        // pass habit id
                navController.navigate(R.id.viewHabitFragment, bundle);
            }
        };

        // feed todayHabitList to the adapter
        habitAdapter = new HabitAdapter(todayHabitList, getActivity(), rvListener);

        // display today habits
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(habitAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}