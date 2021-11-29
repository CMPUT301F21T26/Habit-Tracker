package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.cmput301f21t26.habittracker.CheckViewExists.exists;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import android.util.Log;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MapFragmentTest {

    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        // ALWAYS HAVE LOCATION ON FOR THIS TEST
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("MapFragmentTest", e.toString());
        }

        //Make sure there is a Habit titled Test and that the box is checked off.

        ViewAction itemViewAction = ActionOnItemView.actionOnItemView(withId(R.id.habitCheckbox), click());
        onView(withId(R.id.todayHabitRV))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Test")), itemViewAction));
        // if dialog pops up, then the habit event already exists, click confirm and recreate the habit event.
        if (exists(onView(withText("CANCEL")))) {
            onView(withText("CONFIRM")).perform(click());
            onView(withId(R.id.todayHabitRV))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Test")), itemViewAction));
        }

        // Check that the snackbar shows
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Empty habit event is created")));
        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click());
        // Check if edit habit event fragment is displayed
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(isDisplayed()));
        // Check that the habit event is the right one (since we created the habit event based
        // off of the "Test" habit)
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(withText(containsString("Test"))));

    }

    /**
     * Test clicking on choose location button in edit habit event fragment
     * see if the map fragment pops up with the correct confirm button
     * @throws InterruptedException
     */
    @Test
    public void testMapFragmentView() throws InterruptedException{

        // click on Choose location button to open up fragment
        onView(withId(R.id.chooseLocationButton)).perform(click());
        //check the right fragment is opened
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        onView(withId(R.id.locationConfirmBtn)).check(matches(isDisplayed()));
        // click on confirm button
        onView(withId(R.id.locationConfirmBtn)).perform(click());
        // check that location saved is same as the one displayed;
        onView(withId(R.id.habitEventLocationTV)).check(matches(not(withText(containsString("NONE")))));

    }
}
