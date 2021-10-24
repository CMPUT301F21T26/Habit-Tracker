package com.cmput301f21t26.habittracker.objects;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Habit {

    private String title;
    private String reason;
    private Date startDate;
    private boolean isDoneForToday;
    private final String datePattern = "yyyy-MM-dd_HH:mm:ss";
    private List<HabitEvent> habitEvents;
    private HabitPlan plan;

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
        this.habitEvents = new ArrayList<>();
        this.plan = null;
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

    /**
     * Store the given habit event into the list.
     *
     * @param hEvent habit event to store
     */
    public void addHabitEvent(HabitEvent hEvent) {
        habitEvents.add(hEvent);
    }

    /**
     * Remove a habit event stored in the given index.
     *
     * @param index position at which the target habit event is stored
     */
    public void deleteHabitEvent(int index) {
        habitEvents.remove(index);
    }

    /**
     * Return the total number of habit events associated with the habit.
     *
     * @return (int) the total number of habit events
     */
    public int numTotalHabitEvents() {
        return habitEvents.size();
    }

    // TODO methods for HabitPlan



}