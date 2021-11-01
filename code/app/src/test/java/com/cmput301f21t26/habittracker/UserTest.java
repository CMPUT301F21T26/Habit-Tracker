package com.cmput301f21t26.habittracker;

import static org.junit.jupiter.api.Assertions.*;

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
}
