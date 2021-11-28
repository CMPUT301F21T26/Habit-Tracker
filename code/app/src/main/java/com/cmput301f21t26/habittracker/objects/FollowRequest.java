package com.cmput301f21t26.habittracker.objects;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * FollowRequest class represents a message sent by a user to follow another user
 */
public class FollowRequest implements Serializable {

    private String fromUid;
    private String toUid;
    private Date dateSent;
    private String id;
    private String senderProfilePictureUrl;

    public FollowRequest() { }

    public FollowRequest(User fromUser, User toUser) {
        this.fromUid = fromUser.getUid();
        this.toUid = toUser.getUid();
        this.dateSent = Calendar.getInstance().getTime();
        this.id = UUID.randomUUID().toString();
        this.senderProfilePictureUrl = fromUser.getPictureURL();
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

    public String getSenderProfilePictureUrl() {
        return senderProfilePictureUrl;
    }
}
