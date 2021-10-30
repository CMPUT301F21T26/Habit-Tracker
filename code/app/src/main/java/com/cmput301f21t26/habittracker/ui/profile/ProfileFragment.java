package com.cmput301f21t26.habittracker.ui.profile;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentProfileBinding;
import com.cmput301f21t26.habittracker.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private String TAG = "ProfileFragment";
    private ProfileViewModel profileViewModel;
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
    private User userObject;
    private User otherUser = null;

    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private CollectionReference collectionReference;
    private DocumentReference documentReference;



    // creates profile fragment for other viewing other users profile page
    public static ProfileFragment newInstance(String otherUsername) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("username", otherUsername);
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();


        // Initiate views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        usernameTV = view.findViewById(R.id.usernameTV);
        fullNameTV = view.findViewById(R.id.fullNameTV);
        profilePic = view.findViewById(R.id.profilePicImageView);
        followersTV = view.findViewById(R.id.followersNumberTV);
        followingTV = view.findViewById(R.id.followingNumberTV);

        // Get the user's username and sets the rest of the info
        // As well as getting the user object
        FirebaseUser user = mAuth.getCurrentUser();
        String username = mAuth.getCurrentUser().getDisplayName();
        if (user != null) {
            getUserObject(username);
        }

        collectionReference = mStore.collection("users");
        documentReference = collectionReference.document(username);

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the profile tab layout and viewpager
        setTabsAndViewPager();

        // When user clicks profile picture, change
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");  // launch file explorer
            }
        });

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
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
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
     * Gets the user object from the database and adds
     * a snapshot listener to it, so whenever there is
     * a change in the user's data, the profile fragment
     * and its fields and profile picture are updated.
     * @param username
     *  The username of the user, type {@link String}
     */
    private void getUserObject(String username) {
        mStore
                .collection("users")
                .document(username)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {        // So then whenever there is a change in the data base, this is updated
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                        if (documentSnapshot != null) {
                            // Turn our document into user object
                            userObject = documentSnapshot.toObject(User.class);

                            // set our info into the textViews and profile pic
                            setFields();
                            setProfilePicImageView();
                        }
                    }
                });
    }

    /**
     * Gets the info from the User object and sets
     * the TextViews accordingly
     */
    private void setFields() {
        // Set fields
        fullNameTV.setText(userObject.getFirstName() + " " + userObject.getLastName());
        usernameTV.setText(userObject.getUsername());
        int followersNumber = userObject.getFollowers().size();
        int followingNumber = userObject.getFollowers().size();
        followersTV.setText(String.valueOf(followersNumber));
        followingTV.setText(String.valueOf(followingNumber));

    }

    /**
     * Sets the profile picture to the image
     * referenced in the picturePath attribute
     * in the user's data in the database.
     */
    private void setProfilePicImageView() {
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

            // Store default profile picture in storage
            changeProfilePic();
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
                            changeProfilePic();
                        }

                    }
                }
            });

    /**
     * Changes the profile picture in profile and
     * changes the pictureURL in the database for
     * the current user.
     */
    private void changeProfilePic() {

            mStorageReference = mStorage.getReference(picturePath);
            mStorageReference
                    .putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // If successful, get the download url and store it in picturePath
                            mStorageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    profileImageUrl = task.getResult().toString();
                                    documentReference.update("pictureURL", profileImageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Default profile pic was not stored");
                        }
                    });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}