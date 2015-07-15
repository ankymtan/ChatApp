package com.ankymtan.couplechat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.androidchat.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by An on 10/6/2015.
 */
public class FragmentSetting extends android.support.v4.app.Fragment implements View.OnClickListener {

    private UserLocal userLocal;
    private AlertDialog.Builder alertAddFriend;
    private ArrayList<User> friendList = new ArrayList<User>();
    private ListView listView;
    private AdapterFriendList adapter;
    private TextView tvLogout, tvEditAccount;
    private Button btAddFriend;
    private Socket mSocket;
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
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userLocal = new UserLocal(getActivity());
        listView = (ListView) view.findViewById(R.id.profile_friend_list);
        adapter = new AdapterFriendList(friendList, getActivity());
        listView.setAdapter(adapter);

        initFriendList();

        tvLogout = (TextView) view.findViewById(R.id.logout);
        tvEditAccount = (TextView) view.findViewById(R.id.edit_account);
        btAddFriend = (Button) view.findViewById(R.id.bt_add);
        final EditText etAddFriend = (EditText) view.findViewById(R.id.add_friend);

        btAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(new User(etAddFriend.getText().toString()));
            }
        });


        tvLogout.setOnClickListener(this);
        tvEditAccount.setOnClickListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userLocal.setCurrentFriend(friendList.get(position).getName());
                userLocal.resetNewMessageCounterFrom(friendList.get(position).getName());
                ((ActivityMain) getActivity()).mViewPager.setCurrentItem(0);
            }
        });

        TextView profileUsername = (TextView) view.findViewById(R.id.profile_username);
        profileUsername.setText(userLocal.getLoggedInUser().getName());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logout:
                Log.d("by me", "logging out");
                logout();
                break;
            case R.id.edit_account:
                Intent profileEditActivity = new Intent(getActivity(), ActivityEditProfile.class);
                startActivity(profileEditActivity);
                Log.d("by me", " pressing edit");
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setting, menu);
        updateFriendList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setting:

                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View alertView = inflater.inflate(R.layout.alert_setting,null);
                //create a alert for adding friend
                alertAddFriend = new AlertDialog.Builder(getActivity());
                alertAddFriend.setTitle("Setting");
                alertAddFriend.setView(alertView);


                final Switch enableBackground = (Switch) alertView.findViewById(R.id.enable_background);
                final Switch enableAnimation = (Switch) alertView.findViewById(R.id.enable_animation);

                enableBackground.setChecked(userLocal.getEnableBackground());
                enableAnimation.setChecked(userLocal.getEnableAnimation());

                alertAddFriend.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        userLocal.setEnableBackground(enableBackground.isChecked());
                        userLocal.setEnableAnimation(enableAnimation.isChecked());
                    }
                });

                alertAddFriend.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
                alertAddFriend.show();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void logout(){
        userLocal.clearUserData();
        //restart app
        Intent i = getActivity().getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getActivity().finish();
        startActivity(i);
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
}
