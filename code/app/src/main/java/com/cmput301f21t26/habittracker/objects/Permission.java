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

    public Permission(String fromUid, String toUid, Date dateSent) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.dateSent = dateSent;
        this.id = UUID.randomUUID().toString();
    }

    public Permission(String fromUid, String toUid) {
        this(fromUid, toUid, Calendar.getInstance().getTime());       // dateSent = date now
    }
}
