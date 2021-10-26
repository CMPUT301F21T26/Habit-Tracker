package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class AddHabitFragment extends Fragment{

    Button confirmButton;
    FirebaseFirestore mStore;




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

    public void onClick(View view){

        //make sure user clicks on confirm button before storing data into firebase

        String reason = "";
        Date date = null;

        if (view.getId() == R.id.confirmHabitButton){

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