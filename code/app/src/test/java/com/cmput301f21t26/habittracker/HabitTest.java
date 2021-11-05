package com.cmput301f21t26.habittracker;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HabitTest {

    private Habit habit;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private Date date;
    private HabitEvent habitEvent;
    private HabitEvent habitEvent2;
    private ArrayList<HabitEvent> habitEvents;


    @BeforeEach
    void setupThis() {
        habit = new Habit("Do dishes");
        habitEvent = new HabitEvent(habit.getHabitId(),"Event1");
        habitEvent2 = new HabitEvent(habit.getHabitId(),"Event2");
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

    @Test
    void testGetters() {

        habit.setTitle("Test_title");
        String testTitle = habit.getTitle();
        assertEquals("Test_title", testTitle);

        habit.setReason("Test_reason");
        String testReason = habit.getReason();
        assertEquals("Test_reason", testReason);
    }

    @Test
    void habitEventChronological(){

        habit.addHabitEvent(habitEvent);
        habit.addHabitEvent(habitEvent2);
        habitEvents = (ArrayList<HabitEvent>) habit.getHabitEvents();
        assertEquals(habitEvent,habitEvents.remove(0));
        assertEquals(habitEvent2,habitEvents.remove(0));



    }
    // TODO test methods that use HabitEvent or HabitPlan as an input
}
