package com.ankymtan.couplechat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;

import com.ankymtan.couplechat.adapter.TabAdapter;
import com.ankymtan.couplechat.framework.UserLocal;
import com.ankymtan.couplechat.framework.CustomScroller;
import com.ankymtan.couplechat.framework.onFragmentAttachedListenner;
import com.github.nkzawa.socketio.androidchat.R;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends ActionBarActivity implements onFragmentAttachedListenner {

    private static final String BY_ME = "by me";
    private static final int REQUEST_LOGIN= 0;
    private TabAdapter mTabAdapter;
    private TimerTask timerTask;
    private Timer timer = new Timer();

    public ViewPager mViewPager;
    private String mUsername;


    private int selectedFragment;
    private ActivityChat mainFragment;
    private int x,y;
    //private Backgrounder background;
    private UserLocal userLocal;

    public ActivityMain() {
        super();
    }

    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userLocal = new UserLocal(this);
        //get screen size
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        x = size.x;
        y = size.y;
        //
        //background = (Backgrounder) findViewById(R.id.background);
        mTabAdapter = new TabAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOffscreenPageLimit(2);

        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");

            mScroller.setAccessible(true);
            CustomScroller scroller = new CustomScroller(this, new OvershootInterpolator(1));
            scroller.setDuration(700);
            mScroller.set(mViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        //work for background
        /*
        final Runnable backgroundUpdate = new Runnable() {
            @Override
            public void run() {
                mainFragment = (FragmentMain) mTabAdapter.getRegisteredFragment(0);
                if(mainFragment == null) return;
                if (mainFragment.messageCounter > 0) {
                    mainFragment.numberOfHeart += mainFragment.messageCounter;
                } else {
                    mainFragment.numberOfHeart -= 2;
                    if (mainFragment.numberOfHeart < 0) {
                        mainFragment.numberOfHeart = 0;
                    }
                    ;
                }
                ;
                //TODO set up when to re-draw: if...then below
                if (mainFragment.mInputMessageView.getBottom() > y / 4 * 3) {
                    background.update(mainFragment.numberOfHeart);
                }else{
                    Log.d(BY_ME, "no update cuz typing");
                }
                mainFragment.messageCounter = 0;
            }
        };

        timerTask = new TimerTask() {
            public void run() {
                runOnUiThread(backgroundUpdate);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0 , 5000);
        */
        if(userLocal.isLoggedIn()){
            mUsername = userLocal.getLoggedInUser().getName();
            Log.d(BY_ME, "logged user" + mUsername);
        }else {
            //start login
            Intent intent = new Intent(this, ActivityWelcome.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            finish();
            return;
        }
        mUsername = data.getStringExtra("username");
    }
    /*
    @Override
    protected void onResume() {
        super.onResume();
        background.onResumeMySurfaceView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        background.onPauseMySurfaceView();
    }

    public Backgrounder getBackground(){
        return background;
    }
    */
}
