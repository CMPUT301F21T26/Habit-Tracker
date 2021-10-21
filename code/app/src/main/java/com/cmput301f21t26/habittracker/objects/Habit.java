package com.cmput301f21t26.habittracker.objects;

import java.util.Calendar;
import java.util.Date;

public class Habit {

    private String title;
    private String reason;
    private Date startDate;
    private boolean isDoneForToday;

    /* TODO implement after implementing HabitEvent and HabitPlan
    private List<HabitEvent> habitEvents;
    private List<HabitPlan> plan;
    */

    public Habit(String title, String reason, Date startDate) {
        if (title.length() > 20) {
            throw new IllegalArgumentException("Habit title must be up to 20 characters");
        }
        this.title = title;
        if (reason.length() > 30) {
            throw new IllegalArgumentException("Habit reason must be up to 30 characters");
        }
        this.reason = reason;
        this.startDate = startDate;
        this.isDoneForToday = false;
    }

    public Habit(String title, Date startDate) {
        this(title, "", startDate);
    }

    public Habit(String title, String reason) {
        this(title, reason, Calendar.getInstance().getTime());
    }

    public Habit(String title) {
        this(title, "", Calendar.getInstance().getTime());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title.length() > 20) {
            throw new IllegalArgumentException("Habit title must be up to 20 characters");
        }
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        if (reason.length() > 30) {
            throw new IllegalArgumentException("Habit reason must be up to 30 characters");
        }
        this.reason = reason;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public boolean isDoneForToday() {
        return isDoneForToday;
    }

    public void setDoneForToday(boolean doneForToday) {
        isDoneForToday = doneForToday;
    }
}