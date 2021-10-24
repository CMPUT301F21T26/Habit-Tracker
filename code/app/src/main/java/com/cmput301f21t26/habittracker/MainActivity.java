package com.cmput301f21t26.habittracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cmput301f21t26.habittracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static BottomNavigationView navView;
    private static Button addHabitButtonStatic;
    private static View extendBottomNav;
    private ActivityMainBinding binding;
    private NavController navController = null;
    private Button addHabitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        addHabitButton = findViewById(R.id.addHabitButton);
        addHabitButtonStatic = findViewById(R.id.addHabitButton);
        extendBottomNav = findViewById(R.id.extendBottomNav);

        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.addHabitFragment);
            }
        });
    }

    public static void hideBottomNav(){
        navView.setVisibility(View.GONE);
        addHabitButtonStatic.setVisibility(View.INVISIBLE);
        extendBottomNav.setVisibility(View.INVISIBLE);
    }

    public static void showBottomNav(){
        navView.setVisibility(View.VISIBLE);
        addHabitButtonStatic.setVisibility(View.VISIBLE);
        extendBottomNav.setVisibility(View.VISIBLE);

    }

}