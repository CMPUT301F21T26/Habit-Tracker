package com.cmput301f21t26.habittracker.interfaces;

import com.cmput301f21t26.habittracker.objects.FollowRequest;

/**
 * call back method for follow request
 */
public interface FollowRequestCallback {
    void onCallback(FollowRequest followRequest);
}
