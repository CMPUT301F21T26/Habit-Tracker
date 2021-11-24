package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.ui.LoginSignupActivity;
import com.google.android.material.tabs.TabLayout;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProfileFragmentTest {
    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("ProfileFragmentTest", e.toString());
        }

        Thread.sleep(1000);
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.navigation_profile)).check(matches(isDisplayed()));

    }

    /**
     * Tests that all the views in the profile fragment is shown.
     */
    @Test
    public void testProfileFragmentViews() {
        onView(withId(R.id.profilePicImageView)).check(matches(isDisplayed()));
        onView(withId(R.id.usernameTV)).check(matches(isDisplayed()));
        onView(withId(R.id.fullNameTV)).check(matches(isDisplayed()));
        onView(withId(R.id.followersNumberTV)).check(matches(isDisplayed()));
        onView(withId(R.id.followingNumberTV)).check(matches(isDisplayed()));
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).check(matches(isDisplayed()));
    }

    /**
     * Checks if clicking the tab shows the correct fragments
     */
    @Test
    public void testTabs() throws InterruptedException {
        // Click followers tab
        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(1));
        Thread.sleep(1000);
        onView(withId(R.id.profileFollowersListView)).check(matches(isDisplayed()));

        // Click following tab
        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(2));
        Thread.sleep(1000);
        onView(withId(R.id.profileFollowingListView)).check(matches(isDisplayed()));

        // Click on habits tab again
        onView(withId(R.id.tabLayout)).perform(selectTabAtPosition(0));
        Thread.sleep(1000);
        onView(withId(R.id.profileHabitsRecyclerView)).check(matches(isDisplayed()));
    }

    // Credits to Gimberg: https://stackoverflow.com/users/2873824/gimberg
    // https://stackoverflow.com/questions/49626315/how-to-select-a-specific-tab-position-in-tab-layout-using-espresso-testing
    @NonNull
    public static ViewAction selectTabAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index" + String.valueOf(position);
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof TabLayout) {
                    TabLayout tabLayout = (TabLayout) view;
                    TabLayout.Tab tab = tabLayout.getTabAt(position);

                    if (tab != null) {
                        tab.select();
                    }
                }
            }
        };
    }
}
