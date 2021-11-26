package com.cmput301f21t26.habittracker;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;

import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class EditHabitEventFragmentTest {

    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("EditHabitEventFragmentTest", e.toString());
        }

        // Create a habit event. Double click checkbox so then the state is reset.
        // MAKE SURE THERE IS A HABIT IN TODAY'S HABIT THAT HAS A TITLE OF "Test"!!!!!!
        Thread.sleep(1000);
        ViewAction itemViewAction = ActionOnItemView.actionOnItemView(withId(R.id.habitCheckbox), click());
        onView(withId(R.id.todayHabitRV))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Test")), itemViewAction))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Test")), itemViewAction));

    }

    /**
     * Tests clicking on the edit button on the snackbar
     * that pops up when a habit is checked off in Today's Habits
     * @throws InterruptedException
     */
    @Test
    public void testSnackbarEdit() throws InterruptedException {
        // Check that the snackbar shows
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Empty habit event is created")));
        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click());
        // Check if edit habit event fragment is displayed
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(isDisplayed()));
        // Check that the habit event is the right one (since we created the habit event based
        // off of the "Test" habit)
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(withText(containsString("Test"))));
        // Delete habit event
        onView(withId(R.id.deleteHabitEventButton)).perform(click());
        // Check if we're back to where we were
        Thread.sleep(1000);
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
    }

    /**
     * Test that all the views in edit habit event fragment
     * are shown.
     * @throws InterruptedException
     */
    @Test
    public void testEditHabitEventViews() throws InterruptedException {
        // Go to view habit event fragment first
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
        ViewHabitEventFragmentTest.goToViewHabitEvent();

        // Click edit button
        onView(withId(R.id.editHabitEventButton)).perform(click());

        // Check the views are displayed
        checkEditHabitEventDetailsShown();

        // Delete habit event
        onView(withId(R.id.deleteHabitEventButton)).perform(click());
    }

    /**
     * Tests viewing edit habit fragment from Timeline fragment
     * and editing the details
     * @throws InterruptedException
     */
    @Test
    public void testEditDetails() throws InterruptedException {
        // Go to view habit event fragment first
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
        ViewHabitEventFragmentTest.goToViewHabitEvent();

        // Click edit button
        onView(withId(R.id.editHabitEventButton)).perform(click());

        // Check views shown
        checkEditHabitEventDetailsShown();

        // Edit the comment
        onView(withId(R.id.habitEventCommentET)).perform(typeText("EspressoTester"));

        // Finish
        onView(withId(R.id.confirmHabitEventButton)).perform(click());
        // Should bring user back to the timeline fragment
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));

        // Check that the habit event has been edited correctly and shows in our timeline
        onData(anything()).inAdapterView(withId(R.id.timelineListView))
                .atPosition(0)
                .onChildView(withText("EspressoTester"));


        // Delete that habit event
        ViewHabitEventFragmentTest.goToViewHabitEvent();
        onView(withId(R.id.editHabitEventButton)).perform(click());
        onView(withId(R.id.deleteHabitEventButton)).perform(click());
    }

    /**
     * Checks that all the views are shown
     */
    public void checkEditHabitEventDetailsShown() {
        onView(withId(R.id.habitEventLocationTV)).check(matches(isDisplayed()));
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(isDisplayed()));
        onView(withId(R.id.chooseLocationButton)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventDateFormatTV)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventCommentET)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventImage)).check(matches(isDisplayed()));
        onView(withId(R.id.deleteHabitEventButton)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmHabitEventButton)).check(matches(isDisplayed()));
        // Check that the habit event is the right one (since we created the habit event based
        // off of the "Test" habit)
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(withText(containsString("Test"))));
    }
}
