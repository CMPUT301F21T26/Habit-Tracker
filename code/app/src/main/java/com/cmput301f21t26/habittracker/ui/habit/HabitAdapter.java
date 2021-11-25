package com.cmput301f21t26.habittracker.ui.habit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitController;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.HabitEventController;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.interfaces.UserCallback;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> implements Observer, ItemTouchHelperAdapter {

    private final String TAG = "HabitAdapter";
    private final Activity activity;
    private final ArrayList<Habit> habitList;
    private int mVisibility = View.VISIBLE;
    private RecyclerViewClickListener listener;
    private Context mContext;
    private ItemTouchHelper touchHelper;

    private HabitController habitController;
    private HabitEventController habitEventController;

    /**
     * Initialize the dataset of the HabitAdapter
     *
     * @param habitList ArrayList<Habit> contains the Habit object to populate views to be
     *                  used by RecyclerView
     */
    public HabitAdapter(ArrayList<Habit> habitList, Activity activity, RecyclerViewClickListener listener) {
        this.activity = activity;
        this.habitList = habitList;
        this.listener = listener;

        UserController.addObserverToCurrentUser(this);

        habitController = HabitController.getInstance();
        habitEventController = HabitEventController.getInstance();
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
        holder.getDoneTodayCB().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!holder.getDoneTodayCB().isChecked()) {
                    // prompt user to approve deletion of habit event if habit is already checked
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle("Uncheck Habit")
                            .setMessage("Are you sure you want to uncheck this habit? The corresponding habit event created for today will be deleted.")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // update DB
                                    habitEventController.removeHabitEventFromDb(habit.getHabitEventByDate(Calendar.getInstance().getTime()), callback -> { });
                                    holder.getDoneTodayCB().setChecked(false);
                                    habit.setDoneForToday(false);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    holder.getDoneTodayCB().setChecked(true);
                                    dialog.cancel();
                                }
                            })
                            .show();

                    // Change buttons and background
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.notif_panel_background);
                    Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                    layoutParams.weight = 10;
                    btnPositive.setLayoutParams(layoutParams);
                    btnNegative.setLayoutParams(layoutParams);
                    btnPositive.setTypeface(ResourcesCompat.getFont(mContext, R.font.rubik_black));
                    btnNegative.setTypeface(ResourcesCompat.getFont(mContext, R.font.rubik_black));
                    btnPositive.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                    btnNegative.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                } else {
                    // if check box is unchecked, set habit's done for today to true and update it in db
                    habit.setDoneForToday(true);
                    holder.getDoneTodayCB().setChecked(true);
                    habitController.updateHabitInDb(habit, user -> {
                        HabitEvent hEvent = new HabitEvent(habit.getHabitId());

                        // Get the date for use in title
                        String datePattern = "yyyy-MM-dd";
                        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
                        String habitEventDateFormat = format.format(hEvent.getHabitEventDate());
                        // Set the title of the habit event
                        hEvent.setTitle(mContext.getString(R.string.habit_event_title, habit.getTitle(), habitEventDateFormat));

                        habitEventController.storeHabitEventInDb(hEvent, new UserCallback() {
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
                            }
                        });
                    });
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

    // When habit item is dragged, change its position in the list
    // in the database.
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Habit fromHabit = habitList.get(fromPosition);
        habitList.remove(fromHabit);
        habitList.add(toPosition, fromHabit);
        notifyItemMoved(fromPosition, toPosition);
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
        void onClick(int position);
    }

    /**
     * Sets the habit adapter's ItemTouchHelper
     * @param touchHelper
     *  The {@link ItemTouchHelper} to be attached to the habit adapter
     */
    public void setItemTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnTouchListener,
            GestureDetector.OnGestureListener {
        private final TextView titleTV;
        private final TextView planTV;
        private final CheckBox doneTodayCB;
        GestureDetector gestureDetector;

        public ViewHolder(View view) {
            super(view);

            titleTV = view.findViewById(R.id.habitTitleTV);
            planTV = view.findViewById(R.id.habitPlanTV);
            doneTodayCB = view.findViewById(R.id.habitCheckbox);
            gestureDetector = new GestureDetector(view.getContext(), this);

            view.setOnTouchListener(this);
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
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        // This is basically onClick
        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            listener.onClick(getAdapterPosition());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            touchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            return true;
        }
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
