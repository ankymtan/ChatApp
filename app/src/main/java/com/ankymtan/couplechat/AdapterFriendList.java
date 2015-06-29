package com.ankymtan.couplechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

import java.util.ArrayList;

/**
 * Created by ankym on 28/6/2015.
 */
public class AdapterFriendList extends ArrayAdapter<User>{

    private ArrayList<User> friendList;
    private Context context;
    private RelativeLayout itemFriend;

    public AdapterFriendList(ArrayList<User> friendList, Context context){
        super(context, R.layout.item_friend);
        this.friendList = friendList;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_friend, parent, false);
        }
        // Now we can fill the layout with the right values
        itemFriend = (RelativeLayout) convertView.findViewById(R.id.item_friend_layout);
        TextView friendName = (TextView) convertView.findViewById(R.id.current_friend_name);
        User friend = friendList.get(position);
        friendName.setText(""+friend.getName());


        return convertView;
    }

    @Override
    public int getCount() {
        return friendList.size();
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
}
