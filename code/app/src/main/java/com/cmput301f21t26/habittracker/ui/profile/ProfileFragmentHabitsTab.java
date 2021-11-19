package com.cmput301f21t26.habittracker.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Observable;
import java.util.Observer;


public class ProfileFragmentHabitsTab extends Fragment implements Observer {
    private String TAG = "ProfileFragmentHabitsTab";

    private NavController navController;

    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;
    private RecyclerView mRecyclerView;
    private HabitAdapter.RecyclerViewClickListener rvlistener;
    private User userObject;
    private ImageView lockHabitsImageView;
    private TextView followToSeeHabitsTV;

    public ProfileFragmentHabitsTab() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the user to instantiate this fragment with
        ProfileFragment parentProfileFrag = (ProfileFragment) getParentFragment();
        userObject = parentProfileFrag.getOtherUser();

        UserController.addObserverToCurrentUser(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_habits_tab, container, false);

        mRecyclerView = view.findViewById(R.id.profileHabitsRecyclerView);
        lockHabitsImageView = view.findViewById(R.id.lockHabitsImageView);
        followToSeeHabitsTV = view.findViewById(R.id.followToSeeHabitTV);

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

        updateRecyclerView();
    }

    @Override
    public void update(Observable observable, Object o) {
        updateRecyclerView();
    }

    /**
     * Updates the recycler view for cases when the current user
     * is granted permission to view the other user's public habits
     */
    private void updateRecyclerView() {
        if (userObject.getUid().equals(UserController.getCurrentUserId())
                || UserController.getCurrentUser().isFollowing(userObject)) {

            // show habits only when the userObject is equal to current user
            // or current user is following userObject
            habitAdapter = new HabitAdapter(habitList, getActivity(), rvlistener);

            // Set ItemTouchHelper to allow rearranging habits
            HabitItemTouchHelper habitItemTouchHelper = new HabitItemTouchHelper(habitAdapter);
            if (UserController.getCurrentUser().isFollowing(userObject)) {
                // don't allow current user to drag and drop other user's habits
                habitItemTouchHelper.setDraggable(false);
            }
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(habitItemTouchHelper);
            habitAdapter.setItemTouchHelper(itemTouchHelper);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);

            // display today habits
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(habitAdapter);

            // Hide checkbox
            habitAdapter.checkBoxVisibility(View.GONE);

        } else {
            updateFollowToSeeHabitText();
        }
    }

    /**
     * Reveals the text "FOLLOW THIS USER TO SEE THEIR HABITS"
     * if user is not following, shown otherwise.
     */
    private void updateFollowToSeeHabitText() {
        // If the passed in user object is not the current user and
        // the current user is following the user they are following, update
        // the "FOLLOW THIS USER TO SEE THEIR HABITS" text
        if (!userObject.getUid().equals(UserController.getCurrentUserId())) {
            if (UserController.getCurrentUser().isFollowing(userObject)) {
                lockHabitsImageView.setVisibility(View.GONE);
                followToSeeHabitsTV.setVisibility(View.GONE);
            } else {
                lockHabitsImageView.setVisibility(View.VISIBLE);
                followToSeeHabitsTV.setVisibility(View.VISIBLE);
            }

        }
    }
}