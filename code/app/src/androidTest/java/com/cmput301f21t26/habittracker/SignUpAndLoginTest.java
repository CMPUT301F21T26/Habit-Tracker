package com.cmput301f21t26.habittracker;

import static org.junit.Assert.*;

import android.app.Activity;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the login and signup pages;
 * i.e. tests for input validation
 * and that the login and signup fragments
 * show up correctly.
 */
public class SignUpAndLoginTest {
    private Solo solo;

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPass;

    private FragmentManager fragmentManager;
    private Fragment mainLoginSignupFragment;
    private Fragment signupFragment;
    private Fragment loginFragment;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

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

    /**
     * Deletes the user from Firebase Auth and Firestore
     */
    public void deleteUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            assert user.getDisplayName().equals(username);
            // Prompt the user to re-provide their sign-in credentials
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("SignUpAndLoginTest", "User deleted");
                            }
                        }
                    });
        }
        DocumentReference userRef = mStore.collection("users").document(username);
        userRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("SignUpAndLoginTest", "User deleted from Firestore");
            }
        });
    }

    @Before
    public void initStrings() {
        // Specify a valid string.
        username = "EspressoTester";
        firstName = "Espresso";
        lastName = "Grande";
        email = "NoImNotA@Basic.Bitch";
        password = "starbucksLuvr";
        confirmPass = "starbucksLuvr";

        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());

        fragmentManager = rule.getActivity().getSupportFragmentManager();
        signupFragment = fragmentManager.findFragmentById(R.id.signupFragment);
        loginFragment = fragmentManager.findFragmentById(R.id.loginFragment);
        mainLoginSignupFragment = fragmentManager.findFragmentById(R.id.mainLoginSignupFragment);

        // Wait for splash screen to end
        solo.waitForActivity(LoginSignupActivity.class);

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        // Make sure user doesn't exist before testing
        deleteUser();

    }

    @Test
    public void testSignupPageShows() {
        // Check that the main login/signup page is displayed
        assertFragmentShown(mainLoginSignupFragment);

        // Click signup button
        solo.clickOnButton("SIGNUP");

        // Check if the signup fragment shows up
        assertFragmentShown(signupFragment);

    }

    @Test
    public void testSignupFieldsEntered() {
        testSignupPageShows();

        // Enter values into fields
        solo.enterText((EditText) solo.getView(R.id.firstNameET), firstName);

        // Click signup button; Fragments shouldn't change since there are empty fields
        solo.clickOnButton("SIGNUP");
        assertFragmentShown(signupFragment);

        // Enter rest of the fields, with mismatching passwords
        solo.enterText((EditText) solo.getView(R.id.lastNameET), lastName);
        solo.enterText((EditText) solo.getView(R.id.emailET), email);
        solo.enterText((EditText) solo.getView(R.id.usernameET), username);
        solo.enterText((EditText) solo.getView(R.id.passwordET), password);
        solo.enterText((EditText) solo.getView(R.id.confirmPassET), "WRONG");

        // Fragments shouldn't change since passwords are not matching
        solo.clickOnButton("SIGNUP");
        assertFragmentShown(signupFragment);

        // Enter correct confirmPass
        solo.clearEditText((EditText) solo.getView(R.id.confirmPassET));
        solo.enterText((EditText) solo.getView(R.id.confirmPassET), confirmPass);

        // Signup, now user should be directed to login fragment
        solo.clickOnButton("SIGNUP");
        solo.waitForFragmentById(R.id.loginFragment, 2000);
        assertFragmentShown(loginFragment);

        // Delete user
        deleteUser();
    }


    public void signup() {
        testSignupPageShows();

        // Enter values into fields
        solo.enterText((EditText) solo.getView(R.id.firstNameET), firstName);
        solo.enterText((EditText) solo.getView(R.id.lastNameET), lastName);
        solo.enterText((EditText) solo.getView(R.id.emailET), email);
        solo.enterText((EditText) solo.getView(R.id.usernameET), username);
        solo.enterText((EditText) solo.getView(R.id.passwordET), password);
        solo.enterText((EditText) solo.getView(R.id.confirmPassET), confirmPass);

        // Click signup button
        solo.clickOnButton("SIGNUP");

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
        // Make sure user is signed up
        signup();
        solo.waitForFragmentById(R.id.loginFragment, 2000);
        assertFragmentShown(loginFragment);

        // Enter username only
        solo.enterText((EditText) solo.getView(R.id.usernameET), username);

        // Click login button; Fragments shouldn't change since there are empty fields
        solo.clickOnButton("LOGIN");
        assertFragmentShown(loginFragment);

        // Enter password only
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


}
