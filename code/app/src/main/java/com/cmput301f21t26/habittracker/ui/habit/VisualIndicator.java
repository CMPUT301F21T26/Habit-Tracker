package com.cmput301f21t26.habittracker.ui.habit;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

import com.cmput301f21t26.habittracker.objects.Habit;

public class VisualIndicator extends ProgressBar {

    public VisualIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void updateProgress(Habit habit){
        Log.d("visual", String.valueOf(Math.round(habit.getVisualRatio()*this.getMax())));
        this.setProgress(Math.round(habit.getVisualRatio()*this.getMax()));
    }

}
