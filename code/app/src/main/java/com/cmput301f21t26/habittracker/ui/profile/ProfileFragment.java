package com.cmput301f21t26.habittracker.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
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
    private User userObject;
    private User otherUser = null;

    /**
     * Creates a new profile fragment for when viewing other
     * user profiles.
     * @param otherUser
     *  The other user's User object, of type {@link User}
     * @return
     *  The newly created profile fragment, of type {@link Fragment}
     */
    public ProfileFragment newInstance(User otherUser) {
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
    public ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initiate views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        usernameTV = view.findViewById(R.id.usernameTV);
        fullNameTV = view.findViewById(R.id.fullNameTV);
        profilePic = view.findViewById(R.id.profilePicImageView);
        followersTV = view.findViewById(R.id.followersNumberTV);
        followingTV = view.findViewById(R.id.followingNumberTV);
        followButton = view.findViewById(R.id.followButton);

        if (getArguments() != null) {
            otherUser = (User) getArguments().getSerializable("otherUser");
        }
        // otherUser dne i.e we want to view the current user's profile
        if (otherUser == null) {
            userObject = UserController.getCurrentUser();
            UserController.addObserverToCurrentUser(this);
            followButton.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the profile tab layout and viewpager
        setTabsAndViewPager();

        // fill up user info on view created
        if (otherUser != null) {
            setFields(otherUser);
            setProfilePicImageView(otherUser);
            setFollowButtonState();
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO Change the text to "REQUESTED" and add a permission request to that user's permission list and in the database
                }
            });
        } else {
            setFields(userObject);
            setProfilePicImageView(userObject);

            // When user clicks profile picture, change it
            profilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGetContent.launch("image/*");  // launch file explorer
                }
            });
        }

    }

    private void setFollowButtonState() {
        // TODO
        // Set the state of the follow button accordingly
        // when viewing other user's profile.
        // Determine if current user is following the viewing profile.
        // i.e if (UserManager.isFollowing(otherUser.getUsername()) == 1) {
        // followButton.setText("FOLLOWING"); }
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
        int followingNumber = userObject.getFollowing().size();
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
    public void update(Observable observable, Object o) {
        // set our info into the textViews and profile pic
        if (otherUser != null) {
            setFields(otherUser);
            setProfilePicImageView(otherUser);
        } else {
            setFields(userObject);
            setProfilePicImageView(userObject);
        }
    }
}