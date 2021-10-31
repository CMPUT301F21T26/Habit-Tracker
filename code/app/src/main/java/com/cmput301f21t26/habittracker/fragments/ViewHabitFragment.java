package com.cmput301f21t26.habittracker.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmput301f21t26.habittracker.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentAddHabitBinding;
import com.cmput301f21t26.habittracker.databinding.FragmentViewHabitBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class ViewHabitFragment extends Fragment {

    final private String TAG = "viewHabitFragment";


    private Button confirmHabitButton;
    private Button editHabitButton;
    private ChipGroup chipGroup;
    private @NonNull FragmentViewHabitBinding binding;
    private NavController navController;
    private FirebaseFirestore mStore;
    private String username;

    private Habit habit;




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


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();
        mStore = FirebaseFirestore.getInstance();

        ArrayList<Integer> daysList = new ArrayList<>();       // init everything to false
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
        getHabit(habitId);
        // make the confirm button and edit buttons clickable
        // confirm first
        confirmHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to previous place user was in
                navController.popBackStack();
            }
        });
        // edit button
        editHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked edit habit button");
                Bundle bundle = new Bundle();
                bundle.putString("habitId", habitId);
                navController.navigate(R.id.editHabitFragment, bundle);
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
     * Sets habit to be the habit from the database
     * @param habitID
     *      habitID is necessary to identify in database
     */
    private void getHabit(String habitID){

        DocumentReference doc = mStore.collection("users").document(username).collection("habits").document(habitID);
        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot!=null){
                    habit = documentSnapshot.toObject(Habit.class);
                    Log.d(TAG, "DocSnap data: " + habit);
                    updateScreen();


                } else {
                    Log.d(TAG, "no such document");
                }
            }
        });
    }

    private void updateScreen(){
        Log.d(TAG, "habit is " + habit);
        // habit is retrieved, now populate the View with information

        binding.habitTitle.setText(habit.getTitle());
        binding.habitReasoning.setText(habit.getReason());

        //todo set percentage done

        //get the date and make it ready for presentation
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
        String date = format.format(habit.getStartDate());
        binding.dateFormatMessage.setText(date);

        //set days of the week
        ArrayList<Integer> chips = habit.getDaysList();
        for (int i=0; i<chips.size(); i++){
            Chip chip = (Chip) chipGroup.getChildAt(chips.get(i));
            chip.setChecked(true);
        }

    }


}