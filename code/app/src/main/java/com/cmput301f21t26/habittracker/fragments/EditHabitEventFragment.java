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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditHabitEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditHabitEventFragment extends Fragment {

    private final String TAG = "EditHabitEventFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String username;

    private FragmentEditHabitEventBinding binding;
    private Button delBtn;
    private Button editConfirmBtn;
    private TextInputEditText commentET;

    private NavController navController;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Habit habit;
    private HabitEvent hEvent;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditHabitEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditHabitEventFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditHabitEventFragment newInstance(String param1, String param2) {
        EditHabitEventFragment fragment = new EditHabitEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

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

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        editConfirmBtn.setOnClickListener(editConfirmOnClickListener);
        delBtn.setOnClickListener(deleteOnClickListener);

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
                            NavDirections direction = MobileNavigationDirections.actionGlobalTodaysHabits(null);
                            navController.navigate(direction);      // navigate to TodayHabitFragment
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
                            NavDirections direction = MobileNavigationDirections.actionGlobalTodaysHabits(null);
                            navController.navigate(direction);      // navigate to TodayHabitFragment
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