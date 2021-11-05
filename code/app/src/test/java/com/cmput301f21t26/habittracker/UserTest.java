package com.cmput301f21t26.habittracker;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {
    private User user;
    private final String TITLE = "Default";
    private Habit habit = defaultHabit();

    @BeforeEach
    void setupThis() {
        user = new User();
    }

    private Habit defaultHabit() {
        return new Habit(TITLE, "for tests");
    }

    /**
     * Test add habit function
     */
    @Test
    void testAddHabit() {
        user.addHabit(habit);
        assertEquals(1, user.getHabits().size());
    }

    /**
     * Test remove habit function
     */
    @Test
    void testRemoveHabit() {
        user.addHabit(habit);
        user.removeHabit(habit);
        assertEquals(0, user.getHabits().size());
    }

    /**
     * Test if user class can accurately get a habit based on Id string
     */
    @Test
    void testGetHabit() {
        user.addHabit(habit);
        Habit tester = user.getHabit(habit.getHabitId());
        assertEquals(tester.getHabitId(), habit.getHabitId());
    }

    /**
     * Test updateHabit
     */
    @Test
    void testUpdateHabit() {
        Habit tester = defaultHabit();
        user.addHabit(tester);
        tester.setReason("Updated reason");
        user.updateHabit(tester);
        Habit returned = user.getHabit(tester.getHabitId());
        assertEquals("Updated reason", returned.getReason());
    }

    /**
     * Test addTodayHabit
     */
    @Test
    void testAddTodayHabit() {
        user.addTodayHabit(habit);
        assertEquals(1,user.getTodayHabits().size());
    }

    /**
     * Test removeTodayHabit
     */
    @Test
    void testRemoveTodayHabit() {
        user.addTodayHabit(habit);
        user.removeTodayHabit(habit);
        assertEquals(0, user.getTodayHabits().size());
    }

    /**
     * Test updateTodayHabit
     */
    @Test
    void testUpdateTodayHabit() {
        Habit tester = defaultHabit();
        user.addTodayHabit(tester);
        tester.setReason("updated");
        user.updateTodayHabit(tester);
        Habit out = user.getTodayHabits().get(0);
        assertEquals(out.getReason(), tester.getReason());
    }



}
