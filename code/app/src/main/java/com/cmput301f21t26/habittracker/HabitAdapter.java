package com.cmput301f21t26.habittracker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
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
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserCallback;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> implements Observer {

    private final String TAG = "HabitAdapter";
    private final Activity activity;
    private final ArrayList<Habit> habitList;
    private int mVisibility = View.VISIBLE;
    private RecyclerViewClickListener listener;
    private final String userid;
    private Context mContext;

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

            titleTV = view.findViewById(R.id.habitTitleTV);
            planTV = view.findViewById(R.id.habitPlanTV);
            doneTodayCB = view.findViewById(R.id.habitCheckbox);
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

        UserController.addObserverToCurrentUser(this);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new CardView, which defines the UI of the habit item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_content, parent, false);

        // Set context
        mContext = parent.getContext();
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Changes the checkbox visibility depending on mVisibility
        holder.getDoneTodayCB().setVisibility(mVisibility);

        Habit habit = habitList.get(position);

        holder.getTitleTV().setText(habit.getTitle());
        holder.getPlanTV().setText(getPlanMsg(habit));
        holder.getDoneTodayCB().setChecked(habit.isDoneForToday());
        holder.getDoneTodayCB().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean isChecked) {
                UserController.updateDoneForTodayInDb(habit, isChecked, user -> {

                    if (isChecked) {

                        HabitEvent hEvent = new HabitEvent();

                        // Get the date for use in title
                        String datePattern = "yyyy-MM-dd";
                        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
                        String habitEventDateFormat = format.format(hEvent.getHabitEventDate());
                        // Set the title of the habit event
                        hEvent.setTitle(mContext.getString(R.string.habit_event_title, habit.getTitle(), habitEventDateFormat));

                        UserController.storeHabitEventInDb(habit.getHabitId(), hEvent, new UserCallback() {
                            @Override
                            public void onCallback(User user) {
                                // show snackbar after storing an habit event in db
                                Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.mainActivityConstraintLayout),
                                        "Empty habit event is created", Snackbar.LENGTH_SHORT);
                                snackbar.setAnchorView(activity.findViewById(R.id.addHabitButton));
                                snackbar.setAction("EDIT", view -> {

                                    NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_activity_main);
                                    @NonNull NavDirections direction = MobileNavigationDirections.actionGlobalEditHabitEventFragment(hEvent, habit);

                                    navController.navigate(direction);
                                });
                                snackbar.show();
                            };
                        });
                    }

                });

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

    @Override
    public void update(Observable observable, Object o) {
        notifyDataSetChanged();
    }
}
