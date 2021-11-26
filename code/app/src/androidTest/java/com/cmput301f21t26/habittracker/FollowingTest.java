package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.cmput301f21t26.habittracker.LoginTest.login;
import static com.cmput301f21t26.habittracker.LoginTest.signout;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests user stories 0.5.0X.01 (Following).
 * IMPORTANT: There must be a user with the username "EspressoVenti"
 * which "EspressoTester" (The user we are logging in with)
 * is not following, and a user with the username
 * "EspressoCento" which "EspressoTester" is following.
 */
public class FollowingTest {
    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() {
        try {
            login();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("FollowingTest", e.toString());
        }
    }

    /**
     * Tests that the search fragment is displayed
     * when clicking on the search icon in the menu
     */
    @Test
    public void testSearchFragment() {
        onView(withId(R.id.action_search)).perform(click());
        onView(withId(R.id.searchListView)).check(matches(isDisplayed()));
    }

    /**
     * Tests searching up a user "EspressoVenti" and that clicking on the
     * follow button will send a request, and clicking on it again will unsend
     * the request.
     * @throws InterruptedException thrown when there's an interruption during sleep.
     */
    @Test
    public void testFollow() throws InterruptedException {
        testSearchFragment();

        // Search up EspressoVenti
        onView(withId(R.id.action_clicked_search)).perform(click());
        onView(withId(R.id.action_clicked_search)).perform(typeSearchViewText("Espresso"));
        Thread.sleep(500);

        // Check that it shows up
        onView(withText("EspressoVenti")).check(matches(isDisplayed()));

        // Click on EspressoVenti and check that the views are displayed
        onView(withText("EspressoVenti")).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.usernameTV)).check(matches(isDisplayed()));
        onView(withId(R.id.fullNameTV)).check(matches(isDisplayed()));
        onView(withId(R.id.followersNumberTV)).check(matches(isDisplayed()));
        onView(withId(R.id.followingNumberTV)).check(matches(isDisplayed()));
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.followButton)).check(matches(isDisplayed()));

        // Click follow button, check that the text has changed to REQUESTED
        onView(withId(R.id.followButton)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.followButton)).check(matches(withText("REQUESTED")));

        // Click on the follow button again to test that the request is undone
        onView(withId(R.id.followButton)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.followButton)).check(matches(withText("FOLLOW")));

    }

    /**
     * Tests denying a follow request.
     * REQUIRED USERS: EspressoTester and EspressoVenti
     * @throws InterruptedException
     */
    @Test
    public void testDenyFollowRequest() throws InterruptedException {
        testFollowRequest(false, true);
    }

    /**
     * Tests allowing a follow request.
     * REQUIRED USERS: EspressoTester and EspressoVenti
     * @throws InterruptedException
     */
    @Test
    public void testAllowFollowRequest() throws InterruptedException {
        testFollowRequest(true, false);
    }

    /**
     * Tests that public habits and visual indicators are displayed when looking at
     * following users
     */
    @Test
    public void testViewFollowingHabits() throws InterruptedException {
        // First, follow EspressoVenti
        testFollowRequest(false, false);

        // Check that EspressoVenti's habits are shown when in its profile,
        // or rather, if we can see their habits, then the image saying that
        // their profile is locked, should not be visible
        onView(withId(R.id.lockHabitsImageView)).check(matches(not(isDisplayed())));
    }

    /**
     * Used to test denying and allowing a follow request
     * REQUIRED USERS: EspressoTester and EspressoVenti
     * @param unfollow A {@link boolean} that determines whether to unfollow EspressoVenti after being granted follow permissions
     * @param deny A {@link boolean} that determines whether or not EspressoVenti will deny the follow request or not
     * @throws InterruptedException
     */
    public void testFollowRequest(boolean unfollow, boolean deny) throws InterruptedException {
        // First, send a follow request to EspressoVenti
        testSearchFragment();

        // Search up EspressoVenti
        onView(withId(R.id.action_clicked_search)).perform(click());
        onView(withId(R.id.action_clicked_search)).perform(typeSearchViewText("Espresso"));
        Thread.sleep(500);

        // Check that it shows up
        onView(withText("EspressoVenti")).check(matches(isDisplayed()));

        // Click on EspressoVenti and check that the views are displayed
        onView(withText("EspressoVenti")).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.usernameTV)).check(matches(isDisplayed()));
        onView(withId(R.id.fullNameTV)).check(matches(isDisplayed()));
        onView(withId(R.id.followersNumberTV)).check(matches(isDisplayed()));
        onView(withId(R.id.followingNumberTV)).check(matches(isDisplayed()));
        onView(withId(R.id.viewPager)).check(matches(isDisplayed()));
        onView(withId(R.id.tabLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.followButton)).check(matches(isDisplayed()));

        // Click follow button to send request
        onView(withId(R.id.followButton)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.followButton)).check(matches(withText("REQUESTED")));

        // Now signout and login to EspressoVenti
        Espresso.pressBack();       // press back because menu items are not available when viewing other user profiles
        Espresso.pressBack();       // press back again to close keyboard
        Espresso.pressBack();       // press back again because menu items are not available in search
        signout();
        Thread.sleep(200);
        login("EspressoVenti", "starbucksLuvr");

        // Check that the panel shows up when clicking on the bell icon, and that the follow request is there
        onView(withId(R.id.action_notif)).perform(click());
        onView(withId(R.id.followRequestListView)).check(matches(isDisplayed()));
        onView(withId(R.id.denyButton)).check(matches(isDisplayed()));
        onView(withId(R.id.allowButton)).check(matches(isDisplayed()));
        onView(withText("EspressoTester")).check(matches(isDisplayed()));

        if (deny) {
            // Click deny
            onView(withId(R.id.denyButton)).perform(click());
            return;
        } else {
            // Click allow
            onView(withId(R.id.allowButton)).perform(click());
        }


        // Now go back to EspressoTester and check that EspressoVenti is in their following tab
        Espresso.pressBack();       // press back to close notification panel
        signout();
        Thread.sleep(200);
        login();

        // Click on profile
        onView(withId(R.id.navigation_profile)).perform(click());
        Thread.sleep(200);
        // Click following tab
        onView(withId(R.id.tabLayout)).perform(ProfileFragmentTest.selectTabAtPosition(2));
        Thread.sleep(500);
        // Check EspressoVenti displayed
        onView(withText("EspressoVenti")).check(matches(isDisplayed()));

        // Click on EspressoVenti and unfollow (so we can repeat the test)
        onView(withText("EspressoVenti")).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.followButton)).check(matches(withText("FOLLOWING")));

        if (unfollow) {
            onView(withId(R.id.followButton)).perform(click());
            onView(withId(R.id.followButton)).check(matches(withText("FOLLOW")));
        }

    }


    // Used to type text into SearchView with espresso ViewAction. Normal method does not work.
    // Ref: https://stackoverflow.com/questions/48037060/how-to-type-text-on-a-searchview-using-espresso
    public static ViewAction typeSearchViewText(final String text){
        return new ViewAction(){
            @Override
            public Matcher<View> getConstraints() {
                //Ensure that only apply if it is a SearchView and if it is visible.
                return allOf(isDisplayed(), isAssignableFrom(SearchView.class));
            }

            @Override
            public String getDescription() {
                return "Change view text";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((SearchView) view).setQuery(text,false);
            }
        };
    }
}
