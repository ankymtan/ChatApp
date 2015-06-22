package com.ankymtan.couplechat;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.github.nkzawa.socketio.androidchat.R;

/**
 * A login screen that offers login via username.
 */

public class ActivityWelcome extends FragmentActivity {
    TabAdapterWelcome tabAdapterWelcome;
    ViewPager viewPager;

    public ActivityWelcome() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().setBackgroundDrawableResource(R.drawable.login);

        tabAdapterWelcome = new TabAdapterWelcome(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager_welcome);
        viewPager.setAdapter(tabAdapterWelcome);
    }
}



