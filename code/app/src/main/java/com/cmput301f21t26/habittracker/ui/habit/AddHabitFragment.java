package com.cmput301f21t26.habittracker.ui.habit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentAddHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddHabitFragment extends Fragment {

    private final String TAG = "AddHabitFragment";
    private final String datePattern = "yyyy-MM-dd";
    private String defaultDate;

    private FragmentAddHabitBinding binding;
    private NavController navController;

    private Button confirmAddHabitButton;
    private Button chooseDateButton;
    private ChipGroup chipGroup;
    private SwitchCompat privacySwitch;
    private TextView dateFormatMessageTV;
    private EditText habitTitleET;
    private EditText habitReasoningET;

    /**
     * No-argument empty constructor
     */
    public AddHabitFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddHabitBinding.inflate(inflater, container, false);

        chipGroup = binding.chipGroup;
        confirmAddHabitButton = binding.confirmAddHabitButton;
        chooseDateButton = binding.chooseDateButton;
        privacySwitch = binding.privacySwitch;
        dateFormatMessageTV = binding.dateFormatMessage;
        habitTitleET = binding.habitTitleET;
        habitReasoningET = binding.habitReasoningET;
        defaultDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        // Set the date format message to the current date
        dateFormatMessageTV.setText(defaultDate);

        // Build our Date Picker
        MaterialDatePicker<Long> datePicker;
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        // When date is set
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                SimpleDateFormat toUser = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
                // Issue with correct timezone
                // Ref: https://stackoverflow.com/questions/58931051/materialdatepicker-get-selected-dates
                toUser.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date selectedDate = new Date(selection);
                // Now set our date format message
                dateFormatMessageTV.setText(toUser.format(selectedDate));
                Log.d(TAG, "Date is " + selectedDate.toString());
            }
        });

        chooseDateButton.setOnClickListener(v -> datePicker.show(requireActivity().getSupportFragmentManager(), "start date"));

        confirmAddHabitButton.setOnClickListener(confirmOnClickListener);
    }

    /**
     * When user clicks the confirm button
     */
    private View.OnClickListener confirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (checkFieldsFilled()) {

                Habit newHabit;
                boolean isPrivate;
                ArrayList<Integer> daysList = new ArrayList<>();

                // make sure user clicks on confirm button before storing data into firebase
                final String title = habitTitleET.getText().toString();
                final String reason = habitReasoningET.getText().toString();
                final String dateStr = dateFormatMessageTV.getText().toString();

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
                    if (chip.isChecked()) {
                        daysList.add(i);
                    }
                }

                isPrivate = privacySwitch.isChecked();

                newHabit = new Habit(title, reason, date, daysList);
                newHabit.setPrivate(isPrivate);

                Log.d(TAG, newHabit.getReason());
                UserController.storeHabitInDb(newHabit, cbUser -> {
                    // Goes back to previous fragment user was in
                    navController.popBackStack();
                });
            }
        }
    };

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
     * Checks all the fields and makes sure
     * they are not empty (except for choose date)
     */
    public boolean checkFieldsFilled() {
        boolean filled = true;
        final String title = habitTitleET.getText().toString();
        final String reason = habitReasoningET.getText().toString();

        if (title.isEmpty()) {
            habitTitleET.setError("Title cannot be empty");
            filled = false;
        }
        if (reason.isEmpty()) {
            habitReasoningET.setError("Reasoning cannot be empty");
            filled = false;
        }

        // Check if there is any checked chips
        boolean checked = false;
        for (int i=0; i<chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                checked = true;
            }
        }

        if (!checked) {
            Toast.makeText(getActivity(), "Must choose at least one day in week for habit to occur!", Toast.LENGTH_LONG).show();
            filled = false;
        }

        return filled;
    }
}
