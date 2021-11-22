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
import com.cmput301f21t26.habittracker.objects.FollowRequestController;
import com.cmput301f21t26.habittracker.objects.UserController;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowRequestListAdapter extends BaseAdapter implements Observer {

    private String TAG = "PermissionListAdapter";
    private ArrayList<FollowRequest> permissionsList;
    private Context mContext;
    private OnDialogListClickListener dialogListClickListener;

    private FollowRequestController followRequestController;

    /**
     * Allows clicking of follow request in the notification dialog
     */
    public interface OnDialogListClickListener {
        void onItemClick(String username);
    }

    public FollowRequestListAdapter(Context context, ArrayList<FollowRequest> permissionsList, OnDialogListClickListener listener) {
        this.mContext = context;
        this.permissionsList = permissionsList;
        this.dialogListClickListener = listener;

        UserController.addObserverToCurrentUser(this);

        followRequestController = FollowRequestController.getInstance();
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
            view = LayoutInflater.from(mContext).inflate(R.layout.follow_request_content, null);
        }

        FollowRequest tempFollowRequest = (FollowRequest) getItem(position);

        TextView followRequestUsernameTV = (TextView) view.findViewById(R.id.followRequestUsernameTV);
        CircleImageView profilePicFollowRequestImageView = (CircleImageView) view.findViewById(R.id.profilePicFollowRequestImageView);
        Button allowButton = (Button) view.findViewById(R.id.allowButton);
        Button denyButton = (Button) view.findViewById(R.id.denyButton);

        followRequestUsernameTV.setText(tempFollowRequest.getFromUid());
        if (tempFollowRequest.getPictureURL() != null && mContext != null) {
            Glide.with(mContext)
                    .load(tempFollowRequest.getPictureURL())
                    .into(profilePicFollowRequestImageView);
        }

        allowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followRequestController.follow(tempFollowRequest, followRequest -> {
                    followRequestController.undoFollowRequest(tempFollowRequest, user1 -> { });
                });
            }
        });

        denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followRequestController.undoFollowRequest(tempFollowRequest, user -> { });
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogListClickListener.onItemClick(tempFollowRequest.getFromUid());
            }
        });

        return view;
    }

    @Override
    public void update(Observable observable, Object o) {
       notifyDataSetChanged();
    }
}
