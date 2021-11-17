package com.cmput301f21t26.habittracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.objects.FollowRequest;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowRequestListAdapter extends BaseAdapter {
    private String TAG = "PermissionListAdapter";
    private ArrayList<FollowRequest> permissionsList;
    private Context mContext;

    public FollowRequestListAdapter(Context context, ArrayList<FollowRequest> permissionsList) {
        this.mContext = context;
        this.permissionsList = permissionsList;
    }

    @Override
    public int getCount() {
        return permissionsList.size();
    }

    @Override
    public FollowRequest getItem(int i) {
        return permissionsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.permission_content, viewGroup);
        }

        FollowRequest tempFollowRequest = (FollowRequest) getItem(position);

        TextView permissionUsernameTV = (TextView) view.findViewById(R.id.permissionUsernameTV);
        CircleImageView profilePicPermissionImageView = (CircleImageView) view.findViewById(R.id.profilePicPermissionImageView);
        Button allowButton = (Button) view.findViewById(R.id.allowButton);
        Button denyButton = (Button) view.findViewById(R.id.denyButton);

        permissionUsernameTV.setText(tempFollowRequest.getFromUid());
        if (tempFollowRequest.getPictureURL() != null && mContext != null) {
            Glide.with(mContext)
                    .load(tempFollowRequest.getPictureURL())
                    .into(profilePicPermissionImageView);
        }

        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Allow the user to follow; Add user to followers in database, delete permission request in database
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Deny the user the follow; delete permission request in database
            }
        });

        return view;
    }
}
