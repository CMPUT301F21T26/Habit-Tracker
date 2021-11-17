package com.cmput301f21t26.habittracker.interfaces;

import com.cmput301f21t26.habittracker.objects.Habit;

import java.util.ArrayList;

public interface HabitsListCallback {
    public void onCallback(ArrayList<Habit> listOfHabits);
}
