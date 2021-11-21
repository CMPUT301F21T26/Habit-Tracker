package com.cmput301f21t26.habittracker.interfaces;

import com.cmput301f21t26.habittracker.objects.User;

import java.util.ArrayList;

public interface UserListCallback {
    void onCallback(ArrayList<User> listOfUsers);
}
