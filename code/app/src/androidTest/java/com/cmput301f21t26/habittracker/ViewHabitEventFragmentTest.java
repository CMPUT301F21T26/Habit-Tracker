package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.rule.ActivityTestRule;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import io.victoralbertos.device_animation_test_rule.DeviceAnimationTestRule;

public class ViewHabitEventFragmentTest {
    // Disable device animations as it is required by espresso
    @ClassRule
    static public DeviceAnimationTestRule deviceAnimationTestRule = new DeviceAnimationTestRule();

    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("ViewHabitEventFragmentTest", e.toString());
        }

        Thread.sleep(1000);
        onView(withText("navigation_timeline")).perform(click());
    }

    @Test
    public void testViewHabitEventDetails() {
        onView(withText("Test")).perform(click());
        onView(withId(R.id.viewHabitEventFragment)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventLocationTV)).check(matches(isDisplayed()));
        onView(withId(R.id.viewHabitEventTitleTV)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventDateFormatTV)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventCommentET)).check(matches(isDisplayed()));
    }
}
