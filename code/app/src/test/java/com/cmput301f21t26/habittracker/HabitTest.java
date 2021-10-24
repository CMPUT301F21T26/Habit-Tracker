package com.cmput301f21t26.habittracker;

import com.cmput301f21t26.habittracker.objects.Habit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import android.util.Log;

import java.text.SimpleDateFormat;

public class HabitTest {

    private Habit habit;

    @BeforeEach
    void setupThis() {
        habit = new Habit("Do dishes");
    }

    /**
     * Test whether Habit throws an error when assigning out of bound title.
     */
    @Test
    void testTitleException() {
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in setter
            habit.setTitle("aaaaaaaaaaaaaaaaaaaab");        // 21 chars
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in constructor
            habit = new Habit("aaaaaaaaaaaaaaaaaaaab");     // 21 chars
        });
    }

    /**
     * Test whether Habit throws an error when assigning out of bound reason.
     */
    @Test
    void testReasonException() {
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in setter
            habit.setReason("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab");  // 31 chars
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in constructor
            habit = new Habit("Clean my room", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab");     // 21 chars
        });
    }

    /**
     * Test whether two different habits have different habit ids.
     */
    @Test
    void testHabitIdUniqueness() {
        Habit habit2 = new Habit("Clean washroom");

        assertNotEquals(habit.getHabitId(), habit2.getHabitId());
    }

    // TODO test methods that use HabitEvent or HabitPlan as an input
}
