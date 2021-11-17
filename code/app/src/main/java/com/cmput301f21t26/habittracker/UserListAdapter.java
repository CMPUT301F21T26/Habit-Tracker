package com.cmput301f21t26.habittracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.objects.User;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends BaseAdapter {
    private String TAG = "UserListAdapter";
    private ArrayList<User> usersList;
    private Context mContext;

    public UserListAdapter(Context context, ArrayList<User> usersList) {
        this.mContext = context;
        this.usersList = usersList;
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public User getItem(int i) {
        return usersList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.user_item_content, viewGroup, false);
        }
        User otherUser = getItem(i);

        String otherUsername = otherUser.getUsername();

        TextView userUsernameTV = view.findViewById(R.id.userUsernameTV);
        CircleImageView userProfilePic = view.findViewById(R.id.userProfilePic);

        userUsernameTV.setText(otherUsername);
        // Set profile pic
        Log.d(TAG, otherUser.getUsername());
        String pictureURL = otherUser.getPictureURL();
        if (mContext != null && pictureURL != null) {
            Glide.with(mContext)
                    .load(otherUser.getPictureURL())
                    .into(userProfilePic);
            Log.d(TAG, "Get image success");
        }

        return view;
    }
}
