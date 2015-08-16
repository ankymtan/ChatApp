package com.ankymtan.couplechat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ankymtan.couplechat.adapter.AdapterSetting;
import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by ankym on 7/8/2015.
 */
public class FragmentSetting extends Fragment {


    RecyclerView settingList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {;
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingList = (RecyclerView) view.findViewById(R.id.lv_setting_list);
        settingList.setLayoutManager(new LinearLayoutManager(getActivity()));
        settingList.setItemAnimator(new DefaultItemAnimator());

        AdapterSetting adapter = new AdapterSetting(getActivity());
        settingList.setAdapter(adapter);
    }


}
