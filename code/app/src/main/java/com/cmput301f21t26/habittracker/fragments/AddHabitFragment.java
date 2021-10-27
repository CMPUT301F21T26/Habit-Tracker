package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentAddHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;

public class AddHabitFragment extends Fragment {

    private final String TAG = "AddHabitFragment";
    private Button confirmAddHabitButton;
    private boolean dayList[] = new boolean[7];
    private ChipGroup chipGroup;
    private FragmentAddHabitBinding binding;

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

        chipGroup = binding.chipGroup;
        confirmAddHabitButton = binding.confirmAddHabitButton;

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        confirmAddHabitButton.setOnClickListener(confirmOnClickListener);

    }

    private View.OnClickListener confirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //make sure user clicks on confirm button before storing data into firebase
            final String title = binding.habitTitleET.getText().toString();
            final String reason = binding.habitReasoningET.getText().toString();
            Date date = null;       // TODO add date

            Habit newHabit = null;

            //parameters to handle the chips in chip group
            int chipCount = chipGroup.getChildCount();

            // If user presses confirm then go through all the chips in the group and
            // based on if they are selected, update the booleans in dayList to match
            for (int i=0; i<chipCount; i++) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked()) {
                    dayList[i] = true;
                }
            }

            newHabit = new Habit(title, reason);        // TODO date
            storeData(newHabit);
        }
    };

    public void storeData(Habit habit){

        // mStore.collection("users").document();
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
}