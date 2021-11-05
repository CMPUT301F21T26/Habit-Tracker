package com.cmput301f21t26.habittracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import android.provider.CalendarContract;

import androidx.appcompat.view.SupportActionModeWrapper;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class HabitEventTest {

    // test getters and setters, other functions.

    private HabitEvent habitEvent;
    private Habit habit;
    private String habitId;

    @BeforeEach
    void setupThis() {
        habit = new Habit("Do dishes");
        habitId = habit.getHabitId();
        habitEvent = new HabitEvent(habitId);


    }
    /**
     * Test whether HabitEvent throws an error when assigning out of bound comment length.
     */
    @Test
    void testHabitEventComment() {
        assertThrows(IllegalArgumentException.class, () -> {
            // input validation in setter
            habitEvent.setComment("aaaaaaaaaaaaaaaaaaaab"); // 21 chars
        });

        assertThrows(IllegalArgumentException.class, () -> {
            habitEvent = new HabitEvent(habitId, "aaaaaaaaaaaaaaaaaaaab");
        });
    }
    /**
     * Test whether or not the date that we made the
     * habit Event is the same as the value returned by getHabitEventDateDay().
     */
    @Test
    void testHabitEventGetHabitEventDateDay(){
        Date dateNow = Calendar.getInstance().getTime();
        habitEvent = new HabitEvent(habitId);
        assertEquals(dateNow.toInstant().truncatedTo(ChronoUnit.DAYS), habitEvent.getHabitEventDateDay());
    }
}