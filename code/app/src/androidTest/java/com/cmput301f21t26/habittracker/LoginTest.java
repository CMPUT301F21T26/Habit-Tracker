package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.action.ViewActions;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LoginTest {
    private static Solo soloStatic;
    private Solo solo;

    private String username;
    private String password;
    private FragmentManager fragmentManager;
    private Fragment mainLoginSignupFragment;
    private Fragment loginFragment;

    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    /**
     * Checks if the fragment is displayed
     * @param fragment
     *  The fragment to check if displayed. Type {@link Fragment}
     */
    private void assertFragmentShown(Fragment fragment) {
        if (fragment != null) {
            assertTrue(fragment.isVisible());
        }
    }

    @Before
    public void initStrings() {
        // Specify a valid string.
        username = "EspressoTester";
        password = "starbucksLuvr";

        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());

        fragmentManager = rule.getActivity().getSupportFragmentManager();
        loginFragment = fragmentManager.findFragmentById(R.id.loginFragment);
        mainLoginSignupFragment = fragmentManager.findFragmentById(R.id.mainLoginSignupFragment);

        // Wait for splash screen to end
        solo.waitForActivity(LoginSignupActivity.class);


    }

    @Test
    public void testLoginPageShows() {
        // Check that the main login/signup page is displayed
        assertFragmentShown(mainLoginSignupFragment);

        // Click login button
        solo.clickOnButton("LOGIN");

        // Check if the login fragment shows up
        assertFragmentShown(loginFragment);
    }

    @Test
    public void testLoginFieldsEntered() {
        // Go to login page
        testLoginPageShows();

        // Enter username only
        solo.enterText((EditText) solo.getView(R.id.usernameET), username);

        // Click login button; Fragments shouldn't change since there are empty fields
        solo.clickOnButton("LOGIN");
        assertFragmentShown(loginFragment);

        // Enter password only
        solo.waitForText(username, 1, 2000);
        solo.clearEditText((EditText) solo.getView(R.id.usernameET));
        solo.enterText((EditText) solo.getView(R.id.passwordET), password);

        // Click login button; Fragments shouldn't change since there are empty fields
        solo.clickOnButton("LOGIN");
        assertFragmentShown(loginFragment);

        // Enter username
        solo.enterText((EditText) solo.getView(R.id.usernameET), username);

        // Click login button; MainActivity and its main fragment should be shown
        solo.clickOnButton("LOGIN");
        solo.waitForActivity("MainActivity", 2000);
        solo.assertCurrentActivity("Incorrect activity", MainActivity.class);

    }

    // Logs in user
    public static void login() throws InterruptedException {
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.usernameET)).perform(typeText("EspressoTester"));
        onView(withId(R.id.passwordET)).perform(typeText("starbucksLuvr"));
        onView(withId(R.id.passwordET)).perform(ViewActions.closeSoftKeyboard());

        onView(withId(R.id.loginConfirmButton)).perform(click());
        // sleep to allow time for login to process and view to switch
        Thread.sleep(6000);
    }

    @Test
    public void testSignout() throws InterruptedException {
        login();

        // Test the sign out button
        solo.clickOnMenuItem("Sign Out");

        // The main login/signup page should be shown
        assertFragmentShown(mainLoginSignupFragment);
    }
}
