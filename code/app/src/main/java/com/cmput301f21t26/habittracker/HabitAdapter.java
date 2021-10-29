package com.cmput301f21t26.habittracker;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.objects.Habit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {

    private final String TAG = "HabitAdapter";
    private final Activity activity;
    private final ArrayList<Habit> habitList;
    private int mVisibility = View.VISIBLE;
    private RecyclerViewClickListener listener;
    private final String userid;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView titleTV;
        private final TextView planTV;
        private final CheckBox doneTodayCB;

        public ViewHolder(View view) {
            super(view);

            titleTV = (TextView) view.findViewById(R.id.habitTitleTV);
            planTV = (TextView) view.findViewById(R.id.habitPlanTV);
            doneTodayCB = (CheckBox) view.findViewById(R.id.habitCheckbox);
            view.setOnClickListener(this);
        }

        public TextView getTitleTV() {
            return titleTV;
        }

        public TextView getPlanTV() {
            return planTV;
        }

        public CheckBox getDoneTodayCB() {
            return doneTodayCB;
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
    }

    public class OnEditListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_activity_main);
            @NonNull NavDirections direction = MobileNavigationDirections.actionGlobalEditHabitEventFragment();
            navController.navigate(direction);
        }
    }

    /**
     * Initialize the dataset of the HabitAdapter
     *
     * @param habitList ArrayList<Habit> contains the Habit object to populate views to be
     *                  used by RecyclerView
     */
    public HabitAdapter(ArrayList<Habit> habitList, Activity activity, RecyclerViewClickListener listener, String userid) {
        this.activity = activity;
        this.habitList = habitList;
        this.listener = listener;
        this.userid = userid;
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
        holder.getDoneTodayCB().setChecked(habit.isDoneForToday());
        holder.getDoneTodayCB().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                habit.setDoneForToday(isChecked);
                updateDoneTodayInDb(habit, isChecked);

                if (isChecked) {

                    Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.mainActivityConstraintLayout),
                            "Empty habit event is created", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("EDIT", new OnEditListener());
                    snackbar.show();
                }
            }
        });




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

    private void updateDoneTodayInDb(Habit habit, boolean isDoneToday) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference habitsRef = db.collection("users").document(userid).collection("habits");
        int dayToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;        // Calendar starts the first day as 1, not 0.

        habitsRef.document(habit.getHabitId())
                .update("doneForToday", isDoneToday)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Habit doneForToday successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating habit doneForToday", e);
                    }
                });
    }

}
