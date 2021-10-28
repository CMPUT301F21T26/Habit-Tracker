package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentAddHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddHabitFragment extends Fragment {

    private final String TAG = "AddHabitFragment";
    private final String datePattern = "yyyy-MM-dd";
    private final String defaultDate = "YYYY-MM-DD";

    private NavController navController;
    private Button confirmAddHabitButton;
    private Button chooseDateButton;
    private ArrayList<Boolean> daysList;
    private ChipGroup chipGroup;
    private FragmentAddHabitBinding binding;
    private SwitchCompat privacySwitch;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private String username;

    /**
     * Required empty constructor
     */
    public AddHabitFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddHabitBinding.inflate(inflater, container, false);

        daysList = new ArrayList<>();       // init everything to false
        chipGroup = binding.chipGroup;
        confirmAddHabitButton = binding.confirmAddHabitButton;
        chooseDateButton = binding.chooseDateButton;
        privacySwitch = binding.privacySwitch;
        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        MaterialDatePicker<Long> datePicker;
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String selectedDateStr = datePicker.getHeaderText();
            SimpleDateFormat fromDatePicker = new SimpleDateFormat("MMM dd, yyyy", Locale.ROOT);
            SimpleDateFormat toUser = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
            try {
                String stringDate = toUser.format(fromDatePicker.parse(selectedDateStr));
                binding.dateFormatMessage.setText(stringDate);
            } catch (ParseException e) {
                binding.dateFormatMessage.setText(defaultDate);
            }
        });

        chooseDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePicker.show(requireActivity().getSupportFragmentManager(), "start date");
            }
        });

        confirmAddHabitButton.setOnClickListener(confirmOnClickListener);
    }

    private View.OnClickListener confirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Habit newHabit = null;
            boolean isPrivate = false;

            // make sure user clicks on confirm button before storing data into firebase
            final String title = binding.habitTitleET.getText().toString();
            final String reason = binding.habitReasoningET.getText().toString();
            final String dateStr = binding.dateFormatMessage.getText().toString();


            SimpleDateFormat format = new SimpleDateFormat(datePattern);
            Date date;       // TODO add date

            try {
                date = format.parse(dateStr);
            } catch (ParseException e) {
                // if invalid date or no date is entered, set the date to right now;
                date = Calendar.getInstance().getTime();
            }

            // parameters to handle the chips in chip group
            int chipCount = chipGroup.getChildCount();

            // If user presses confirm then go through all the chips in the group and
            // based on if they are selected, update the booleans in daysList to match
            for (int i=0; i<chipCount; i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                daysList.add(chip.isChecked());
            }

            if(privacySwitch.isChecked()){
                isPrivate = true;
            }
            assert daysList.size() == 7;
            newHabit = new Habit(title, reason, date, daysList);
            newHabit.setPrivate(isPrivate);

            storeHabitInDb(newHabit);

            clearEditTexts();

            NavDirections direction = MobileNavigationDirections.actionGlobalTodaysHabits(null);
            navController.navigate(direction);      // go to TodayHabitFragment
        }
    };

    public void storeHabitInDb(Habit habit){
        mStore.collection("users").document(username).collection("habits")
                .document(habit.getHabitId())
                .set(habit);
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

    public void clearEditTexts() {
        binding.habitTitleET.setText("");
        binding.habitReasoningET.setText("");
        binding.dateFormatMessage.setText(defaultDate);
    }
}
