package com.cmput301f21t26.habittracker;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class AddHabitFragmentTest {
    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("AddHabitFragmentTest", e.toString());
        }
    }

    /**
     * Tests if all details of the fragment are properly accessible
     * @throws InterruptedException
     */
    @Test
    public void testAddHabitDetailsShown() throws InterruptedException {
        onView(withId(R.id.addHabitButton))
            .perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.habitTitleET)).check(matches(isDisplayed()));
        onView(withId(R.id.habitReasoningET)).check(matches(isDisplayed()));
        onView(withId(R.id.chipGroup)).check(matches(isDisplayed()));
        onView(withId(R.id.dateFormatMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmAddHabitButton)).check(matches(isDisplayed()));
        onView(withId(R.id.chooseDateButton)).check(matches(isDisplayed()));
        onView(withId(R.id.privacySwitch)).check(matches(isDisplayed()));
    }

    /**
     * Tests adding a habit, then deletes it
     * @throws InterruptedException
     */
    @Test
    public void testAddHabit() throws InterruptedException{
        // adds a habit
        addHabit();
        //check that it has returned to today_habits
        onView(withId(R.id.todays_habits)).check(matches(isDisplayed()));
        //check if text is correct
        ViewHabitFragmentTest.clickOnHabit();
        onView(withId(R.id.habitTitle)).check(matches(withText("EspressoTest")));
        //delete the habit
        ViewHabitFragmentTest.editHabit();
        EditHabitFragmentTest.deleteHabit();
    }

    /**
     * Creates a new habit from today habit
     * @throws InterruptedException
     */
    public static void addHabit() throws InterruptedException {
        Thread.sleep(200);
        onView(withId(R.id.addHabitButton))
            .perform(click());
        addDefaultHabit();
    }

    /**
     * Creates a new habit, assumes its in add habit fragment
     * @throws InterruptedException
     */
    public static void addDefaultHabit() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.habitTitleET))
            .perform(typeText("EspressoTest"));
        onView(withId(R.id.habitReasoningET))
            .perform(typeText("tester"));
        onView(withId(R.id.habitReasoningET)).perform(ViewActions.closeSoftKeyboard());
        // select all chips
        onView(withId(R.id.sundayChip))
            .perform(click());
        onView(withId(R.id.mondayChip))
            .perform(click());
        onView(withId(R.id.tuesdayChip))
            .perform(click());
        onView(withId(R.id.wednesdayChip))
            .perform(click());
        onView(withId(R.id.thursdayChip))
            .perform(click());
        onView(withId(R.id.fridayChip))
            .perform(click());
        onView(withId(R.id.saturdayChip))
            .perform(click());
        //set the start date to today
        //get current date, then format it to expected result
        Date current = Calendar.getInstance().getTime();
        SimpleDateFormat toUser = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        toUser.setTimeZone(TimeZone.getTimeZone("UTC"));
        onView(withId(R.id.dateFormatMessage))
            .perform(setTextInTV(toUser.format(current)));
        //click on confirm button
        Thread.sleep(200);
        onView(withId(R.id.confirmAddHabitButton))
            .perform(click());
    }

    public static ViewAction setTextInTV(final String val) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TextView.class));
            }

            @Override
            public String getDescription() {
                return "replaces text";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((TextView) view).setText(val);
            }
        };
    }
}
