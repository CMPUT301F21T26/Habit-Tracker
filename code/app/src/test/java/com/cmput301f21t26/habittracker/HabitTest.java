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

    @Test
    void testReasonExpception() {
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in setter
            habit.setReason("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab");  // 31 chars
        });
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in constructor
            habit = new Habit("Clean my room", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab");     // 21 chars
        });
    }

    @Test
    void testHabitId() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String strId = habit.getTitle() + habit.getReason() + formatter.format(habit.getStartDate());

        assertEquals(habit.getHabitId(), strId.hashCode());
    }

}
