package com.cmput301f21t26.habittracker.ui.habitevent;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class ViewHabitEventFragment extends Fragment {
    private String TAG = "ViewHabitEventFragment";

    private NavController navController;
    private Button confirmHabitEventButton;
    private Button editHabitEventButton;
    private TextView habitEventTitleTV;
    private TextView habitEventDateFormatTV;
    private EditText habitEventCommentET;
    private TextView habitEventLocationTV;
    private ImageView habitEventImageView;

    private Habit habit;
    private HabitEvent hEvent;

    public ViewHabitEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        habit = ViewHabitEventFragmentArgs.fromBundle(getArguments()).getHabit();
        hEvent = ViewHabitEventFragmentArgs.fromBundle(getArguments()).getHabitEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_habit_event, container, false);

        // Initiate views
        confirmHabitEventButton = view.findViewById(R.id.confirmHabitEventButton);
        editHabitEventButton = view.findViewById(R.id.editHabitEventButton);
        habitEventTitleTV = view.findViewById(R.id.viewHabitEventTitleTV);
        habitEventDateFormatTV = view.findViewById(R.id.habitEventDateFormatTV);
        habitEventCommentET = view.findViewById(R.id.habitEventCommentET);
        habitEventLocationTV = view.findViewById(R.id.habitEventLocationTV);
        habitEventImageView = view.findViewById(R.id.habitEventImage);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // confirm button clicked
        confirmHabitEventButton.setOnClickListener(v -> {
            NavDirections action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
            navController.navigate(action);
        });
        // edit button
        editHabitEventButton.setOnClickListener(v -> {
            Log.d(TAG, "clicked edit habit event button");
            NavDirections action = MobileNavigationDirections.actionGlobalEditHabitEventFragment(hEvent, habit);
            navController.navigate(action);
        });

        setViewHabitEventFields();
    }

    /**
     * Sets all the fields in view habit fragment
     * to the habit event's info
     */
    private void setViewHabitEventFields() {

        // Get date and set it to TextView
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
        String habitEventDateFormat = format.format(hEvent.getHabitEventDate());
        habitEventDateFormatTV.setText(habitEventDateFormat);

        // Temporary...don't know if title should be this.
        habitEventTitleTV.setText(hEvent.getTitle());

        habitEventCommentET.setText(hEvent.getComment());
        if (hEvent.getAddress() != null) {
            habitEventLocationTV.setText(hEvent.getAddress());
        }

        if (hEvent.getPhotoUrl() == null) {
            habitEventImageView.setImageResource(R.color.transparent);
        }
        else{
            if (getActivity() != null) {
                Glide.with(getActivity())
                        .load(hEvent.getPhotoUrl())
                        .placeholder(R.drawable.default_image)
                        .into(habitEventImageView);
            }
        }
    }

    /**
     * Hides menu items in edit habit event fragment
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
}