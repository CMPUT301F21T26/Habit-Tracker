package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentAddHabitBinding;
import com.cmput301f21t26.habittracker.databinding.FragmentEditHabitBinding;
import com.cmput301f21t26.habittracker.databinding.FragmentEditHabitEventBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditHabitEventFragment extends Fragment {

    private final String TAG = "EditHabitEventFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String username;

    private FragmentEditHabitEventBinding binding;
    private Button delBtn;
    private Button editConfirmBtn;
    private TextInputEditText commentET;
    private TextView habitEventDateFormatTV;
    private TextView habitEventTitleTV;
    private TextView habitEventLocationTV;
    private EditText habitEventCommentET;

    private NavController navController;

    private Habit habit;
    private HabitEvent hEvent;

    public EditHabitEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();

        habit = EditHabitEventFragmentArgs.fromBundle(getArguments()).getHabit();
        hEvent = EditHabitEventFragmentArgs.fromBundle(getArguments()).getHabitEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditHabitEventBinding.inflate(inflater, container, false);

        delBtn = binding.deleteHabitEventButton;
        editConfirmBtn = binding.confirmHabitEventButton;
        commentET = binding.habitEventCommentET;
        habitEventDateFormatTV = binding.habitEventDateFormatTV;
        habitEventTitleTV = binding.editHabitEventTitleTV;
        habitEventCommentET = binding.habitEventCommentET;
        habitEventLocationTV = binding.habitEventLocationTV;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        editConfirmBtn.setOnClickListener(editConfirmOnClickListener);
        delBtn.setOnClickListener(deleteOnClickListener);

        setEditHabitEventFields();
    }

    /**
     * Sets the fields of the edit habit event fragment views
     * to the proper values given by the habit event object
     */
    private void setEditHabitEventFields() {
        // Get date and set it to TextView
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
        String habitEventDateFormat = format.format(hEvent.getHabitEventDate());
        habitEventDateFormatTV.setText(habitEventDateFormat);

        // Temporary...don't know if title should be this.
        habitEventTitleTV.setText(hEvent.getTitle());

        habitEventCommentET.setText(hEvent.getComment());

        if (hEvent.getLocation() != null) {
            habitEventLocationTV.setText(hEvent.getLocation().toString());
        }

        if (hEvent.getPhotoUrl() != null) {
            // TODO set image view to the image given by habit event
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

    private View.OnClickListener editConfirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String comment = commentET.getText().toString();

            hEvent.setComment(comment);
            // TODO get location, photograph from the user


            DocumentReference userRef = mStore.collection("users").document(username);
            DocumentReference habitRef = userRef.collection("habits").document(habit.getHabitId());
            habitRef.collection("habitEvents")
                    .document(hEvent.getHabitEventId())
                    .update(
                            "comment", comment,
                            "location", null
                            // TODO photograph
                    )
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "Habit event successfully updated!");

                            NavDirections action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                            navController.navigate(action);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating habit event", e);
                        }
                    });
        }
    };

    private View.OnClickListener deleteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DocumentReference userRef = mStore.collection("users").document(username);
            DocumentReference habitRef = userRef.collection("habits").document(habit.getHabitId());
            habitRef.collection("habitEvents")
                    .document(hEvent.getHabitEventId())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Habit event succesfully deleted!");

                            NavDirections action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                            navController.navigate(action);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting habit event", e);
                        }
                    });
        }
    };
}