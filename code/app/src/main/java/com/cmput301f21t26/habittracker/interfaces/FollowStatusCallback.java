package com.cmput301f21t26.habittracker.interfaces;

import com.cmput301f21t26.habittracker.objects.FollowStatus;

/**
 * call back method for FollowStatus
 */
public interface FollowStatusCallback {
    void onCallback(FollowStatus status);
}
