package com.cmput301f21t26.habittracker.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController = null;

    private static BottomNavigationView navView;
    private Button addHabitButton;
    private Toolbar toolbar;
    private ImageView redCircle;
    private ImageView searchIcon;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        addHabitButton = findViewById(R.id.addHabitButton);
        navView = findViewById(R.id.nav_view);

        // Set profile icon to current user's profile picture in bottom nav
        setProfileIconToProfilePic(UserController.getCurrentUser().getPictureURL());

        // Setting up toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.todays_habits, R.id.navigation_timeline, R.id.navigation_profile)
                .build();

        // Setting up navController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Clicking on icons navigate to the selected fragments
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                NavDirections action = null;

                int id = item.getItemId();
                if (id == R.id.todays_habits) {
                    action = MobileNavigationDirections.actionGlobalTodaysHabits(null);
                }
                if (id == R.id.navigation_timeline) {
                    action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                }
                if (id == R.id.navigation_profile) {
                    action = MobileNavigationDirections.actionGlobalNavigationProfile(null);
                }
                assert action != null;
                navController.navigate(action);

                return true;
            }
        });

        NavigationUI.setupWithNavController(binding.navView, navController);
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavDirections action = MobileNavigationDirections.actionGlobalAddHabitFragment(null);
                navController.navigate(action);
            }
        });
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
            // Just to show what it would look like with a notif coming in
            redCircle.setVisibility(View.VISIBLE);
            return true;
        }
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Signs out the user from Firebase Auth and sends them back
     * to the signup/login screen
     */
    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginSignupActivity.class);
        // Make it so user can't press back button to go back to home page
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserController.detachSnapshotListeners();
        UserController.deleteAllObserversFromCurrentUser();
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

        MenuItem profileItem = navView.getMenu().findItem(R.id.navigation_profile);
        Glide.with(this)
                .asBitmap()
                .load(URL)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_person))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        navView.setItemIconTintList(null);                  // issue with tinting covering image
                        profileItem.setIcon(new BitmapDrawable(getResources(), resource));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) { }
                });
    }

}