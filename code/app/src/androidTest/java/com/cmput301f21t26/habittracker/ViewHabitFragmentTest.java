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

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.home.TodayHabitFragment;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class ViewHabitFragmentTest {
    final static public Integer WAIT = 500;
    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
            Thread.sleep(WAIT);

        } catch (InterruptedException e) {
            Log.d("ViewHabitFragmentTest", e.toString());
        }
    }

    /**
     * Tests if the views are shown
     */
    @Test
    public void testViewHabitDetailsShown() throws InterruptedException {
        clickOnHabit();
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
        clickOnHabit();
        confirmHabit();
        // Should bring user back to the timeline fragment
        onView(withId(R.id.todays_habits)).check(matches(isDisplayed()));
    }

    /**
     * Clicks on top habit from recycler view
     * @throws InterruptedException
     */
    public static void clickOnHabit() throws InterruptedException {
        ArrayList<Habit> todayHabitList = (ArrayList<Habit>) UserController.getCurrentUser().getTodayHabits();
        Integer position = todayHabitList.size() - 1;
        onView(withId(R.id.todayHabitRV))
            .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        Thread.sleep(WAIT);
    }

    /**
     * Assumes already in a view habit, meant to be used after clickOnHabit
     * Let's you get into editHabitFragment
     * @throws InterruptedException
     */
    public static void editHabit() throws InterruptedException {
        onView(withId(R.id.editHabitButton))
            .perform(click());
        Thread.sleep(WAIT);
    }

    /**
     * Presses the confirm/return button
     * @throws InterruptedException
     */
    public static void confirmHabit() throws InterruptedException {
        onView(withId(R.id.confirmHabitButton)).perform(click());
        Thread.sleep(WAIT);
    }
}
