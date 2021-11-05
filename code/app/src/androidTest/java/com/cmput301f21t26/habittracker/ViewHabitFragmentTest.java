package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.hasToString;

import android.util.Log;

import androidx.test.rule.ActivityTestRule;


import com.cmput301f21t26.habittracker.ui.LoginSignupActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ViewHabitFragmentTest {
    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("ViewHabitFragmentTest", e.toString());
        }
    }

    /**
     * Tests if the views are shown
     */
    @Test
    public void testViewHabitDetailsShown() throws InterruptedException {
        Thread.sleep(1000);
        onData(anything()).inAdapterView(withId(R.id.todayHabitRV)).atPosition(0).perform(click());
        onView(withId(R.id.habitTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.habitReasoning)).check(matches(isDisplayed()));
        onView(withId(R.id.chipGroup)).check(matches(isDisplayed()));
        onView(withId(R.id.dateFormatMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmHabitButton)).check(matches(isDisplayed()));
        onView(withId(R.id.editHabitButton)).check(matches(isDisplayed()));
    }

    /**
     * Tests if clicking the checkmark button
     * brings the user back
     */
    @Test
    public void testFinish() throws InterruptedException {
        Thread.sleep(1000);
        onData(anything()).inAdapterView(withId(R.id.todayHabitRV)).atPosition(0).perform(click());
        onView(withId(R.id.confirmHabitButton)).perform(click());
        // Should bring user back to the timeline fragment
        onView(withId(R.id.todays_habits)).check(matches(isDisplayed()));
    }
}
