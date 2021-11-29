package com.cmput301f21t26.habittracker.ui.habit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentTodayHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Display's today's date and the daily habits of the user
 */
public class TodayHabitFragment extends Fragment {

    private final String TAG = "TodayHabitFragment";

    private FragmentTodayHabitBinding binding;
    private ArrayList<Habit> todayHabitList;
    private HabitAdapter habitAdapter;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private HabitAdapter.RecyclerViewClickListener rvListener;
    private TextView todayIsDay;

    private NavController navController;

    private UserController userController;

    /**
     * Assign UI elements
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTodayHabitBinding.inflate(inflater, container, false);
        mRecyclerView = binding.todayHabitRV;
        mLayoutManager = new LinearLayoutManager(getActivity());
        todayIsDay = binding.todayIsDayTV;

        userController = UserController.getInstance();

        return binding.getRoot();
    }

    /**
     * Fetch today's habits and display them
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        // set today's day
        todayIsDay.setText(LocalDate.now().getDayOfWeek().name());

        todayHabitList = (ArrayList<Habit>) userController.getCurrentUser().getTodayHabits();
        // If there are no habits today, show text that says so
        if (todayHabitList.isEmpty()) {
            binding.noHabitsTodayTV.setVisibility(View.VISIBLE);
        }
        Log.d("Number", String.valueOf(todayHabitList.size()));

        rvListener = new HabitAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(int position) {
                Habit habit = todayHabitList.get(position);     // retrieve habit in this pos
                NavDirections action = MobileNavigationDirections.actionGlobalViewHabitFragment(habit, userController.getCurrentUser());
                navController.navigate(action);
            }
        };

        // feed todayHabitList to the adapter
        habitAdapter = new HabitAdapter(todayHabitList, getActivity(), rvListener);
        // Set ItemTouchHelper to disallow rearranging habits
        HabitItemTouchHelper habitItemTouchHelper = new HabitItemTouchHelper(habitAdapter);
        habitItemTouchHelper.setDraggable(false);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(habitItemTouchHelper);
        habitAdapter.setItemTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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