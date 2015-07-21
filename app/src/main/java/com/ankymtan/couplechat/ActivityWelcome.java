package com.ankymtan.couplechat;


import android.graphics.Interpolator;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

import com.ankymtan.couplechat.framework.CustomScroller;
import com.github.nkzawa.socketio.androidchat.R;

import java.lang.reflect.Field;

/**
 * A login screen that offers login via usernameEt.
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
        viewPager.setCurrentItem(0);

        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");

            mScroller.setAccessible(true);
            CustomScroller scroller = new CustomScroller(this, new OvershootInterpolator(1));
            scroller.setDuration(700);
            mScroller.set(viewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



