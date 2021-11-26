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
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentViewHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.MainActivity;
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
    private SwitchCompat isPrivateSwitch;

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
        isPrivateSwitch = binding.privacySwitch;

        // Hide edit button if passed in user object is not the owner (i.e. not the current user)
        User userObject = ViewHabitFragmentArgs.fromBundle(getArguments()).getUser();
        if (userObject != UserController.getCurrentUser() &&
                !userObject.getUsername().equals(UserController.getCurrentUser().getUsername())) {           // for the case of viewing one's own profile
            editHabitButton.setVisibility(View.GONE);
        }

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        assert getArguments() != null;
        // grabs habit and updates screen with this information
        habit = ViewHabitFragmentArgs.fromBundle(getArguments()).getHabit();
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
            NavDirections action = MobileNavigationDirections.actionGlobalEditHabitFragment(habit);
            navController.navigate(action);
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

    /**
     * Updates all the views in the screen with the info given by the habit.
     */
    private void updateScreen(){
        Log.d(TAG, "habit is " + habit);
        // habit is retrieved, now populate the View with information

        binding.habitTitle.setText(habit.getTitle());
        binding.habitReasoning.setText(habit.getReason());
        if (habit.isPrivate()) {
            isPrivateSwitch.setChecked(true);
        } else {
            isPrivateSwitch.setChecked(false);
        }


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