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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.github.nkzawa.socketio.androidchat.R;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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

        tvLogout.setOnClickListener(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userLocal.setCurrentFriend(friendList.get(position).getName());
            }
        });

        TextView profileUsername = (TextView) view.findViewById(R.id.profile_username);
        profileUsername.setText(userLocal.getLoggedInUser().getName());

        final EditText etAddFriend = (EditText) view.findViewById(R.id.add_friend);
        etAddFriend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if ((id == R.id.add_friend_action || id == EditorInfo.IME_NULL)) {
                    addFriend(new User(etAddFriend.getText().toString()));
                    return true;
                }
                return false;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logout:
                Log.d("by me", "logging out");
                logout();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_setting, menu);
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
        friendList.add(user);
        adapter.notifyDataSetChanged();
        userLocal.addFriend(user);
    }

    private void initFriendList(){
        HashSet<String> storagedFriendList =  userLocal.getFriends();
        if (storagedFriendList == null) return;
        for (String friendName: storagedFriendList){
            friendList.add(new User(friendName));
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

}
