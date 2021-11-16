package com.cmput301f21t26.habittracker.objects;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Permission class represents a message sent by a user to follow another user
 */
public class Permission {

    private final String fromUid;
    private final String toUid;
    private final Date dateSent;
    private final String id;
    private final String pictureURL;

    public Permission(String fromUid, String toUid, Date dateSent, String pictureURL) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.dateSent = dateSent;
        this.id = UUID.randomUUID().toString();
        this.pictureURL = pictureURL;
    }

    public Permission(String fromUid, String toUid, String pictureURL) {
        this(fromUid, toUid, Calendar.getInstance().getTime(), pictureURL);       // dateSent = date now
    }

    public String getFromUid() {
        return fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public String getId() {
        return id;
    }

    public String getPictureURL() {
        return pictureURL;
    }
}
