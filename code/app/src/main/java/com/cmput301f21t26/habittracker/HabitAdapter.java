package com.cmput301f21t26.habittracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.objects.Habit;

import java.util.ArrayList;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private final ArrayList<Habit> habitList;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTV;
        private final TextView planTV;

        public ViewHolder(View view) {
            super(view);

            titleTV = (TextView) view.findViewById(R.id.habitTitleTV);
            planTV = (TextView) view.findViewById(R.id.habitPlanTV);
        }

        public TextView getTitleTV() {
            return titleTV;
        }

        public TextView getPlanTV() {
            return planTV;
        }
    }

    /**
     * Initialize the dataset of the HabitAdapter
     *
     * @param habitList ArrayList<Habit> contains the Habit object to populate views to be
     *                  used by RecyclerView
     */
    public HabitAdapter(ArrayList<Habit> habitList) {
        this.habitList = habitList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new CardView, which defines the UI of the habit item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_content, parent, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        Habit habit = habitList.get(position);

        holder.getTitleTV().setText(habit.getTitle());
        // TODO update after HabitPlan is implemented
        holder.getPlanTV().setText("");

        // TODO update progress bar
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return habitList.size();
    }




}
