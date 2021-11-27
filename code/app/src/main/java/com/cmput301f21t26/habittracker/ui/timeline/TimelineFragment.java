package com.cmput301f21t26.habittracker.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentTimelineBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.UserController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class TimelineFragment extends Fragment implements Observer {
    private String TAG = "TimelineFragment";

    private FragmentTimelineBinding binding;
    private NavController navController;

    private List<Habit> habitsList;
    private ArrayList<HabitEvent> allHabitEventsList;

    private TimelineListAdapter timelineListAdapter;
    private ListView timelineListView;

    private UserController userController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentTimelineBinding.inflate(inflater, container, false);

        userController = UserController.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        habitsList = userController.getCurrentUser().getHabits();
        allHabitEventsList = new ArrayList<>();
        // add all habit events into one list
        for (Habit habit : habitsList) {
            allHabitEventsList.addAll(habit.getHabitEvents());
        }

        // If there are no habit events, show text that says so
        if (allHabitEventsList.isEmpty()) {
            binding.noHabitEventsTV.setVisibility(View.VISIBLE);
        }

        timelineListView = binding.timelineListView;

        if (getActivity() != null) {
            // Sort habitEventsList in chronological order
            Collections.sort(allHabitEventsList, (habitEvent, t1) -> {
                if (habitEvent.getHabitEventDate() != null && t1.getHabitEventDate() != null) {
                    return t1.getHabitEventDate().compareTo(habitEvent.getHabitEventDate());
                } else {
                    return 0;
                }
            });
            timelineListAdapter = new TimelineListAdapter(getActivity(), allHabitEventsList);
            timelineListView.setAdapter(timelineListAdapter);
        }

        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the habit event from clicked, and its associated habit
                // to prepare it to be sent to view habit event fragment
                HabitEvent hEvent = (HabitEvent) adapterView.getItemAtPosition(i);
                Habit habit = userController.getHabit(hEvent.getParentHabitId());

                // Navigate to view habit event fragment
                NavDirections action = MobileNavigationDirections.actionGlobalViewHabitEventFragment(hEvent, habit);
                navController.navigate(action);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void update(Observable observable, Object o) {
        habitsList = userController.getCurrentUser().getHabits();
        allHabitEventsList = new ArrayList<>();

        // add all habit events into one list
        for (Habit habit : habitsList) {
            allHabitEventsList.addAll(habit.getHabitEvents());
        }
    }
}