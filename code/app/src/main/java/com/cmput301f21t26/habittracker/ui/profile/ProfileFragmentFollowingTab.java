package com.cmput301f21t26.habittracker.ui.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.interfaces.UserListCallback;
import com.cmput301f21t26.habittracker.objects.OtherUserController;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;

import java.util.ArrayList;

/**
 * handles showing of following tab's listview
 */
public class ProfileFragmentFollowingTab extends Fragment {
    private User userObject;
    private ImageView lockFollowingImageView;
    private TextView followToSeeFollowingTV;
    private ListView profileFollowingListView;
    private UserListAdapter userListAdapter;
    private NavController navController;
    private OtherUserController otherUserController;
    private ArrayList<User> usersList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get the user to instantiate this fragment with
        ProfileFragment parentProfileFrag = (ProfileFragment) getParentFragment();
        userObject = parentProfileFrag.getOtherUser();
        usersList = new ArrayList<>();
        otherUserController = OtherUserController.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_following_tab, container, false);
        lockFollowingImageView = view.findViewById(R.id.lockFollowingImageView);
        followToSeeFollowingTV = view.findViewById(R.id.followToSeeFollowingTV);
        profileFollowingListView = view.findViewById(R.id.profileFollowingListView);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        updateListView();

        return view;
    }

    /**
     * Updates the list view for cases when the current user
     * is granted permission to view the other user's following
     */
    private void updateListView() {
        if (userObject.getUid().equals(UserController.getCurrentUserId())
                || UserController.getCurrentUser().isFollowing(userObject)) {
            updateFollowToSeeFollowingText();

            if (getActivity() != null) {
                userListAdapter = new UserListAdapter(getActivity(), usersList);
                profileFollowingListView.setAdapter(userListAdapter);
            }

            // When clicking, get their habit lists which stores it in the user object, then go to user profile
            profileFollowingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    User otherUser = (User) adapterView.getItemAtPosition(i);
                    otherUserController.getHabitList(otherUser, updatedOtheruser -> {
                        NavDirections action = MobileNavigationDirections.actionGlobalNavigationProfile(updatedOtheruser);
                        navController.navigate(action);
                    });
                }
            });

            // get list of users from user's following list and display it
            otherUserController.getUsersList((ArrayList<String>) userObject.getFollowings(), new UserListCallback() {
                @Override
                public void onCallback(ArrayList<User> listOfUsers) {
                    usersList = listOfUsers;
                    Log.d("ProfileFollowingTab", usersList.toString());
                    userListAdapter = new UserListAdapter(getActivity(), usersList);
                    profileFollowingListView.setAdapter(userListAdapter);
                }
            });

        } else {
            profileFollowingListView.setAdapter(null);
            updateFollowToSeeFollowingText();
        }
    }


    /**
     * Reveals the text "FOLLOW THIS USER TO SEE THEIR FOLLOWING"
     * if user is not following, shown otherwise.
     */
    private void updateFollowToSeeFollowingText() {
        if (!userObject.getUid().equals(UserController.getCurrentUserId())) {
            if (UserController.getCurrentUser().isFollowing(userObject)) {
                lockFollowingImageView.setVisibility(View.GONE);
                followToSeeFollowingTV.setVisibility(View.GONE);
            } else {
                lockFollowingImageView.setVisibility(View.VISIBLE);
                followToSeeFollowingTV.setVisibility(View.VISIBLE);
            }

        }
    }
}