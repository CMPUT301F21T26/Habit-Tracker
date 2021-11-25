package com.cmput301f21t26.habittracker.objects;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Habit class stores all habit-related data and associated habit events.
 */
public class Habit implements Serializable {

    private String habitId;
    private String title;
    private String reason;
    private Date startDate;
    private boolean isDoneForToday;
    private boolean isPrivate;
    @Exclude
    private ArrayList<HabitEvent> habitEvents;
    private ArrayList<Integer> daysList;
    private int habitPosition;

    /**
     * A default Habit constructor.
     *
     * @param title habit title
     * @param reason reason why the habit is created
     * @param startDate date when the habit was created
     * @throws IllegalArgumentException if habit title or reason are too long
     */
    public Habit(String title, String reason, Date startDate, ArrayList<Integer> daysList) throws IllegalArgumentException {
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
        this.daysList = daysList;     // all init to false
        this.habitId = UUID.randomUUID().toString();
        this.isPrivate = false;
        this.habitPosition = 0;
    }

    /**
     * Empty no-argument constructor required for deserialization
     */
    public Habit() {
        this("", "", Calendar.getInstance().getTime(), new ArrayList<>());
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
        this(title, "", startDate, new ArrayList<>());
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
        this(title, reason, Calendar.getInstance().getTime(), new ArrayList<>());
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
        this(title, "", Calendar.getInstance().getTime(), new ArrayList<>());
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
     * @return (String) unique habit id
     */
    public String getHabitId() {
        return habitId;
    }

    @Exclude
    public List<HabitEvent> getHabitEvents() {
        return habitEvents;
    }

    /**
     * Given a date, return a habit event object at that date
     * Return null if no such object is found
     *
     * @param date the date to search for
     * @return Habit event on given date, if it exists
     */
    public HabitEvent getHabitEventByDate(Date date){
        for (int i=0; i<habitEvents.size(); i++) {
            if (habitEvents.get(i).getHabitEventDateDay().equals(date.toInstant().truncatedTo(ChronoUnit.DAYS))){
                return habitEvents.get(i);
            }
        }
        return null;
    }

    /**
     * Add the given habit event into the list.
     *
     * @param hEvent habit event to store
     */
    public void addHabitEvent(HabitEvent hEvent) {
        habitEvents.add(hEvent);
    }

    /**
     * Remove a habit event stored in the given index.
     *
     * @param hEvent habit event to remove from the list
     */
    public void deleteHabitEvent(HabitEvent hEvent) {
        for (int i=0; i<habitEvents.size(); i++) {
            if (hEvent.getHabitEventId().equals(habitEvents.get(i).getHabitEventId())) {
                habitEvents.remove(i);
                return;
            }
        }
    }

    /**
     * Update an existing habit event to a given habit event
     *
     * @param hEvent updated habit event
     */
    public void updateHabitEvent(HabitEvent hEvent) {
        for (int i=0; i<habitEvents.size(); i++) {
            if (hEvent.getHabitEventId().equals(habitEvents.get(i).getHabitEventId())) {
                habitEvents.set(i, hEvent);
                return;
            }
        }
    }

    /**
     * Return the total number of habit events associated with the habit.
     *
     * @return (int) the total number of habit events
     */
    public int numTotalHabitEvents() {
        return habitEvents.size();
    }

    public void setDaysList(ArrayList<Integer> daysList) {
        this.daysList = daysList;
    }

    public ArrayList<Integer> getDaysList() {
        return daysList;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public int getHabitPosition() {
        return habitPosition;
    }

    public void setHabitPosition(int habitPosition) {
        this.habitPosition = habitPosition;
    }
}