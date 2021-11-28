package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
        Thread.sleep(1000);
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));



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
