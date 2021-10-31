package com.cmput301f21t26.habittracker;

import com.cmput301f21t26.habittracker.objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.BeforeEach;

public class UserTest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String confirmPass;
    private User userTest;

    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mStore;

    @BeforeEach
    public void setUpUser() {
        username = "EspressoTester";
        firstName = "Espresso";
        lastName = "Grande";
        email = "NoImNot@Basic.com";
        password = "starbucksLuvr";
        confirmPass = "starbucksLuvr";
        userTest = new User(username, firstName, lastName, email);
    }
}
