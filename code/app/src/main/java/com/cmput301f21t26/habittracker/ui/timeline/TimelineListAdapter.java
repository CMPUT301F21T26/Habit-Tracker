package com.cmput301f21t26.habittracker.ui.timeline;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class TimelineListAdapter extends BaseAdapter {
    ArrayList<HabitEvent> hEventsList;
    Context mContext;

    public TimelineListAdapter(Context context, ArrayList<HabitEvent> habitEvents) {
        this.mContext = context;
        this.hEventsList = habitEvents;
    }

    @Override
    public int getCount() {
        return hEventsList.size();
    }

    @Override
    public Object getItem(int position) {
        return hEventsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.habit_event_content, parent, false);
        }

        HabitEvent tempHEvent = (HabitEvent) getItem(position);
        Habit tempHabit = (Habit) getItem(position);

        TextView habitEventTitleTV = (TextView) convertView.findViewById(R.id.habitEventTitleTV);
        TextView habitEventLocationTV = (TextView) convertView.findViewById(R.id.habitEventLocationTV);
        TextView habitEventCommentTV = (TextView) convertView.findViewById(R.id.habitEventCommentTV);
        ImageView habitEventImageView = (ImageView) convertView.findViewById(R.id.habitEventImage);

        // Assign habit event's attributes to the corresponding views
        if (tempHEvent.getLocation() == null) {
            habitEventLocationTV.setText("");
        } else {
            habitEventLocationTV.setText(tempHEvent.getLocation().toString());
        }
        habitEventCommentTV.setText(tempHEvent.getComment());
        if (tempHEvent.getImageURI() == null) {
            habitEventImageView.setImageResource(R.color.transparent);
        } else {
            habitEventImageView.setImageURI(tempHEvent.getImageURI());
        }

        return convertView;
    }
}
