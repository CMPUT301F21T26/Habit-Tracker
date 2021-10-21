package com.cmput301f21t26.habittracker;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class Habit {

    private String title;
    private String reason;
    private LocalDate startDate;

    public Habit(String title, String reason, LocalDate startDate) {
        this.title = title;
        this.reason = reason;
        this.startDate = startDate;
    }

    public Habit(String title) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this(title, "", LocalDate.now());
        } else {
            Date startDate = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");


        }
    }
}
