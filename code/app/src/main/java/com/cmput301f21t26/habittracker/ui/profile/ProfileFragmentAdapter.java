package com.cmput301f21t26.habittracker.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for the child fragment (with view pager and tabs)
 * in the profile fragment.
 */
public class ProfileFragmentAdapter extends FragmentStateAdapter {

    public ProfileFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch(position) {
            case 1:
                return new ProfileFragmentFollowersTab();

            case 2:
                return new ProfileFragmentFollowingTab();

            default:
                return new ProfileFragmentHabitsTab();
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
