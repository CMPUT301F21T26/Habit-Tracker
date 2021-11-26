package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;
import com.cmput301f21t26.habittracker.ui.MainActivity;
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

        solo = new Solo(getInstrumentation(), rule.getActivity());

        fragmentManager = rule.getActivity().getSupportFragmentManager();
        loginFragment = fragmentManager.findFragmentById(R.id.loginFragment);
        mainLoginSignupFragment = fragmentManager.findFragmentById(R.id.mainLoginSignupFragment);

        // Wait for splash screen to end
        solo.waitForActivity(LoginSignupActivity.class);
    }

    /**
     * Tests that the login page shows when clicking from the main login signup page
     */
    @Test
    public void testLoginPageShows() {
        // Check that the main login/signup page is displayed
        assertFragmentShown(mainLoginSignupFragment);

        // Click login button
        solo.clickOnButton("LOGIN");

        // Check if the login fragment shows up
        assertFragmentShown(loginFragment);
    }

    /**
     * Tests the login fields in the login page for correct input validation.
     */
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

    /**
     * Goes to the login page and logs in with the tester user,
     * EspressoTester. It is important to note that in that user,
     * it must have a habit that is planned for everyday, and has a title
     * of "Test". As well, the timeline which shows all the user's habit events,
     * should have at least one habit event.
     * @throws InterruptedException
     */
    public static void login() throws InterruptedException {
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.usernameET)).perform(typeText("EspressoTester"));
        onView(withId(R.id.passwordET)).perform(typeText("starbucksLuvr"));
        onView(withId(R.id.passwordET)).perform(ViewActions.closeSoftKeyboard());

        onView(withId(R.id.loginConfirmButton)).perform(click());
        // sleep to allow time for login to process and view to switch
        Thread.sleep(3000);
    }

    /**
     * Logins to user with the given username and password strings
     * @param username the username of the user to login to, {@link String}
     * @param password the password of the user to login to, {@link String}
     * @throws InterruptedException
     */
    public static void login(String username, String password) throws InterruptedException {
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.usernameET)).perform(typeText(username));
        onView(withId(R.id.passwordET)).perform(typeText(password));
        onView(withId(R.id.passwordET)).perform(ViewActions.closeSoftKeyboard());

        onView(withId(R.id.loginConfirmButton)).perform(click());
        // sleep to allow time for login to process and view to switch
        Thread.sleep(2000);
    }

    /**
     * Signs out the current user by clicking on the signout button
     */
    public static void signout() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Sign Out"))
                .perform(click());
    }

    /**
     * Tests the functionality of the signout button.
     * @throws InterruptedException
     */
    @Test
    public void testSignout() throws InterruptedException {
        login();

        // Test the sign out button
        solo.clickOnMenuItem("Sign Out");

        // The main login/signup page should be shown
        assertFragmentShown(mainLoginSignupFragment);
    }
}
