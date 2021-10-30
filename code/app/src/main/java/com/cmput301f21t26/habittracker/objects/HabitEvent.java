package com.cmput301f21t26.habittracker.objects;

import android.location.Location;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class HabitEvent implements Serializable {

    private String comment;
    private Location loc;
    private Uri photoUri;
    private Date hEventDate;
    private final String habitEventId;

    /**
     * Default constructor of HabitEvent
     *
     * @param comment optional string comment up to 20 characters
     * @param loc optional location information
     * @param photoUri optional photo uri
     * @param hEventDate date at which the event occurred
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String comment, @Nullable Location loc, @Nullable Uri photoUri, Date hEventDate) {
        if (comment.length() > 20) {
            throw new IllegalArgumentException("Habit event comment must be up to 20 characters");
        }
        this.comment = comment;
        this.loc = loc;
        this.photoUri = photoUri;
        this.hEventDate = hEventDate;
        this.habitEventId = UUID.randomUUID().toString();
    }

    /**
     * A constructor for the case where the location and the photograph are not given.
     * In this case, both are set to null.
     *
     * @param comment optional string comment up to 20 characters
     * @param hEventDate date at which the event occurred
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String comment, Date hEventDate) {
        this(comment, null, null, hEventDate);
    }

    /**
     * A constructor for the case where the location and habit event date are not given.
     * In this case, location and photo uri are set to null and the date is set
     * to the time where the constructor is invoked.
     *
     * @param comment optional string comment up to 20 characters
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String comment) {
        this(comment, null, null, Calendar.getInstance().getTime());
    }

    /**
     * A constructor for creating an empty habit event.
     * The default comment is an empty string, the default location and photo uri are null, and
     * the default habit event date is the time where the constructor is invoked.
     *
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent() {
        this("", null, null, Calendar.getInstance().getTime());
    }

    public String getComment() {
        return comment;
    }

    /**
     * Sets habit event comment
     *
     * @param comment optional string comment up to 20 characters
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public void setComment(String comment) {
        if (comment.length() > 20) {
            throw new IllegalArgumentException("Habit event comment must be up to 20 characters");
        }
        this.comment = comment;
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public Date getHabitEventDate() {
        return hEventDate;
    }

    public void setHabitEventDate(Date hEventDate) {
        this.hEventDate = hEventDate;
    }

    public String getHabitEventId() {
        return habitEventId;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }
}
