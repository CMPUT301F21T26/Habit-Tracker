package com.cmput301f21t26.habittracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.Permission;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PermissionListAdapter extends BaseAdapter {
    private String TAG = "PermissionListAdapter";
    private ArrayList<Permission> permissionsList;
    private Context mContext;

    public PermissionListAdapter(Context context, ArrayList<Permission> permissionsList) {
        this.mContext = context;
        this.permissionsList = permissionsList;
    }

    @Override
    public int getCount() {
        return permissionsList.size();
    }

    @Override
    public Permission getItem(int i) {
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

        Permission tempPermission = (Permission) getItem(position);

        TextView permissionUsernameTV = (TextView) view.findViewById(R.id.permissionUsernameTV);
        CircleImageView profilePicPermissionImageView = (CircleImageView) view.findViewById(R.id.profilePicPermissionImageView);
        Button allowButton = (Button) view.findViewById(R.id.allowButton);
        Button denyButton = (Button) view.findViewById(R.id.denyButton);

        permissionUsernameTV.setText(tempPermission.getFromUid());
        if (tempPermission.getPictureURL() != null && mContext != null) {
            Glide.with(mContext)
                    .load(tempPermission.getPictureURL())
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
