package com.cmput301f21t26.habittracker.objects;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Habit {

    private String title;
    private String reason;
    private Date startDate;
    private boolean isDoneForToday;
    private final String datePattern = "yyyy-MM-dd_HH:mm:ss";

    /* TODO implement after implementing HabitEvent and HabitPlan
    private List<HabitEvent> habitEvents;
    private List<HabitPlan> plan;
    */

    /**
     * A default Habit constructor.
     *
     * @param title habit title
     * @param reason reason why the habit is created
     * @param startDate date when the habit was created
     * @throws IllegalArgumentException if habit title or reason are too long
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

    /**
     * A Habit constructor for the case where the habit reason is not given by the user.
     * A default habit reason is an empty string.
     *
     * @param title habit title
     * @param startDate date when the habit was created
     * @throws IllegalArgumentException if habit title or reason are too long
     */
    public Habit(String title, Date startDate) {
        this(title, "", startDate);
    }

    /**
     * A Habit constructor for the case where the habit creation date is not given by the user.
     * A default habit creation date is the time when the constructor is called.
     *
     * @param title habit title
     * @param reason reason why the habit is created
     * @throws IllegalArgumentException if habit title or reason are too long
     */
    public Habit(String title, String reason) {
        this(title, reason, Calendar.getInstance().getTime());
    }

    /**
     * A Habit constructor for the case where both habit creation date habit reason
     * are not given by the user.
     * A default habit reason is an empty string.
     * A default habit creation date is the time when the constructor is called.
     *
     * @param title habit title
     * @throws IllegalArgumentException if habit title or reason are too long
     */
    public Habit(String title) {
        this(title, "", Calendar.getInstance().getTime());
    }

    public String getTitle() {
        return title;
    }

    /**
     * @param title habit title
     * @throws IllegalArgumentException if habit title is too long
     */
    public void setTitle(String title) {
        if (title.length() > 20) {
            throw new IllegalArgumentException("Habit title must be up to 20 characters");
        }
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    /**
     * @param reason reason why the habit is created
     * @throws IllegalArgumentException if habit reason is too long
     */
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

    /**
     * Return a unique habit id.
     *
     * Note: Whenever title, reason, or startDate changes, the habit id changes.
     *
     * @return (int) unique habit id
     */
    public int getHabitId() {
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        return String.format("%s%s%s", title, reason, formatter.format(startDate)).hashCode();
    }
}