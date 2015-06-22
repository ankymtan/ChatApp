package com.ankymtan.couplechat;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by An on 20/6/2015.
 */
public class FragmentRegister extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setup Buttons color, theme
        Button loginFacebook = (Button) view.findViewById(R.id.login_facebook);
        Button loginGoogle = (Button) view.findViewById(R.id.login_google);
        Button signInButton = (Button) view.findViewById(R.id.register_button);

        loginFacebook.getBackground().setColorFilter(Color.rgb(59, 89, 152), PorterDuff.Mode.SRC);
        loginGoogle.getBackground().setColorFilter(Color.rgb(221,75,57), PorterDuff.Mode.SRC);
        signInButton.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);

        loginFacebook.setTextColor(Color.WHITE);
        loginGoogle.setTextColor(Color.WHITE);
    }
}
