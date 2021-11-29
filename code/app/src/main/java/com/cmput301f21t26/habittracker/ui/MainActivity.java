package com.cmput301f21t26.habittracker.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.ActivityMainBinding;
import com.cmput301f21t26.habittracker.objects.AuthController;
import com.cmput301f21t26.habittracker.objects.FollowRequest;
import com.cmput301f21t26.habittracker.objects.OtherUserController;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    private final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController = null;

    private static BottomNavigationView navView;
    private Button addHabitButton;
    private Toolbar toolbar;
    private ImageView redCircle;
    private ImageView searchIcon;
    private BottomNavigationItemView profileItemView;
    private MenuItem profileItem;
    private ImageView profilePicIV;
    private ImageView profilePicOutline;
    private View profile_badge;

    private UserController userController;
    private OtherUserController otherUserController;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // find views
        addHabitButton = findViewById(R.id.addHabitButton);
        navView = findViewById(R.id.nav_view);

        // Get profile icon view
        profileItem = navView.getMenu().findItem(R.id.navigation_profile);
        BottomNavigationMenuView mbottomNavigationMenuView =
                (BottomNavigationMenuView) navView.getChildAt(0);
        View view = mbottomNavigationMenuView.getChildAt(2);
        profileItemView = (BottomNavigationItemView) view;
        profile_badge = LayoutInflater.from(this)
                .inflate(R.layout.profile_pic_icon,
                        mbottomNavigationMenuView, false);
        profilePicIV = profile_badge.findViewById(R.id.profilePicIV);
        profilePicOutline = profile_badge.findViewById(R.id.outline);

        // Get our instances
        userController = UserController.getInstance();
        otherUserController = OtherUserController.getInstance();
        authController = AuthController.getInstance();

        userController.addObserverToCurrentUser(this);

        // Set profile icon to current user's profile picture in bottom nav
        setProfileIconToProfilePic(userController.getCurrentUser().getPictureURL());

        // Setting up navController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Setting up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.todays_habits, R.id.navigation_timeline, R.id.navigation_profile)
                .build();

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Clicking on icons navigate to the selected fragments
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                NavDirections action = null;

                int id = item.getItemId();
                if (id == R.id.todays_habits && !navView.getMenu().findItem(R.id.todays_habits).isChecked()) {
                    action = MobileNavigationDirections.actionGlobalTodaysHabits(null);
                    profilePicOutline.setVisibility(View.INVISIBLE);
                    navController.navigate(action);
                    return true;
                }
                if (id == R.id.navigation_timeline && !navView.getMenu().findItem(R.id.navigation_timeline).isChecked()) {
                    action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                    profilePicOutline.setVisibility(View.INVISIBLE);
                    navController.navigate(action);
                    return true;
                }
                if (id == R.id.navigation_profile && !navView.getMenu().findItem(R.id.navigation_profile).isChecked()) {
                    action = MobileNavigationDirections.actionGlobalNavigationProfile(userController.getCurrentUser());
                    profilePicOutline.setVisibility(View.VISIBLE);
                    navController.navigate(action);
                    return true;
                }
                return false;
            }
        });

        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = MobileNavigationDirections.actionGlobalAddHabitFragment(null);
                navController.navigate(action);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (profileItem.isChecked()) {
            profilePicOutline.setVisibility(View.VISIBLE);
        } else {
            profilePicOutline.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Inflates the menu and adds the items to toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    /**
     * Before creation of options menu, applies
     * the custom layouts of the search and
     * bell/notification icon buttons, and adds
     * an onClickListener to each since using
     * actionLayout does not make onOptionsItemSelected
     * call that item that is using actionLayout.
     *
     * @param menu
     *  The menu, type {@link Menu}
     * @return
     *  Return type {@link Boolean}
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // For notification icon
        MenuItem notifMenuItem = menu.findItem(R.id.action_notif);
        FrameLayout notifRootView = (FrameLayout) notifMenuItem.getActionView();
        redCircle = (ImageView) notifRootView.findViewById(R.id.redCircle);
        // Because using app:actionLayout makes onOptionsItemSelected not call our custom menu item
        notifRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(notifMenuItem);
            }
        });

        // For search icon
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        FrameLayout searchRootView = (FrameLayout) searchMenuItem.getActionView();
        searchIcon = (ImageView) searchRootView.findViewById(R.id.searchIcon);
        searchRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(searchMenuItem);
            }
        });

        updateBellIcon();

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Called when user clicks on item in toolbar;
     * Does things according to what is clicked.
     *
     * @param item
     *  The item selected, type {@link MenuItem}
     * @return
     *  Return type {@link Boolean}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            signOut();
            return true;
        }
        if (id == R.id.action_notif) {
            openNotifDialog();
            return true;
        }
        if (id == R.id.action_search) {
            NavDirections action = MobileNavigationDirections.actionGlobalSearchFragment();
            navController.navigate(action);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates a custom dialog that contains
     * potential follow requests, and shows it
     */
    private void openNotifDialog() {
        final Dialog notifDialog = new Dialog(this);
        notifDialog.setContentView(R.layout.notification_panel);
        notifDialog.getWindow().setBackgroundDrawableResource(R.drawable.notif_panel_background);
        notifDialog.getWindow().setDimAmount(0.2F);
        notifDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        notifDialog.getWindow().getAttributes().windowAnimations = R.style.notifPanelAnimation;

        // Set our list view and its adapter
        ListView followRequestListView = (ListView) notifDialog.findViewById(R.id.followRequestListView);
        ArrayList<FollowRequest> followRequestList = (ArrayList<FollowRequest>) userController.getCurrentUser().getFollowRequests();

        // Set item onclick listener so when user clicks on a follow request, go to user's profile
        FollowRequestListAdapter.OnDialogListClickListener onDialogListClickListener = new FollowRequestListAdapter.OnDialogListClickListener() {
            @Override
            public void onItemClick(String username) {
                otherUserController.getUser(username, otherUser -> {
                    otherUserController.getHabitList(otherUser, updatedOtheruser -> {
                        NavDirections action = MobileNavigationDirections.actionGlobalNavigationProfile(updatedOtheruser);
                        navController.navigate(action);
                        notifDialog.dismiss();
                    });
                });
            }
        };

        FollowRequestListAdapter followRequestListAdapter = new FollowRequestListAdapter(this, followRequestList, onDialogListClickListener);
        followRequestListView.setAdapter(followRequestListAdapter);

        notifDialog.show();
        // When the dialog is dismissed, update notification icon
        notifDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                updateBellIcon();
            }
        });
    }

    /**
     * Updates the bell icon to have a red dot
     * if there are follow requests, invisible otherwise.
     */
    private void updateBellIcon() {
        // Set red circle to appear on bell icon if there are follow requests
        if (!userController.getCurrentUser().getFollowRequests().isEmpty()) {
            redCircle.setVisibility(View.VISIBLE);
        } else {
            redCircle.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Signs out the user from Firebase Auth and sends them back
     * to the signup/login screen
     */
    private void signOut() {
        authController.signOut();
        Intent intent = new Intent(this, LoginSignupActivity.class);
        // Make it so user can't press back button to go back to home page
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userController.detachSnapshotListeners();
        userController.deleteAllObserversFromCurrentUser();
    }

    /**
     * Hides the menu items.
     * MAKE SURE TO HAVE "setHasOptionsMenu(true);"
     * in your onCreate method in the fragment!
     *
     * @param menu
     *  The Menu, type {@link Menu}
     */
    public static void hideMenuItems(Menu menu) {
        // Make it so the menu items don't appear in the fragment
        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem notifItem = menu.findItem(R.id.action_notif);
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        if (searchItem != null && notifItem != null && settingsItem != null) {
            searchItem.setVisible(false);
            notifItem.setVisible(false);
            settingsItem.setVisible(false);
        }
    }

    /**
     * Hides the bottom navigation bar including the addHabitButton
     *
     * @param addHabitButton
     *  The addHabitButton in activity_main.xml, type {@link Button}
     * @param extendBottomNav
     *  The view that extends the background of the bottom navigation bar to the addHabitButton,
     *  to make it look more clean. Type {@link View}
     */
    public static void hideBottomNav(Button addHabitButton, View extendBottomNav){
        navView.setVisibility(View.GONE);
        addHabitButton.setVisibility(View.INVISIBLE);
        extendBottomNav.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows the bottom navigation bar including the addHabitButton
     *
     * @param addHabitButton
     *  The addHabitButton in activity_main.xml, type {@link Button}
     * @param extendBottomNav
     *  The view that extends the background of the bottom navigation bar to the addHabitButton,
     *  to make it look more clean. Type {@link View}
     */
    public static void showBottomNav(Button addHabitButton, View extendBottomNav){
        navView.setVisibility(View.VISIBLE);
        addHabitButton.setVisibility(View.VISIBLE);
        extendBottomNav.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the profile icon in the bottom navigation view
     * to the profile picture of the current user
     * @param URL
     *  The URL that contains an image of the user's profile picture {@link String}
     */
    public void setProfileIconToProfilePic(String URL) {
        Glide.with(this)
                .asBitmap()
                .load(URL)
                .placeholder(R.drawable.default_profile_pic)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_person))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // if profile pic is loaded properly, then set the OG icon to null and make the profile pic the "icon"
                        profileItem.setIcon(null);
                        profilePicIV.setImageBitmap(resource);
                        profileItemView.addView(profile_badge);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d("MainActivity", "GOT NOTIFICATION");
        updateBellIcon();
    }
}