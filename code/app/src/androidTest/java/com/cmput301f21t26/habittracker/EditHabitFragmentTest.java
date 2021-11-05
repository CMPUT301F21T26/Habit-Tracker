package com.cmput301f21t26.habittracker;

import android.util.Log;

import com.cmput301f21t26.habittracker.ui.habit.ViewHabitFragment;
import com.cmput301f21t26.habittracker.ui.LoginSignupActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class EditHabitFragmentTest {
    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
            Thread.sleep(1000);
            AddHabitFragmentTest.addHabit();
            ViewHabitFragmentTest.clickOnHabit();
            ViewHabitFragmentTest.editHabit();
        } catch (InterruptedException e) {
            Log.d("ViewHabitFragmentTest", e.toString());
        }
    }

    /**
     * Tests if deleting the default habit works and returns to today habits page
     * @throws InterruptedException
     */
    @Test
    public void testDeleteHabit() throws InterruptedException {
        deleteHabit();
        // now should be at today habits
        onView(withId(R.id.todays_habits)).check(matches(isDisplayed()));
    }

    /**
     * Tests if edited habit returns to today to do page and data is updated
     * @throws InterruptedException
     */
    @Test
    public void testEditHabit() throws InterruptedException {
        Thread.sleep(ViewHabitFragmentTest.WAIT);
        onView(withId(R.id.habitTitleET))
            .perform(clearText());
        onView(withId(R.id.habitTitleET))
            .perform(typeText("EspressoTest2"));
        //click confirm
        onView(withId(R.id.confirmHabitButton))
            .perform(click());
        //should be back at main page
        onView(withId(R.id.todays_habits)).check(matches(isDisplayed()));
        //view first habit and see if title matches our string
        ViewHabitFragmentTest.clickOnHabit();
        onView(withId(R.id.habitTitle)).check(matches(withText("EspressoTest2")));
        //delete habit
        ViewHabitFragmentTest.editHabit();
        deleteHabit();
    }


    /**
     * Assumes you're already in editing view
     * Deletes the targeted habit
     * @throws InterruptedException
     */
    public static void deleteHabit() throws InterruptedException {
        Thread.sleep(ViewHabitFragmentTest.WAIT);
        onView(withId(R.id.deleteHabitButton))
            .perform(click());
        Thread.sleep(ViewHabitFragmentTest.WAIT);
        onView(withId(android.R.id.button1))
            .perform(click());
    }




}
