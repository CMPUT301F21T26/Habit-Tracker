package com.cmput301f21t26.habittracker.objects;

public class UserID {
    private final String uid;

    public UserID(User user) {
        this.uid = user.getUsername();
    }

    public UserID(String username) {
        this.uid = username;
    }

    public String getUid() {
        return uid;
    }

    public static String getUidOf(User user) {
        return user.getUsername();
    }

    @Override
    public String toString() {
        return getUid();
    }
}
