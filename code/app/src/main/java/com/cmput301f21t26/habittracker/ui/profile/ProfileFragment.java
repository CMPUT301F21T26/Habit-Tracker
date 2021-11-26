package com.cmput301f21t26.habittracker.ui.profile;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentProfileBinding;
import com.cmput301f21t26.habittracker.objects.FollowRequestController;
import com.cmput301f21t26.habittracker.objects.FollowStatus;
import com.cmput301f21t26.habittracker.objects.OtherUserController;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.MainActivity;
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
    private Uri imageUri;
    private Uri newImageUri;
    private String picturePath;
    private String profileImageUrl;
    private User otherUser;
    private User currentUser;

    private Button followButton;
    private boolean isFollowButtonClicked;

    private FollowRequestController followRequestController;
    private OtherUserController otherUserController;

    private FollowStatus followStatus;

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

        isFollowButtonClicked = false;

        // Get users
        currentUser = UserController.getCurrentUser();
        otherUser = ProfileFragmentArgs.fromBundle(getArguments()).getUser();
        
        followRequestController = FollowRequestController.getInstance();
        otherUserController = OtherUserController.getInstance();

        // otherUser dne i.e we want to view the current user's profile
        if (otherUser == currentUser) {
            UserController.addObserverToCurrentUser(this);
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

            otherUserController.getFollowStatusOfCurrentUserTo(otherUser, initStatus -> {

                followStatus = initStatus;

                setFollowButton(followStatus);

                followButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (followStatus == FollowStatus.NOT_FOLLOWING) {
                            // user sends follow request, change button to pending
                            setFollowButton(FollowStatus.PENDING);
                            if (!isFollowButtonClicked) {
                                isFollowButtonClicked = true;
                                followRequestController.sendFollowRequest(otherUser, user -> {
                                    followStatus = FollowStatus.PENDING;
                                    isFollowButtonClicked = false;
                                });
                            }
                        } else if (followStatus == FollowStatus.PENDING) {
                            // cancelled follow request, change button back to follow
                            setFollowButton(FollowStatus.NOT_FOLLOWING);
                            if (!isFollowButtonClicked) {
                                isFollowButtonClicked = true;
                                followRequestController.getFollowRequestSentBy(currentUser, otherUser, followRequest -> {
                                    followRequestController.undoFollowRequest(followRequest, user -> {
                                        followStatus = FollowStatus.NOT_FOLLOWING;
                                        isFollowButtonClicked = false;
                                    });
                                });
                            }
                        } else {
                            // Ask user to confirm unfollow
                            if (!isFollowButtonClicked) {
                                isFollowButtonClicked = true;

                                AlertDialog dialog = new AlertDialog.Builder(getContext())
                                        .setTitle("Unfollow " + otherUser.getUsername())
                                        .setMessage("Are you sure you want unfollow " + otherUser.getUsername() + "?")
                                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // update DB
                                                followRequestController.unfollow(otherUser.getUid(), user -> {
                                                    setFollowButton(FollowStatus.NOT_FOLLOWING);
                                                    followStatus = FollowStatus.NOT_FOLLOWING;
                                                    // reset the tabs and viewpager so then user cannot
                                                    // view the other user's habits/followers/following after unfollowing
                                                    setTabsAndViewPager();
                                                    isFollowButtonClicked = false;
                                                });
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                isFollowButtonClicked = false;
                                                dialog.cancel();
                                            }
                                        })
                                        .show();

                                // Change buttons and background
                                dialog.getWindow().setBackgroundDrawableResource(R.drawable.notif_panel_background);
                                Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                                layoutParams.weight = 10;
                                btnPositive.setLayoutParams(layoutParams);
                                btnNegative.setLayoutParams(layoutParams);
                                btnPositive.setTypeface(getResources().getFont(R.font.rubik_black));
                                btnNegative.setTypeface(getResources().getFont(R.font.rubik_black));
                                btnPositive.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                                btnNegative.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                            }

                        }
                    }
                });
            });



        } else {
            // The other user is the current user
            setFields(otherUser);
            setProfilePicImageView(otherUser);
            followButton.setVisibility(View.INVISIBLE);
            // When user clicks profile picture, change it
            profilePic.setOnClickListener(view1 -> {
                mGetContent.launch("image/*");  // launch file explorer
            });
        }
    }

    /**
     * Given a follow status, will change the follow button accordingly
     * @param initStatus the {@link FollowStatus} of the follow request.
     */
    private void setFollowButton(FollowStatus initStatus) {
        if (initStatus == FollowStatus.NOT_FOLLOWING) {
            followButton.setText("FOLLOW");
            followButton.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            followButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green));
        } else if (initStatus == FollowStatus.PENDING) {
            followButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.lighter_gray));
            followButton.setText("REQUESTED");
        } else {
            followButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.dark_blue));
            followButton.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            followButton.setText("FOLLOWING");
        }
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
        viewPager.setOffscreenPageLimit(2);     // so we're not creating new pages constantly
        tabLayout.removeAllTabs();
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
                        .placeholder(R.drawable.default_profile_pic)
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

    public User getAttachedUser() {
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
            // Add back button
            if (((MainActivity) getActivity()) != null) {
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (otherUser != currentUser) {
            MainActivity.hideBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
            // Add back button
            if (((MainActivity) getActivity()) != null) {
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (otherUser != currentUser) {
            MainActivity.showBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
            // remove back button
            if (((MainActivity) getActivity()) != null) {
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
            }

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