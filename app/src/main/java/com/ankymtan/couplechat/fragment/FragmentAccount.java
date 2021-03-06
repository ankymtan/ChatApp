package com.ankymtan.couplechat.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.ankymtan.couplechat.activity.ActivityChat;
import com.ankymtan.couplechat.adapter.AdapterFriendList;
import com.ankymtan.couplechat.entity.User;
import com.ankymtan.couplechat.framework.UserLocal;
import com.ankymtan.couplechat.framework.ProfileManager;
import com.ankymtan.couplechat.framework.onAddFriendListener;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.androidchat.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by An on 10/6/2015.
 */
public class FragmentAccount extends android.support.v4.app.Fragment implements View.OnClickListener, onAddFriendListener {

    private UserLocal userLocal;
    private AlertDialog.Builder alertAddFriend;
    private ArrayList<User> friendList = new ArrayList<User>();
    private ListView listView;
    private AdapterFriendList adapter;
    private Socket mSocket;
    private ProfileManager profileManager;
    private ActionBar actionBar;
    private MenuItem add, setting;
    //private TextView tvNoFriend;

    {
        try {
            mSocket = IO.socket(FragmentLogin.ADDRESS);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mSocket.on("check exist", onCheckExist);
        mSocket.connect();
        profileManager = new ProfileManager(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userLocal = new UserLocal(getActivity());

        //tvNoFriend = (TextView) view.findViewById(R.id.tv_no_friend);
        //tvNoFriend.setVisibility(View.INVISIBLE);
        listView = (ListView) view.findViewById(R.id.profile_friend_list);
        adapter = new AdapterFriendList(friendList, getActivity());
        adapter.setOnAddFriendListener(this);
        listView.setAdapter(adapter);

        initFriendList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    return;
                }
                userLocal.setCurrentFriend(friendList.get(position - 2).getName());
                userLocal.resetNewMessageCounterFrom(friendList.get(position - 2).getName());

                Intent activityChat = new Intent(getActivity(), ActivityChat.class);
                getActivity().startActivity(activityChat);
            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setting, menu);

        add = menu.findItem(R.id.add);
        setting = menu.findItem(R.id.setting);

        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        //getCount = 1 then only have profile panel
        //if(adapter.getCount() > 1){
         //   tvNoFriend.setVisibility(View.INVISIBLE);
       // }else{
         //   tvNoFriend.setVisibility(View.VISIBLE);
       // }
        updateFriendList();
    }

    private void addFriend(User user){
        //first check if user is alr in the list or not
        for(User mUser: friendList){
            if(mUser.getName().equals(user.getName())) return;
        }
        //then check if friendname is on database
        JSONObject json = new JSONObject();
        try {
            json.put("username", user.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //first check if user is alr in the list or not
        for(User mUser: friendList){
            if(mUser.getName().equals(user.getName())) return;
        }
        mSocket.emit("check exist", json.toString());
    }

    private void initFriendList(){
        ArrayList<User> storagedFriendList =  userLocal.getFriendList();
        if (storagedFriendList == null) return;
        for (User friend: storagedFriendList){
            friendList.add(friend);
            adapter.notifyDataSetChanged();
        }
    }



    private Emitter.Listener onCheckExist = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject json = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Boolean isExist = json.getBoolean("checkExist");
                        if (isExist) {
                            User user = new User(json.getString("username"));
                            friendList.add(user);
                            adapter.notifyDataSetChanged();
                            userLocal.addFriend(user);
                            Toast.makeText(getActivity(), "Add friend successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), "Please check username again", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private void updateFriendList(){
        ArrayList<User> savedFriendList = userLocal.getFriendList();
        friendList.clear();
        for(User savedFriend: savedFriendList){
            friendList.add(savedFriend);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off("check exist", onCheckExist);
    }

    @Override
    public void onAddFriend(User user) {
        addFriend(user);
    }
}
