package com.ankymtan.couplechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

public class ActivityPluginSetting extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_setting);
        TextView choseCustomer = (TextView) findViewById(R.id.tv_chose_cutomer);
        TextView choseManager = (TextView) findViewById(R.id.tv_chose_manager);

        choseCustomer.setOnClickListener(this);
        choseManager.setOnClickListener(this);

        //change actionbar layout
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = LayoutInflater.from(this);

        View mCustomView = inflater.inflate(R.layout.item_action_bar_normal, null);

        actionBar.setCustomView(mCustomView);

        TextView tvTitle = (TextView) mCustomView.findViewById(R.id.tv_activity_name);
        tvTitle.setText("Plugin Setting");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_chose_cutomer:
                Intent intentChoseCustomer = new Intent(this, ActivityChooseFriend.class);
                startActivity(intentChoseCustomer);
                break;
            case R.id.tv_chose_manager:
                Intent intentChoseManager = new Intent(this, ActivityChooseManager.class);
                startActivity(intentChoseManager);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
