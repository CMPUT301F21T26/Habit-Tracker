package com.cmput301f21t26.habittracker.objects;

import java.util.Calendar;
import java.util.Date;

public class Permission {

    private final String fromUid;
    private final Date dateSent;

    public Permission(String fromUid, Date dateSent) {
        this.fromUid = fromUid;
        this.dateSent = dateSent;
    }

    public Permission(String fromUid) {
        this(fromUid, Calendar.getInstance().getTime());       // dateSent = date now
    }
}
