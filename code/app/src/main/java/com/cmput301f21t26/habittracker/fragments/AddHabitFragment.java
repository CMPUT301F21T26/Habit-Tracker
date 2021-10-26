package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;

public class AddHabitFragment extends Fragment implements View.OnClickListener{

    private Button confirmHabitButton;
    private FirebaseFirestore mStore;
    private boolean dayList[] = new boolean[7];
    private ChipGroup chipGroup;

    /**
     * required empty public constructor
     */

    public AddHabitFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mStore = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_habit, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chipGroup = view.findViewById(R.id.chipGroup);
        confirmHabitButton = view.findViewById(R.id.confirmHabitButton);
        confirmHabitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){

        //make sure user clicks on confirm button before storing data into firebase

        String reason = "";
        Date date = null;

        //parameters to handle the chips in chip group
        int chipCount = chipGroup.getChildCount();
        int i = 0;
        String msg = "Checked chips are: ";
        if (view.getId() == R.id.confirmHabitButton){

            /* If user presses confirm then go through all the chips in the group and
            based on if they are selected, update the booleans in dayList to match
             */
            while (i<chipCount){
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked() ) {
                    dayList[i] = true;
                    msg += chip.getText().toString() + " ";
                }
                i++;
            }
            //wanted to test if the output is proper, but doesn't seem to be working
            Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();


            //if the user did not give startDate
            if (reason == ""){
                Habit habit = new Habit("", "");
                storeData(habit);
            }
            // if the user did not give us a reason or start date
            if (reason == "" && date == null){
                Habit habit = new Habit("");
                storeData(habit);

            }
            //if the user entered everything
            else{
                Habit habit = new Habit("","", date);
                storeData(habit);
            }
        }
    }

    public void storeData(Habit habit){

        // mStore.collection("users").document();
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