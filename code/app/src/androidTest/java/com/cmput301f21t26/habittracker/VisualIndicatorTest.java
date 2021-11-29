package com.cmput301f21t26.habittracker;

import android.util.Log;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;
import com.cmput301f21t26.habittracker.ui.habit.ViewHabitFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class VisualIndicatorTest {
    final static public Integer WAIT = 200;
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

        AddHabitFragmentTest.addHabit();
        Thread.sleep(WAIT);
    }

    /**
     * Tests whether creating a new habit event will increase indicator
     * @throws InterruptedException
     */
    @Test
    public void testIndicatorHabitEventCreated() throws InterruptedException {

        ArrayList<Habit> todayHabitList = (ArrayList<Habit>) UserController.getInstance().getCurrentUser().getTodayHabits();
        Integer position = todayHabitList.size() - 1;

        onView(withId(R.id.todayHabitRV))
                .check(matches(hasDescendant(withText("0%"))));
        Thread.sleep(WAIT);


        ViewAction itemViewAction = ActionOnItemView.actionOnItemView(withId(R.id.habitCheckbox), click());
        onView(withId(R.id.todayHabitRV)).perform(RecyclerViewActions.actionOnItemAtPosition(position, itemViewAction));
        Thread.sleep(WAIT);

        onView(withId(R.id.todayHabitRV))
                .check(matches(hasDescendant(withText("100%"))));
        Thread.sleep(WAIT);
        //check if the visual
        testIndicatorHabitEventDeleted();
    }


    public void testIndicatorHabitEventDeleted() throws InterruptedException {
        ArrayList<Habit> todayHabitList = (ArrayList<Habit>) UserController.getInstance().getCurrentUser().getTodayHabits();
        Integer position = todayHabitList.size() - 1;

        ViewAction itemViewAction = ActionOnItemView.actionOnItemView(withId(R.id.habitCheckbox), click());
        onView(withId(R.id.todayHabitRV)).perform(RecyclerViewActions.actionOnItemAtPosition(position, itemViewAction));
        Thread.sleep(WAIT);

        //click confirm on dialog
        onView(withId(android.R.id.button1))
                .perform(click());
        Thread.sleep(WAIT);

        onView(withId(R.id.todayHabitRV))
                .check(matches(hasDescendant(withText("0%"))));
        Thread.sleep(WAIT);

        testIndicatorViewHabit();

    }

    public void testIndicatorViewHabit() throws InterruptedException {
        ViewHabitFragmentTest.clickOnHabit();
        //check if the visual indicator says 0
        onView(withId(R.id.progressDisplay))
                .check(matches(withText("0%")));
        Thread.sleep(WAIT);
        ViewHabitFragmentTest.confirmHabit();
        Thread.sleep(WAIT);
    }

    @After
    public void close() throws InterruptedException {
        ViewHabitFragmentTest.clickOnHabit();
        Thread.sleep(WAIT);
        ViewHabitFragmentTest.editHabit();
        Thread.sleep(WAIT);
        EditHabitFragmentTest.deleteHabit();
        Thread.sleep(WAIT);
    }

}
