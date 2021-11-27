package com.cmput301f21t26.habittracker;


import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;

import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.cmput301f21t26.habittracker.CheckViewExists.exists;
import static com.cmput301f21t26.habittracker.ViewHabitFragmentTest.WAIT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;

import com.cmput301f21t26.habittracker.ui.auth.LoginSignupActivity;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;

public class EditHabitEventFragmentTest {

    @Rule
    public ActivityTestRule<LoginSignupActivity> rule = new ActivityTestRule<>(LoginSignupActivity.class);

    @Before
    public void setUp() throws InterruptedException {
        try {
            LoginTest.login();
        } catch (InterruptedException e) {
            Log.d("EditHabitEventFragmentTest", e.toString());
        }

        // Create a habit event. Double click checkbox so then the state is reset.
        // MAKE SURE THERE IS A HABIT IN TODAY'S HABIT THAT HAS A TITLE OF "Test"!!!!!!
        Thread.sleep(1000);
        // click checkbox
        ViewAction itemViewAction = ActionOnItemView.actionOnItemView(withId(R.id.habitCheckbox), click());
        onView(withId(R.id.todayHabitRV))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Test")), itemViewAction));
        // if dialog pops up, then the habit event already exists, click confirm and recreate the habit event.
        if (exists(onView(withText("CANCEL")))) {
            onView(withText("CONFIRM")).perform(click());
            onView(withId(R.id.todayHabitRV))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText("Test")), itemViewAction));
        }


    }

    /**
     * Tests clicking on the edit button on the snackbar
     * that pops up when a habit is checked off in Today's Habits
     * @throws InterruptedException
     */
    @Test
    public void testSnackbarEdit() throws InterruptedException {
        // Check that the snackbar shows
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Empty habit event is created")));
        onView(withId(com.google.android.material.R.id.snackbar_action)).perform(click());
        // Check if edit habit event fragment is displayed
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(isDisplayed()));
        // Check that the habit event is the right one (since we created the habit event based
        // off of the "Test" habit)
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(withText(containsString("Test"))));
        // Delete habit event
        onView(withId(R.id.deleteHabitEventButton)).perform(click());
        Thread.sleep(500);
        onView(withText("CONFIRM")).perform(click());
        // Check if we're back to where we were
        Thread.sleep(1000);
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
    }

    /**
     * Test that all the views in edit habit event fragment
     * are shown.
     * @throws InterruptedException
     */
    @Test
    public void testEditHabitEventViews() throws InterruptedException {
        // Go to view habit event fragment first
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
        ViewHabitEventFragmentTest.goToViewHabitEvent();

        // Click edit button
        onView(withId(R.id.editHabitEventButton)).perform(click());

        // Check the views are displayed
        checkEditHabitEventDetailsShown();

        // Delete habit event
        onView(withId(R.id.deleteHabitEventButton)).perform(click());
        Thread.sleep(500);
        onView(withText("CONFIRM")).perform(click());
    }

    /**
     * Tests viewing edit habit fragment from Timeline fragment
     * and editing the details
     * @throws InterruptedException
     */
    @Test
    public void testEditDetails() throws InterruptedException {
        // Go to view habit event fragment first
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
        ViewHabitEventFragmentTest.goToViewHabitEvent();

        // Click edit button
        onView(withId(R.id.editHabitEventButton)).perform(click());

        // Check views shown
        checkEditHabitEventDetailsShown();

        // Edit the comment
        onView(withId(R.id.habitEventCommentET)).perform(typeText("I finished a test!"));

        // Finish
        onView(withId(R.id.confirmHabitEventButton)).perform(click());
        Thread.sleep(500);
        // Should bring user back to the timeline fragment
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));

        // Check that the habit event has been edited correctly and shows in our timeline
        onData(anything()).inAdapterView(withId(R.id.timelineListView))
                .atPosition(0)
                .onChildView(withText("I finished a test!"));


        // Delete that habit event
        ViewHabitEventFragmentTest.goToViewHabitEvent();
        onView(withId(R.id.editHabitEventButton)).perform(click());
        onView(withId(R.id.deleteHabitEventButton)).perform(click());
        Thread.sleep(500);
        onView(withText("CONFIRM")).perform(click());
    }

    /**
     * Checks that all the views are shown
     */
    public void checkEditHabitEventDetailsShown() {
        onView(withId(R.id.habitEventLocationTV)).check(matches(isDisplayed()));
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(isDisplayed()));
        onView(withId(R.id.chooseLocationButton)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventDateFormatTV)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventCommentET)).check(matches(isDisplayed()));
        onView(withId(R.id.habitEventImage)).check(matches(isDisplayed()));
        onView(withId(R.id.deleteHabitEventButton)).check(matches(isDisplayed()));
        onView(withId(R.id.confirmHabitEventButton)).check(matches(isDisplayed()));
        // Check that the habit event is the right one (since we created the habit event based
        // off of the "Test" habit)
        onView(withId(R.id.editHabitEventTitleTV)).check(matches(withText(containsString("Test"))));
    }

    /**
     * Tests choosing an image and that once chosen it is displayed.
     * Ref: http://pengj.me/android/test/2015/10/17/expresso-test-intent.html
     * @throws InterruptedException
     */
    @Test
    public void chooseImageTest() throws InterruptedException {
        // Go to view habit event fragment first
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
        ViewHabitEventFragmentTest.goToViewHabitEvent();

        // Click edit button
        onView(withId(R.id.editHabitEventButton)).perform(click());

        // Check views shown
        checkEditHabitEventDetailsShown();

        // Now check choosing image intent
        Intent resultData = new Intent();
        Uri imageUri = Uri.parse("android.resource://com.cmput301f21t26.habittracker/drawable/default_profile_pic");
        resultData.setData(imageUri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Matcher<Intent> expectedIntent = allOf(hasAction(Intent.ACTION_PICK),
                hasData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                hasAction(Intent.ACTION_CHOOSER));
        Intents.init();
        intending(expectedIntent).respondWith(result);
        Thread.sleep(1000);
        // Click choose image button
        onView(withId(R.id.chooseImageButton)).perform(click());

    }

    /**
     * Tests taking a picture with the camera and once taken is displayed
     * @throws InterruptedException
     */
    @Test
    public void cameraTest() throws InterruptedException {
        // Go to view habit event fragment first
        onView(withId(R.id.navigation_timeline)).perform(click());
        onView(withId(R.id.navigation_timeline)).check(matches(isDisplayed()));
        ViewHabitEventFragmentTest.goToViewHabitEvent();

        // Click edit button
        onView(withId(R.id.editHabitEventButton)).perform(click());

        // Check views shown
        checkEditHabitEventDetailsShown();

        // Now check camera intent
        Intent resultData = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("IMG_DATA", BitmapFactory.decodeResource(ApplicationProvider.getApplicationContext().getResources(), R.mipmap.ic_launcher));
        resultData.putExtras(bundle);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Intents.init();
        intending(hasData(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(result);
        Thread.sleep(1000);
        // Click camera button
        onView(withId(R.id.cameraButton)).perform(click());

    }


}
