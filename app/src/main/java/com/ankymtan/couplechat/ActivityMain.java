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

import java.util.Timer;
import java.util.TimerTask;

public class ActivityMain extends ActionBarActivity implements onFragmentAttachedListenner{

    private static final String BY_ME = "by me";
    private static final int REQUEST_LOGIN= 0;
    private TabAdapter mTabAdapter;
    private ActionBar actionBar;
    private TimerTask timerTask;
    private Timer timer = new Timer();

    ViewPager mViewPager;
    private String mUsername;


    private int numUsers, selectedFragment;
    private FragmentMain mainFragment;
    private int x,y;
    private Backgrounder background;

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
        background = (Backgrounder) findViewById(R.id.background);
        mTabAdapter = new TabAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setOffscreenPageLimit(2);
        //action Bar
        actionBar = getActionBar();
        //
        //work for background
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
}
