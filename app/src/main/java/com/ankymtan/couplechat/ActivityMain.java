package com.ankymtan.couplechat;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.WindowManager;

import com.github.nkzawa.socketio.androidchat.R;

public class ActivityMain extends ActionBarActivity implements onFragmentAttachedListenner{

    private static final String BY_ME = "by me";
    private static final int REQUEST_LOGIN= 0;
    private TabAdapter mTabAdapter;
    private ActionBar actionBar;

    ViewPager mViewPager;
    private String mUsername;


    private int numUsers, selectedFragment;
    private FragmentMain mainFrangment;
    private int x,y;

    public ActivityMain() {
        super();
    }

    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    public int getNumberUser() {
        return numUsers;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get screen size
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        x = size.x;
        y = size.y;
        //
        mTabAdapter = new TabAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset == 0 & selectedFragment == 0) {
                    mainFrangment = (FragmentMain) mTabAdapter.getRegisteredFragment(0);
                    mainFrangment.getBackgrounder().startTime();
                    mainFrangment.getBackgrounder().onResumeMySurfaceView();
                } else {
                    mainFrangment.getBackgrounder().onPauseMySurfaceView();
                }
                Log.d(BY_ME, mainFrangment.getBackgrounder().isRunning() + "");
            }

            @Override
            public void onPageSelected(int position) {
                selectedFragment = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageMargin(x / 90);
        mViewPager.setPageMarginDrawable(R.color.black);
        //action Bar
        actionBar = getActionBar();
        //start login
        Intent intent = new Intent(this, ActivityWelcome.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK != resultCode) {
            finish();
            return;
        }

        mUsername = data.getStringExtra("usernameEt");
        numUsers = data.getIntExtra("numUsers", 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
