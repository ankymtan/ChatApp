package com.ankymtan.couplechat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

import java.util.ArrayList;

/**
 * Created by ankym on 8/8/2015.
 */
public class AdapterFriendListChoser extends ArrayAdapter<String> {

    ArrayList<String> friendNames;
    Context context;

    public AdapterFriendListChoser(Context context, ArrayList<String> friendNames){
        super(context, R.layout.item_friend);
        this.friendNames = friendNames;
        this.context = context;
    }

    @Override
    public int getCount() {
        return friendNames.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FriendViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_friend, parent, false);

            viewHolder = new FriendViewHolder();
            viewHolder.friendName = (TextView) convertView.findViewById(R.id.tv_added_friend_name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (FriendViewHolder) convertView.getTag();
        }

        viewHolder.friendName.setText(friendNames.get(position));
        return convertView;
    }

    class FriendViewHolder{
        TextView friendName;
    }
}
