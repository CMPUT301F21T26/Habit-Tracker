package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.util.Log;

import androidx.test.rule.ActivityTestRule;

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
}
