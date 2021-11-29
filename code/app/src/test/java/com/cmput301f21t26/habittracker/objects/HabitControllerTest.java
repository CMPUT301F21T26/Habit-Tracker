package com.cmput301f21t26.habittracker.objects;

import static org.junit.jupiter.api.Assertions.*;

import android.util.Log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Tests the visual indicator updater without Db functionality to ensure proper behaviour
 */
class HabitControllerTest {
    Habit habit;
    Calendar lastAccess;

    @BeforeEach
    void setupThis() {
        habit = new Habit("Do dishes");
        lastAccess = Calendar.getInstance();
    }

    /**
     * Tests proper functionality of the updateVisualDenominator function for a single habit
     * Doesn't access DB
     */
    @Test
    void testUpdateVisualDenominator() {
        System.out.print("HabitControllerTest " + lastAccess.getTimeInMillis() + "\n");
        lastAccess.add(Calendar.DAY_OF_WEEK, -10);
        System.out.print("HabitControllerTest " + lastAccess.getTimeInMillis() + "\n");
        ArrayList<Integer> daysList = new ArrayList<>();
        for (int i=0; i<7; i++) {
            daysList.add(i);
        }
        // every day it should add one, so the difference specified at the top should be correct
        habit.setDaysList(daysList);

        updateVisualDenominator(habit, lastAccess.getTime(), Calendar.getInstance());
        assertEquals(10, habit.getSupposedHE());

        habit.setSupposedHE(0);
        ArrayList<Integer> daysList2 = new ArrayList<>();
        for (int i=0; i<7; i++) {
            if (i%2==0){
                daysList2.add(i);
            }
        }
        //sundays, tuesdays, thursdays, saturdays
        habit.setDaysList(daysList2);
        updateVisualDenominator(habit, lastAccess.getTime(), Calendar.getInstance());

        assertEquals(6, habit.getSupposedHE());

        habit.setSupposedHE(0);
        updateVisualDenominator(habit, Calendar.getInstance().getTime(), Calendar.getInstance());
        assertEquals(0, habit.getSupposedHE());
    }


    /**
     * Updates supposedHE to keep track of how many habit events were supposed to have happened
     * Used in UserController in the process of setting up the user class when logging in/authenticating
     * Checks for days until current day
     * @param dayLastAccessed
     * The last time the user logged in prior to today
     * @param today
     * the date for today, only passed so we don't have to get todays date every time within the function
     */
    public void updateVisualDenominator(Habit habit, Date dayLastAccessed, Calendar today) {
        //loop through days until we get to current day, translate this.daysList into which days
        //of the week we need to update the denominator for
        //first things first convert dayLastAccessed to a calendar object to make comparisons easier
        Calendar start = Calendar.getInstance();
        start.setTime(dayLastAccessed);
        System.out.println("converted from day in ms " + start.getTimeInMillis());
        start.set(start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        System.out.println(habit.getDaysList());
        System.out.println("\nThe value of right now " + today.getTimeInMillis());
        //now check today>start
        while (start.before(today)) {
            //now we know at least one day has passed, loop through all days until we get to today
            start.add(Calendar.DAY_OF_WEEK, 1);
            //System.out.println(start.get(Calendar.DAY_OF_WEEK));
            System.out.println(start.getTimeInMillis());
            if (habit.getDaysList().contains(start.get(Calendar.DAY_OF_WEEK)-1)) {
                habit.setSupposedHE(habit.getSupposedHE()+1);
            }
            // loops up to exact date
        }

    }


}