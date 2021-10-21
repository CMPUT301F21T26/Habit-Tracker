package com.cmput301f21t26.habittracker.objects;

import java.util.Calendar;
import java.util.Date;

public class Permission {

    private final UserID fromUid;
    private final Date dateSent;

    public Permission(UserID fromUid, Date dateSent) {
        this.fromUid = fromUid;
        this.dateSent = dateSent;
    }

    public Permission(UserID from) {
        this(from, Calendar.getInstance().getTime());       // dateSent = date now
    }
}
