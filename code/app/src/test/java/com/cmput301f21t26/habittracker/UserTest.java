package com.cmput301f21t26.habittracker;

import static org.junit.jupiter.api.Assertions.*;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class UserTest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String pictureURL;

    private User userTest;

    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mStore;

    @BeforeEach
    public void setUpUser() {
        username = "EspressoTester";
        firstName = "Espresso";
        lastName = "Grande";
        email = "NoImNot@Basic.com";
        pictureURL = "google.com/yeeeeeeeeeeeeeeeeeeeeaaaaahh boiiiiiiiiii";
        userTest = new User(username, firstName, lastName, email, pictureURL);
    }

    /**
     * Tests the getter methods of the User class
     */
    @Test
    public void testGetters() {
        assertEquals(username, userTest.getUsername());
        assertEquals(firstName, userTest.getFirstName());
        assertEquals(lastName, userTest.getLastName());
        assertEquals(email, userTest.getEmail());
        assertEquals(pictureURL, userTest.getPictureURL());
    }

    /**
     * Tests the setter methods of the User class
     */
    @Test
    public void testSetters() {
        String testString = "Testing";
        userTest.setFirstName(testString);
        assertEquals(userTest.getFirstName(), testString);
        userTest.setLastName(testString);
        assertEquals(userTest.getLastName(), testString);
        userTest.setPictureURL(testString);
        assertEquals(userTest.getPictureURL(), testString);
    }

    /**
     * Tests adding objects to their respective lists
     * in the user class by using the add methods
     */
    @Test
    public void testAddingObjects() {
        // Test adding follower
        userTest.addFollower("UserOne");
        userTest.addFollower("UserTwo");
        assertTrue(userTest.getFollowers().contains("UserOne"));
        assertTrue(userTest.getFollowers().contains("UserTwo"));
        assertEquals(userTest.getFollowers().size(), 2);

        // Test adding following
        userTest.addFollowing("UserOne");
        assertTrue(userTest.getFollowing().contains("UserOne"));
        assertFalse(userTest.getFollowing().contains("UserTwo"));
        assertEquals(userTest.getFollowing().size(), 1);

        // Test adding habits
        Habit habit = new Habit();
        userTest.addHabit(habit);
        assertTrue(userTest.getHabits().contains(habit));
        assertEquals(userTest.getHabits().size(), 1);

        // Test adding todays habits
        Habit habit2 = new Habit();
        Habit habit3 = new Habit("title", "reason");
        userTest.addTodayHabit(habit2);
        userTest.addTodayHabit(habit3);
        assertTrue(userTest.getTodayHabits().contains(habit2));
        assertEquals(userTest.getTodayHabits().size(), 2);
        // Doubly make sure the correct habit is being stored
        boolean correct = false;
        for (Habit todayHabit : userTest.getTodayHabits()) {
            if (todayHabit.getTitle().equals("title")) {
                correct = true;
            }
        }
        assertTrue(correct);
    }

    /**
     * Tests removing objects from their respective lists
     * in the user class by using the remove methods
     */
    @Test
    public void testRemovingObjects() {
        // Test removing follower
        userTest.addFollower("UserOne");
        userTest.addFollower("UserTwo");
        userTest.removeFollower("UserTwo");
        assertFalse(userTest.getFollowers().contains("UserTwo"));
        assertTrue(userTest.getFollowers().contains("UserTwo"));
        assertEquals(userTest.getFollowers().size(), 1);

        // Test removing following
        userTest.addFollowing("UserOne");
        userTest.removeFollowing("UserOne");
        assertFalse(userTest.getFollowing().contains("UserOne"));
        assertTrue(userTest.getFollowing().isEmpty());

        // Test removing habits
        Habit habit = new Habit();
        userTest.addHabit(habit);
        assertTrue(userTest.getHabits().contains(habit));
        userTest.removeHabit(habit);
        assertTrue(userTest.getHabits().isEmpty());

        // Test adding todays habits
        Habit habit2 = new Habit();
        Habit habit3 = new Habit("title", "reason");
        userTest.addTodayHabit(habit2);
        userTest.addTodayHabit(habit3);
        userTest.removeTodayHabit(habit2);
        assertEquals(userTest.getTodayHabits().size(), 1);
        assertFalse(userTest.getTodayHabits().contains(habit2));
    }
}
