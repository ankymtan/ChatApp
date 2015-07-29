package com.ankymtan.couplechat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ankymtan.couplechat.activity.ActivityEditProfile;
import com.ankymtan.couplechat.entity.User;
import com.ankymtan.couplechat.framework.ProfileManager;
import com.ankymtan.couplechat.framework.UserLocal;
import com.github.nkzawa.socketio.androidchat.R;

import java.util.ArrayList;

/**
 * Created by ankym on 28/6/2015.
 */
public class AdapterFriendList extends ArrayAdapter<User> implements View.OnClickListener {

    private final static int VIEW_TYPE_PROFILE_PANEL = 0;
    private final static int VIEW_TYPE_FRIEND_ITEM = 1;

    private ArrayList<User> friendList;
    private Context context;
    private ProfileManager profileManager;
    private UserLocal userLocal;


    public AdapterFriendList(ArrayList<User> friendList, Context context) {
        super(context, R.layout.item_friend);
        this.friendList = friendList;
        this.context = context;
        this.profileManager = new ProfileManager(context);
        this.userLocal = new UserLocal(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        switch (getItemViewType(position)) {
            case VIEW_TYPE_PROFILE_PANEL:
                if(convertView != null) {
                    viewHolder = (ViewHolderItem) convertView.getTag();
                }else{
                    viewHolder = new ViewHolderItem();
                };

                if (convertView == null|| viewHolder.ivProfile == null) {
                    viewHolder = new ViewHolderItem();
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    //setup viewHolder for profile panel
                    convertView = inflater.inflate(R.layout.item_profile_panel, parent, false);

                    viewHolder.ivProfile = (ImageView) convertView.findViewById(R.id.iv_profile_pic);
                    viewHolder.tvLogout = (TextView) convertView.findViewById(R.id.logout);
                    viewHolder.tvEditAccount = (TextView) convertView.findViewById(R.id.edit_account);
                    viewHolder.profileUsername = (TextView) convertView.findViewById(R.id.profile_username);

                    viewHolder.tvLogout.setOnClickListener(this);
                    viewHolder.tvEditAccount.setOnClickListener(this);

                    convertView.setTag(viewHolder);
                }

                //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //convertView = inflater.inflate(R.layout.item_profile_panel, parent, false);

                viewHolder.profileUsername.setText(userLocal.getLoggedInUser().getName());
                profileManager.lazyLoad(viewHolder.ivProfile, userLocal.getLoggedInUser().getName(), false);

                return convertView;

            case VIEW_TYPE_FRIEND_ITEM:
                viewHolder = (ViewHolderItem) convertView.getTag();
                if (convertView == null || viewHolder.friendName == null) {
                    viewHolder = new ViewHolderItem();
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.item_friend, parent, false);

                    viewHolder.friendName = (TextView) convertView.findViewById(R.id.tv_added_friend_name);
                    viewHolder.tvNewMessage = (TextView) convertView.findViewById(R.id.new_message);
                    viewHolder.ivFriendProfile = (ImageView) convertView.findViewById(R.id.iv_added_friend_profile);
                    convertView.setTag(viewHolder);

                }
                //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //convertView = inflater.inflate(R.layout.item_friend, parent, false);

                User friend = friendList.get(position - 1);
                Log.d("By me", friend.getName() + "  " + friend.getUnreadCounter() + " position: " + position);
                viewHolder.friendName.setText(friend.getName());
                //set unread message counter
                if (friend.getUnreadCounter() == 0) {
                    viewHolder.tvNewMessage.setText("No new message");
                    viewHolder.tvNewMessage.setTextColor(Color.GRAY);
                } else {
                    viewHolder.tvNewMessage.setText(friend.getUnreadCounter() + " new messages");
                }
                profileManager.lazyLoad(viewHolder.ivFriendProfile, friend.getName(), false);

                return convertView;
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return friendList.size() + 1;
    }

    @Override
    public User getItem(int position) {
        return friendList.get(position);
    }

    public void setFriendList(ArrayList<User> friendList2) {
        friendList.clear();
        for (int i = 0; i < friendList2.size(); i++) {
            friendList.add(friendList2.get(i));
        }
    }

    private void logout() {
        userLocal.clearUserData();
        //restart app
        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ((Activity) context).finish();
        context.startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.logout:
                Log.d("by me", "logging out");
                logout();
                break;
            case R.id.edit_account:
                Intent profileEditActivity = new Intent(context, ActivityEditProfile.class);
                context.startActivity(profileEditActivity);
                Log.d("by me", " pressing edit");
                break;
        }
    }

    ;

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_PROFILE_PANEL;
        }
        return VIEW_TYPE_FRIEND_ITEM;
    }

    private class ViewHolderItem {
        TextView tvLogout, tvEditAccount;
        ImageView ivProfile, ivFriendProfile;
        TextView profileUsername;
        TextView friendName;
        TextView tvNewMessage;
    }

}
