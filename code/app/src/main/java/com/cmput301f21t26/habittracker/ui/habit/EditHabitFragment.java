package com.cmput301f21t26.habittracker.ui.habit;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.objects.HabitController;
import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentEditHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditHabitFragment extends Fragment {

    final private String TAG = "editHabitFragment";
    private final String datePattern = "yyyy-MM-dd";
    private final String defaultDate = "YYYY-MM-DD";

    private ArrayList<Integer> daysList;

    private @NonNull FragmentEditHabitBinding binding;

    private Button confirmHabitButton;
    private Button deleteHabitButton;
    private ChipGroup chipGroup;
    private NavController navController;
    private EditText habitTitleET;
    private EditText habitReasoningET;
    private TextView dateFormatMessageTV;
    private Button chooseDateButton;
    private SwitchCompat privacySwitch;

    private Habit habit;

    private HabitController habitController;

    public EditHabitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        habitController = HabitController.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditHabitBinding.inflate(inflater, container, false);

        daysList = new ArrayList<>();       // init everything to false
        chipGroup = binding.chipGroup;
        confirmHabitButton = binding.confirmHabitButton;
        deleteHabitButton = binding.deleteHabitButton;
        habitTitleET = binding.habitTitleET;
        habitReasoningET = binding.habitReasoningET;
        dateFormatMessageTV = binding.dateFormatMessage;
        chooseDateButton = binding.chooseDateButton;
        privacySwitch = binding.privacySwitch;

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

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

        // grabs habit and updates screen with this information
        habit = EditHabitFragmentArgs.fromBundle(getArguments()).getHabit();
        updateScreen();

        // make the confirm button and delete buttons clickable
        // confirm first
        confirmHabitButton.setOnClickListener(confirmOnClickListener);
        // delete button
        deleteHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prompt user to approve
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete this habit? The data will be lost forever.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // update DB
                            habitController.removeHabitFromDb(habit, cbUser -> {
                                // go to previous fragment user was in
                                navController.popBackStack();       // this goes back to view habit fragment
                                navController.popBackStack();
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

                // Change buttons and background
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.notif_panel_background);
                Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);
                btnPositive.setTypeface(getResources().getFont(R.font.rubik_black));
                btnNegative.setTypeface(getResources().getFont(R.font.rubik_black));
                btnPositive.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                btnNegative.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            }
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

        habitTitleET.setText(habit.getTitle());
        habitReasoningET.setText(habit.getReason());

        // TODO set percentage done

        // get the date and make it ready for presentation
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
        String date = format.format(habit.getStartDate());
        dateFormatMessageTV.setText(date);

        // set days of the week
        ArrayList<Integer> chips = habit.getDaysList();
        for (int i=0; i<chips.size(); i++){
            Chip chip = (Chip) chipGroup.getChildAt(chips.get(i));
            chip.setChecked(true);
        }

        // set the privacy switch
        if (habit.isPrivate()) {
            privacySwitch.setChecked(habit.isPrivate());
        }
    }

    private View.OnClickListener confirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkFieldsFilled()) {

                boolean isPrivate = false;

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

                Log.d(TAG, "The days list is " + daysList.toString());
                if(privacySwitch.isChecked()) {
                    isPrivate = true;
                }

                habit.setTitle(title);
                habit.setDaysList(daysList);
                habit.setReason(reason);
                habit.setStartDate(date);
                habit.setPrivate(isPrivate);

                habitController.updateHabitInDb(habit, cbUser -> {
                    // go to previous fragment user was in
                    navController.popBackStack();       // this goes back to view habit fragment
                    navController.popBackStack();
                });
            }
        }
    };

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