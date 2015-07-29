package com.ankymtan.couplechat.fragment;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ankymtan.couplechat.activity.ActivityWelcome;
import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by An on 20/6/2015.
 */
public class FragmentWelcome extends Fragment implements View.OnClickListener{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button loginFacebook = (Button) view.findViewById(R.id.bt_facebook);
        Button loginGoogle = (Button) view.findViewById(R.id.bt_google);
        Button signInButton = (Button) view.findViewById(R.id.bt_register);
        TextView tvLogin = (TextView) view.findViewById(R.id.tv_login);

        loginFacebook.getBackground().setColorFilter(Color.rgb(59, 89, 152), PorterDuff.Mode.SRC);
        loginGoogle.getBackground().setColorFilter(Color.rgb(221, 75, 57), PorterDuff.Mode.SRC);
        signInButton.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);

        loginFacebook.setTextColor(Color.WHITE);
        loginGoogle.setTextColor(Color.WHITE);
        signInButton.setTextColor(Color.BLACK);

        loginFacebook.setOnClickListener(this);
        loginGoogle.setOnClickListener(this);
        signInButton.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_register:
                ((ActivityWelcome) getActivity()).viewPager.setCurrentItem(1);
                break;
            case R.id.tv_login:
                ((ActivityWelcome) getActivity()).viewPager.setCurrentItem(2);
                break;
        }
    }
}
