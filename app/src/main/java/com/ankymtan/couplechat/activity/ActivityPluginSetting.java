package com.ankymtan.couplechat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ankymtan.couplechat.adapter.AdapterPluginSetting;
import com.github.nkzawa.socketio.androidchat.R;

public class ActivityPluginSetting extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_setting);

        ListView settingList = (ListView) findViewById(R.id.lv_setting_plugin_list);
        AdapterPluginSetting adapter = new AdapterPluginSetting(this);
        settingList.setAdapter(adapter);

        settingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:
                        Intent intentChoseCustomer = new Intent(getApplicationContext(), ActivityChooseFriend.class);
                        startActivity(intentChoseCustomer);
                        break;
                    case 2:
                        Intent intentChoseManager = new Intent(getApplicationContext(), ActivityChoosePlugin.class);
                        startActivity(intentChoseManager);
                        break;
                }
            }
        });

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

        ImageView ivBack = (ImageView) mCustomView.findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
