package com.ankymtan.couplechat.activity;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by ankym on 8/8/2015.
 */
public class ActivityChoosePlugin extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_plugin);

        //change actionbar layout
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = LayoutInflater.from(this);

        View mCustomView = inflater.inflate(R.layout.item_action_bar_normal, null);

        actionBar.setCustomView(mCustomView);

        TextView tvTitle = (TextView) mCustomView.findViewById(R.id.tv_activity_name);
        tvTitle.setText("Chose Plugin");

        ImageView ivBack = (ImageView) mCustomView.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
