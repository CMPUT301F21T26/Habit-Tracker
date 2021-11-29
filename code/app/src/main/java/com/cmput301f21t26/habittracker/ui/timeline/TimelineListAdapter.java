package com.cmput301f21t26.habittracker.ui.timeline;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.UserController;

import java.util.ArrayList;

/**
 * The list view adapter for the timeline fragment;
 * It displays the user's habit events and will
 * adapt the size of the item depending on the
 * available info.
 */
public class TimelineListAdapter extends BaseAdapter {
    private String TAG = "TimelineListAdapter";
    private ArrayList<HabitEvent> hEventsList;
    private Context mContext;
    private float dpRatio;

    private UserController userController;

    public TimelineListAdapter(Context context, ArrayList<HabitEvent> habitEvents) {
        this.mContext = context;
        this.hEventsList = habitEvents;
        if (mContext != null) {
            dpRatio = mContext.getResources().getDisplayMetrics().density;
        }
        userController = UserController.getInstance();

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

        TextView habitEventTitleTV = (TextView) convertView.findViewById(R.id.habitEventTitleTV);
        TextView locationTV = (TextView) convertView.findViewById(R.id.locationTV);
        TextView habitEventCommentTV = (TextView) convertView.findViewById(R.id.habitEventCommentTV);
        ImageView habitEventImageView = (ImageView) convertView.findViewById(R.id.habitEventImage);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.commentLayout);
        LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.habitEventTopLayout);


        // Assign habit event's attributes to the corresponding views
        habitEventTitleTV.setText(tempHEvent.getTitle());
        if (tempHEvent.getAddress() == null) {
            locationTV.setText("");

            // Change bottom margin of the top linear layout that houses the title and location
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
            params2.bottomMargin = 0;
        } else {
            locationTV.setText(tempHEvent.getAddress());
        }
        if (tempHEvent.getComment().isEmpty()) {
            // When there is no comment, get rid of it so content only shows habit title
            habitEventCommentTV.setText("");
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
            params.topMargin = 0;
            params.bottomMargin = 0;
            params.leftMargin = 0;
            params.rightMargin = 0;
            params.removeRule(RelativeLayout.BELOW);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);


        } else {
            habitEventCommentTV.setText(tempHEvent.getComment());
        }

        if (tempHEvent.getPhotoUrl() == null) {
            // If photo DNE then make it transparent and move comment up
            habitEventImageView.setImageResource(R.color.transparent);
            habitEventImageView.setMaxHeight(0);
            habitEventImageView.setMinimumHeight(0);

            // Move comment up
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.habitEventTopLayout);

            // If location doesn't exist then move the comment up close to title for looks
            if (tempHEvent.getAddress() == null) {
                params.topMargin = 0;
            }

        } else {
            if (mContext != null) {
                Glide.with(mContext)
                        .load(tempHEvent.getPhotoUrl())
                        .into(habitEventImageView);
                Log.d(TAG, "Get image success");
            }
        }

        return convertView;
    }
}
