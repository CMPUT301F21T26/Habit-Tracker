package com.cmput301f21t26.habittracker.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.habit.HabitAdapter;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.ui.habit.HabitItemTouchHelper;

import java.util.ArrayList;


public class ProfileFragmentHabitsTab extends Fragment {
    private String TAG = "ProfileFragmentHabitsTab";

    private NavController navController;

    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;
    private RecyclerView mRecyclerView;
    private HabitAdapter.RecyclerViewClickListener rvlistener;
    private User userObject;

    public ProfileFragmentHabitsTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProfileFragment parentProfileFrag = (ProfileFragment) getParentFragment();

        userObject = parentProfileFrag.getOtherUser();
        if (parentProfileFrag.getOtherUser().getUid().equals(UserController.getCurrentUserId())) {
            userObject = UserController.getCurrentUser();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_habits_tab, container, false);

        mRecyclerView = view.findViewById(R.id.profileHabitsRecyclerView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        habitList = (ArrayList<Habit>) userObject.getHabits();
        rvlistener = new HabitAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on item at position: " + String.valueOf(position));
                NavDirections action = MobileNavigationDirections.actionGlobalViewHabitFragment(habitList.get(position), userObject);
                navController.navigate(action);
            }
        };

        if (userObject.getUid().equals(UserController.getCurrentUserId())
                || UserController.getCurrentUser().isFollowing(userObject)) {

            // show habits only when the userObject is equal to current user
            // or current user is following userObject
            habitAdapter = new HabitAdapter(habitList, getActivity(), rvlistener);

            // Set ItemTouchHelper to allow rearranging habits
            HabitItemTouchHelper habitItemTouchHelper = new HabitItemTouchHelper(habitAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(habitItemTouchHelper);
            habitAdapter.setItemTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);

            // display today habits
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(habitAdapter);

            // Hide checkbox
            habitAdapter.checkBoxVisibility(View.GONE);

        } else {
            // TODO tell the user you can't view this selected other user's habits
        }
    }
}