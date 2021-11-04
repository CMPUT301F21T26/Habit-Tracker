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
import androidx.lifecycle.ViewModelProvider;
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
import java.util.Map;
import java.util.Objects;

public class TimelineFragment extends Fragment {
    private String TAG = "TimelineFragment";

    private FragmentTimelineBinding binding;
    private NavController navController;

    private Map<String, List<HabitEvent>> habitEventsMap;
    private ArrayList<HabitEvent> habitEventsList;

    private TimelineViewModel timelineViewModel;
    private TimelineListAdapter timelineListAdapter;
    private ListView timelineListView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        timelineViewModel = new ViewModelProvider(this).get(TimelineViewModel.class);

        binding = FragmentTimelineBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        habitEventsMap = UserController.getCurrentUser().getHabitEventsMap();
        habitEventsList = new ArrayList<>();

        // add all habit events into one list
        for (String parentHabitId : habitEventsMap.keySet()) {
            habitEventsList.addAll(Objects.requireNonNull(habitEventsMap.get(parentHabitId)));
        }

        timelineListView = binding.timelineListView;

        if (getActivity() != null) {
            // Sort habitEventsList in chronological order
            Collections.sort(habitEventsList, (habitEvent, t1) -> {
                if (habitEvent.getHabitEventDate() != null && t1.getHabitEventDate() != null) {
                    return t1.getHabitEventDate().compareTo(habitEvent.getHabitEventDate());
                } else {
                    return 0;
                }
            });
            timelineListAdapter = new TimelineListAdapter(getActivity(), habitEventsList);
            timelineListView.setAdapter(timelineListAdapter);
        }

        timelineListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get the habit event from clicked, and its associated habit
                // to prepare it to be sent to view habit event fragment
                HabitEvent hEvent = (HabitEvent) adapterView.getItemAtPosition(i);
                Habit habit = UserController.getHabit(hEvent.getParentHabitId());

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
}