package com.cmput301f21t26.habittracker.objects;

import android.location.Location;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class HabitEvent implements Serializable {

    private String comment;
    private Location loc;
    private String photoUrl;
    private Date hEventDate;
    private final String habitEventId;
    private final String parentHabitId;
    private String title;

    /**
     * Default constructor of HabitEvent
     *
     * @param parentHabitId id of habit that owns this habit event
     * @param comment optional string comment up to 20 characters
     * @param loc optional location information
     * @param photoUrl optional photo url, which is obtained from StorageReference.getDownloadUrl();
     * @param hEventDate date at which the event occurred
     * @param title habit event title
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String parentHabitId, String comment, @Nullable Location loc, String photoUrl, Date hEventDate, String title) {
        if (comment.length() > 20) {
            throw new IllegalArgumentException("Habit event comment must be up to 20 characters");
        }
        this.comment = comment;
        this.loc = loc;
        this.photoUrl = photoUrl;
        this.hEventDate = hEventDate;
        this.habitEventId = UUID.randomUUID().toString();
        this.parentHabitId = parentHabitId;
        this.title = title;
    }

    /**
     * A constructor for the case where the location and the photograph are not given.
     * In this case, both are set to null.
     *
     * @param parentHabitId id of habit that owns this habit event
     * @param comment optional string comment up to 20 characters
     * @param hEventDate date at which the event occurred
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String parentHabitId, String comment, Date hEventDate) {
        this(parentHabitId, comment, null, null, hEventDate, "");
    }

    /**
     * A constructor for the case where the location and habit event date are not given.
     * In this case, location and photo uri are set to null and the date is set
     * to the time where the constructor is invoked.
     *
     * @param parentHabitId id of habit that owns this habit event
     * @param comment optional string comment up to 20 characters
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String parentHabitId, String comment) {
        this(parentHabitId, comment, null, null, Calendar.getInstance().getTime(), "");
    }

    /**
     *
     * @param parentHabitId id of habit that owns this habit event
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent(String parentHabitId) {
        this(parentHabitId, "", null, null, Calendar.getInstance().getTime(), "");
    }

    /**
     * A constructor for creating an empty habit event.
     * The default comment is an empty string, the default location and photo uri are null, and
     * the default habit event date is the time where the constructor is invoked.
     * The default title is an empty string.
     *
     * @throws IllegalArgumentException if habit event comment is too long
     */
    public HabitEvent() {
        this(null, "", null, null, Calendar.getInstance().getTime(),"");
    }

    public String getParentHabitId() {
        return parentHabitId;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Instant getHabitEventDateDay(){
        return this.hEventDate.toInstant().truncatedTo(ChronoUnit.DAYS);
    }
}
