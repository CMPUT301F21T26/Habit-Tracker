package com.cmput301f21t26.habittracker.ui.habit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentViewHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ViewHabitFragment extends Fragment {

    final private String TAG = "viewHabitFragment";

    private @NonNull FragmentViewHabitBinding binding;
    private NavController navController;

    private Habit habit;

    private Button confirmHabitButton;
    private Button editHabitButton;
    private ChipGroup chipGroup;

    public ViewHabitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentViewHabitBinding.inflate(inflater, container, false);

        chipGroup = binding.chipGroup;
        confirmHabitButton = binding.confirmHabitButton;
        editHabitButton = binding.editHabitButton;

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        assert getArguments() != null;
        String habitId = getArguments().getString("habitId");
        // grabs habit and updates screen with this information
        habit = UserController.getHabit(habitId);
        updateScreen();
        // make the confirm button and edit buttons clickable
        // confirm first
        confirmHabitButton.setOnClickListener(v -> {
            // Go back to previous place user was in
            navController.popBackStack();
        });
        // edit button
        editHabitButton.setOnClickListener(v -> {
            Log.d(TAG, "clicked edit habit button");
            Bundle bundle = new Bundle();
            bundle.putString("habitId", habitId);
            navController.navigate(R.id.editHabitFragment, bundle);
        });
    }

    /**
     * Hides menu items in add habit fragment
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity.hideMenuItems(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.hideBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.showBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
    }

    private void updateScreen(){
        Log.d(TAG, "habit is " + habit);
        // habit is retrieved, now populate the View with information

        binding.habitTitle.setText(habit.getTitle());
        binding.habitReasoning.setText(habit.getReason());

        // TODO set percentage done

        // get the date and make it ready for presentation
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
        String date = format.format(habit.getStartDate());
        binding.dateFormatMessage.setText(date);

        // set days of the week
        ArrayList<Integer> chips = habit.getDaysList();
        for (int i=0; i<chips.size(); i++){
            Chip chip = (Chip) chipGroup.getChildAt(chips.get(i));
            chip.setChecked(true);
        }
    }
}