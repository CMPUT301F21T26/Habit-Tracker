package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Notification;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewFinder;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class ReorderingTest {

    private static RecyclerView mRecyclerList;

    public static ViewAction dragFrom(int fromPos, int toPos) {

        float margin = 8;
        float height = 80;

        /**
         * find coordinates for swipe action to take place
         * based on position of habits in recycler view
         */
        return new GeneralSwipeAction(
                Swipe.SLOW,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        float width = view.getWidth();

                        Log.d("screenPos", Arrays.toString(screenPos));
                        float screenX = screenPos[0] + width / 2;
                        float screenY = screenPos[1] + margin + (height / 2) + (height*fromPos);
                        float[] coords = {screenX, screenY};
                        Log.d("screenPos", Arrays.toString(coords));

                        return coords;
                    }
                },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        float width = view.getWidth();

                        float screenX = screenPos[0] + width / 2;
                        float screenY = screenPos[1] + margin + (height / 2) + (height*toPos);
                        float[] coords = {screenX, screenY};
                        Log.d("screenPos", Arrays.toString(coords));

                        return coords;
                    }
                },
                Press.FINGER
        );
    }

    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("ReorderingTest", e.toString());
        }
        Thread.sleep(1000);
    }

    /**
     *
     * @throws InterruptedException
     * tests drag and drop of habits in habit list of profile
     */
    @Test
    public void testSwiping() throws InterruptedException {
        onView(withId(R.id.navigation_profile)).perform(click());
        clickOnHabit();

    }
    public static void clickOnHabit() throws InterruptedException {
        ArrayList<Habit> todayHabitList = (ArrayList<Habit>) UserController.getInstance().getCurrentUser().getHabits();
        Integer position = todayHabitList.size() - 1;

        onView(withId(R.id.profileHabitsRecyclerView)).perform(dragFrom(1, 2));
        Thread.sleep(1000);
    }

}
