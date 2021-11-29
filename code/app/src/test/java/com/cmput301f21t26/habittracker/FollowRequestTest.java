package com.cmput301f21t26.habittracker;

import static org.junit.jupiter.api.Assertions.*;

import com.cmput301f21t26.habittracker.objects.FollowRequest;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

/**
 * Tests getters for the FollowRequest class
 */
class FollowRequestTest {

    private User userFrom;
    private User userTo;
    private FollowRequest fr;

    @BeforeEach
    void setupThis() {
        userFrom = new User("from", "f", "rom", "a@b.c", " ", Calendar.getInstance().getTime());
        userTo = new User("to", "t", "o", "b@c.d", " ", Calendar.getInstance().getTime());
        fr = new FollowRequest(userFrom, userTo);
    }

    @Test
    void testGetFromUid() {
        assertEquals(userFrom.getUid(), fr.getFromUid());
    }

    @Test
    void testGetToUid() {
        assertEquals(userTo.getUid(), fr.getToUid());
    }

    @Test
    void testGetSenderProfilePictureUrl() {
        assertEquals(userFrom.getPictureURL(), fr.getSenderProfilePictureUrl());
    }
}