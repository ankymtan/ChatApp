package com.ankymtan.couplechat.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.ankymtan.couplechat.adapter.AdapterFriendListChoser;
import com.ankymtan.couplechat.framework.MyDatabaseHelper;
import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by ankym on 8/8/2015.
 */
public class ActivityChooseFriend extends AppCompatActivity{

    private static final String LOG_TAG = "by me ";
    ListView friendList;
    AdapterFriendListChoser adapter;
    MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_friend);
        friendList = (ListView) findViewById(R.id.profile_friend_list);
        myDatabaseHelper = new MyDatabaseHelper(this);
        adapter = new AdapterFriendListChoser(this, myDatabaseHelper.getFriendNameList());
        Log.d(LOG_TAG, "friend names list: " + myDatabaseHelper.getFriendNameList());
        friendList.setAdapter(adapter);

        //change actionbar layout
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = LayoutInflater.from(this);

        View mCustomView = inflater.inflate(R.layout.item_action_bar_normal, null);

        actionBar.setCustomView(mCustomView);

        TextView tvTitle = (TextView) mCustomView.findViewById(R.id.tv_activity_name);
        tvTitle.setText("Chose Friend");

    }
}
