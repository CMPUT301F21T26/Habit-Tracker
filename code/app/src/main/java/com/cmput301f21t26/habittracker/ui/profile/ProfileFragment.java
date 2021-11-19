package com.cmput301f21t26.habittracker.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.objects.FollowRequest;
import com.cmput301f21t26.habittracker.objects.FollowRequestController;
import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentProfileBinding;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.tabs.TabLayout;

import java.util.Observable;
import java.util.Observer;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements Observer {
    private String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfileFragmentAdapter fragmentAdapter;
    private TextView usernameTV;
    private TextView fullNameTV;
    private TextView followersTV;
    private TextView followingTV;
    private CircleImageView profilePic;
    private Button followButton;
    private Uri imageUri;
    private Uri newImageUri;
    private String picturePath;
    private String profileImageUrl;
    private User otherUser;
    private User currentUser;

    private FollowRequestController followRequestController;
    
    /**
     * Creates a new profile fragment for when viewing other
     * user profiles.
     * @param otherUser
     *  The other user's User object, of type {@link User}
     * @return
     *  The newly created profile fragment, of type {@link Fragment}
     */
    public static ProfileFragment newInstance(User otherUser) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("otherUser", otherUser);
        profileFragment.setArguments(bundle);
        return new ProfileFragment();
    }

    /**
     * Creates a new profile fragment for the current user.
     * @return
     * The newly created profile fragment, of type {@link Fragment}
     */
    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setHasOptionsMenu(true);

        // Initiate views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        usernameTV = view.findViewById(R.id.usernameTV);
        fullNameTV = view.findViewById(R.id.fullNameTV);
        profilePic = view.findViewById(R.id.profilePicImageView);
        followersTV = view.findViewById(R.id.followersNumberTV);
        followingTV = view.findViewById(R.id.followingNumberTV);
        followButton = view.findViewById(R.id.followButton);

        // Get users
        currentUser = UserController.getCurrentUser();
        otherUser = ProfileFragmentArgs.fromBundle(getArguments()).getUser();
        
        followRequestController = FollowRequestController.getInstance();

        // otherUser dne i.e we want to view the current user's profile
        if (otherUser == currentUser) {
            UserController.addObserverToCurrentUser(this);
        } else {

        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the profile tab layout and viewpager
        setTabsAndViewPager();

        // fill up user info on view created
        if (!otherUser.getUsername().equals(currentUser.getUsername())) {
            // When the inputted userObject is not the current user (is the other user)
            setFields(otherUser);
            setProfilePicImageView(otherUser);
            int initState = getInitialFollowButtonState();

            if (initState == 0) {
                followButton.setText("FOLLOW");
            } else if (initState == 1) {
                followButton.setText("REQUESTED");
            } else {
                followButton.setText("FOLLOWING");
            }

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    followRequestController.storeFollowRequestInDb(currentUser, otherUser, unused -> {
                        if (initState == 0) {
                            // sent a follow request
                            followButton.setText("REQUESTED");
                        } else if (initState == 1) {
                            // cancelled request
                            followButton.setText("FOLLOW");
                        } else {
                            // unfollow
                            followButton.setText("FOLLOW");
                        }
                    });
                }
            });

            // Add back button
            if (((MainActivity) getActivity()) != null) {
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        } else {
            setFields(otherUser);
            setProfilePicImageView(otherUser);
            followButton.setVisibility(View.INVISIBLE);
            // When user clicks profile picture, change it
            profilePic.setOnClickListener(view1 -> {
                mGetContent.launch("image/*");  // launch file explorer
            });
        }
    }

    private int getInitialFollowButtonState() {

        if (currentUser.isFollowing(otherUser)) {
            /*
            if (UserController.hasCurrUserSentFollowRequestTo(otherUser)) {
                return 1;   // 1: sent a follow request
            }
            */
            return 2;   // 2: following
        }
        return 0;      // 0: not following (default)
    }

    /**
     * Sets up the tab layout and viewpager
     * located in the profile fragment.
     */
    private void setTabsAndViewPager() {
        // Set up profile tablayout and viewpager
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentAdapter = new ProfileFragmentAdapter(fragmentManager, getLifecycle());
        viewPager.setAdapter(fragmentAdapter);

        tabLayout.addTab(tabLayout.newTab().setText("Habits"));
        tabLayout.addTab(tabLayout.newTab().setText("Followers"));
        tabLayout.addTab(tabLayout.newTab().setText("Following"));

        // Change view pager when tab selected
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        // Change tabs when view pager changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    /**
     * Gets the info from the User object and sets
     * the TextViews accordingly
     * @param userObject
     * The user object to get the info from {@link User}
     */
    private void setFields(User userObject) {
        // Set fields
        fullNameTV.setText(userObject.getFirstName() + " " + userObject.getLastName());
        usernameTV.setText(userObject.getUsername());
        int followersNumber = userObject.getFollowers().size();
        int followingNumber = userObject.getFollowings().size();
        followersTV.setText(String.valueOf(followersNumber));
        followingTV.setText(String.valueOf(followingNumber));
    }

    /**
     * Sets the profile picture to the image
     * referenced in the pictureURL attribute
     * in the user's data in the database.
     * @param userObject
     *  The user object to get the profile picture from {@link User}
     */
    private void setProfilePicImageView(User userObject) {
        // Set the profile picture
        profileImageUrl = userObject.getPictureURL();
        if (profileImageUrl != null) {
            // Without Glide there will be bad bitmap error
            // Ref: https://stackoverflow.com/questions/3681714/bad-bitmap-error-when-setting-uri/58268202
            if (getActivity() != null) {
                Glide.with(getActivity())
                        .load(profileImageUrl)
                        .into(profilePic);
                Log.d(TAG, "Get profile picture success");
            }
        } else {
            // if for whatever reason the picture path is null
            // make the profile picture the default one
            imageUri = Uri.parse("android.resource://com.cmput301f21t26.habittracker/drawable/default_profile_pic");
            picturePath = "image/" + imageUri.hashCode() + ".jpeg";

            UserController.updateProfilePicInDb(picturePath, imageUri, user -> {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).setProfileIconToProfilePic(profileImageUrl);
                }
            });
        }
    }

    /**
     * Functions essentially like the now deprecated onActivityResult.
     * When User clicks on the circle image view, their default
     * file explorer pops up, and once they select an image,
     * onActivityResult here notices, and sets imageUri to
     * the image selected. It then calls changeProfilePic
     * to change the profile pic.
     */
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Make sure uri not null; uri null can occur when we click to go to file explorer and press the back button without choosing an image
                    if (uri != null) {
                        newImageUri = uri;
                        if (newImageUri != imageUri && newImageUri != null) {
                            picturePath = "image/" + newImageUri.hashCode() + ".jpeg";
                            profilePic.setImageURI(newImageUri);
                            imageUri = newImageUri;

                            UserController.updateProfilePicInDb(picturePath, imageUri, user -> {
                                if (getActivity() != null) {
                                   ((MainActivity) getActivity()).setProfileIconToProfilePic(profileImageUrl);
                                }
                            });
                        }
                    }
                }
            });

    public User getOtherUser() {
        return otherUser;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        UserController.deleteObserverFromCurrentUser(this);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (otherUser != currentUser) {
            MainActivity.hideMenuItems(menu);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (otherUser != currentUser) {
            MainActivity.hideBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (otherUser != currentUser) {
            MainActivity.showBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        // TODO what's this for?
        // set our info into the textViews and profile pic
        // and only update if the user we're viewing
        // is the current user (because other users don't have
        // a listener)
        if (otherUser == currentUser) {
            setFields(otherUser);
            setProfilePicImageView(otherUser);
        }
    }
}