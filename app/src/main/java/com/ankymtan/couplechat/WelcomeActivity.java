package com.ankymtan.couplechat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import com.ankymtan.couplechat.MainActivity;
import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by An on 26/5/2015.
 */
public class WelcomeActivity extends Activity{

    Button loginFacebook, loginGoogle, signUp, login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);

        loginFacebook = (Button) findViewById(R.id.login_facebook);
        loginGoogle = (Button) findViewById(R.id.login_google);
        signUp = (Button) findViewById(R.id.sign_up);
        login = (Button) findViewById(R.id.log_in);

        loginFacebook.getBackground().setColorFilter(Color.rgb(59,89,152), PorterDuff.Mode.SRC);
        loginGoogle.getBackground().setColorFilter(Color.rgb(221,75,57), PorterDuff.Mode.SRC);
        signUp.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
        login.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);

        loginFacebook.setTextColor(Color.WHITE);
        loginGoogle.setTextColor(Color.WHITE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(getBaseContext(), MainActivity.class);
                startActivity(mainActivity);
            }
        });

    }
}
