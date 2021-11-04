package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasToString;

import android.util.Log;

import androidx.test.rule.ActivityTestRule;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class ViewHabitEventFragmentTest {
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
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));

    }

    /**
     * Tests if the views are shown
     */
    @Test
    public static void testViewHabitEventDetailsShown() throws InterruptedException {
        Thread.sleep(1000);
        onData(anything()).inAdapterView(withId(R.id.timelineListView)).atPosition(0).perform(click());
        onView(withId(R.id.habitEventLocationTV)).check(matches(isDisplayed()));
        onView(withId(R.id.viewHabitEventTitleTV)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventDateFormatTV)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventCommentET)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventImage)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmHabitEventButton)).check(matches(isDisplayed()));
        onView(withId(R.id.editHabitEventButton)).check(matches(isDisplayed()));
    }

    /**
     * Tests if clicking the checkmark button
     * brings the user back
     */
    @Test
    public void testFinish() throws InterruptedException {
        Thread.sleep(1000);
        onData(anything()).inAdapterView(withId(R.id.timelineListView)).atPosition(0).perform(click());
        onView(withId(R.id.confirmHabitEventButton)).perform(click());
        // Should bring user back to the timeline fragment
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
    }
}
