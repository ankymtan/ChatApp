package com.ankymtan.couplechat.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ankymtan.couplechat.activity.ActivityPluginSetting;
import com.ankymtan.couplechat.adapter.AdapterPlugin;
import com.ankymtan.couplechat.framework.PluginManager;
import com.github.nkzawa.socketio.androidchat.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ankym on 28/7/2015.
 */
public class FragmentPlugin extends Fragment {

    private static final String LOG_TAG = "by me fragmentPlugin ";
    PluginManager pluginManager;
    Activity activity;
    ArrayList<String> serviceNames = new ArrayList<>();
    ListView listview;
    AdapterPlugin adapterPlugin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plugin, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listview = (ListView) view.findViewById(R.id.lv_plugin_list);
        adapterPlugin = new AdapterPlugin(getAllPluginName(), getActivity());
        listview.setAdapter(adapterPlugin);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ActivityPluginSetting.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        pluginManager = new PluginManager(activity);
        pluginManager.onStart();
        Log.d(LOG_TAG, "onAttach");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onDestroy() {
        super.onDestroy();
        pluginManager.onStop();
    }

    private ArrayList<String> getAllPluginName(){
        ArrayList<String> result = new ArrayList<>();
        ArrayList<HashMap<String, String>> services = pluginManager.getAllServices();

        for (HashMap<String, String> serviceInfo: services ){
            String serviceName = serviceInfo.get(PluginManager.KEY_SERVICENAME);
            serviceName = serviceName.substring(serviceName.lastIndexOf(".")+1);
            result.add(serviceName);
        }
        Log.d(LOG_TAG, "service name: "+ result);
        return result;
    }
}
