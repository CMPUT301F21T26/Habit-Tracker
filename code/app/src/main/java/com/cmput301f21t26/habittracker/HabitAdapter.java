package com.cmput301f21t26.habittracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.objects.Habit;

import java.util.ArrayList;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private final ArrayList<Habit> habitList;
    private int mVisibility = View.VISIBLE;
    private RecyclerViewClickListener listener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleTV;
        private final TextView planTV;

        public ViewHolder(View view) {
            super(view);

            titleTV = (TextView) view.findViewById(R.id.habitTitleTV);
            planTV = (TextView) view.findViewById(R.id.habitPlanTV);
            view.setOnClickListener(this);
        }

        public TextView getTitleTV() {
            return titleTV;
        }

        public TextView getPlanTV() {
            return planTV;
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }

    /**
     * Initialize the dataset of the HabitAdapter
     *
     * @param habitList ArrayList<Habit> contains the Habit object to populate views to be
     *                  used by RecyclerView
     */
    public HabitAdapter(ArrayList<Habit> habitList, RecyclerViewClickListener listener) {
        this.habitList = habitList;
        this.listener = listener;
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
        // Changes the checkbox visibility depending on mVisibility
        holder.itemView.findViewById(R.id.habitCheckbox).setVisibility(mVisibility);

        Habit habit = habitList.get(position);

        holder.getTitleTV().setText(habit.getTitle());
        holder.getPlanTV().setText(getPlanMsg(habit));


        // TODO update progress bar
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return habitList.size();
    }

    /**
     * Changes the visibility of the check box in the habit item UI
     * @param visibility
     *  the visibility (View.VISIBLE, View.INVISIBLE, View.GONE)
     */
    public void checkBoxVisibility(int visibility) {
        mVisibility = visibility;
    }

    // Implementing onClick
    public interface RecyclerViewClickListener {
        void onClick(View view, int position);
    }

    private String getPlanMsg(Habit habit) {
        StringBuilder planMsg = new StringBuilder();
        for (int i = 0; i < habit.getDaysList().size(); i++) {
            int day = habit.getDaysList().get(i);
            if (day == 0) {
                planMsg.append("Sun, ");
            }
            if (day == 1) {
                planMsg.append("Mon, ");
            }
            if (day == 2) {
                planMsg.append("Tue, ");
            }
            if (day == 3) {
                planMsg.append("Wed, ");
            }
            if (day == 4) {
                planMsg.append("Thu, ");
            }
            if (day == 5) {
                planMsg.append("Fri, ");
            }
            if (day == 6) {
                planMsg.append("Sat, ");
            }
        }

        String planMsgStr = planMsg.toString();
        if (planMsgStr.length() > 0) {
            planMsgStr = planMsgStr.substring(0, planMsgStr.length() - 2);
        }

        return planMsgStr;
    }


}
