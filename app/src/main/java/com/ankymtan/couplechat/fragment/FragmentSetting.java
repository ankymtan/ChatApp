package com.ankymtan.couplechat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ankymtan.couplechat.framework.UserLocal;
import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by ankym on 7/8/2015.
 */
public class FragmentSetting extends Fragment implements CompoundButton.OnCheckedChangeListener{

    UserLocal userLocal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userLocal = new UserLocal(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alert_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Switch enableBackground = (Switch) view.findViewById(R.id.enable_background);
        enableBackground.setOnCheckedChangeListener(this);
        Switch enableAnimation = (Switch) view.findViewById(R.id.enable_animation);
        enableAnimation.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.enable_background:
                userLocal.setEnableBackground(isChecked);
                break;
            case R.id.enable_animation:
                userLocal.setEnableAnimation(isChecked);
                break;
        }
    }
}
