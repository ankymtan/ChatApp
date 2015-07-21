package com.ankymtan.couplechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ankymtan.couplechat.framework.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Created by ankym on 23/6/2015.
 */
public class UserLocal {

    private static final String BY_ME = "by me";
    private static final String SP_NAME = "userDetails";
    private static final String SP_NAME_FRIEND_LIST = "friendList";
    private static final String SP_MESSAGE_LIST = "messageList";
    private SharedPreferences userLocalDetails;
    private MyDatabaseHelper myDatabaseHelper;

    public UserLocal(Context context) {
        userLocalDetails = context.getSharedPreferences(SP_NAME, 0);
        myDatabaseHelper = new MyDatabaseHelper(context);
    }

    public void storeUser(User user) {
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putString("usernameEt", user.getName());
        editor.putString("password", user.getPassword());
        editor.putString("email", user.getEmail());
        editor.commit();
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return userLocalDetails.getBoolean("loggedIn", false);
    }

    public User getLoggedInUser() {
        if (userLocalDetails.getBoolean("loggedIn", false) == false) {
            return null;
        }

        String username = userLocalDetails.getString("usernameEt", "");
        String password = userLocalDetails.getString("password", "");
        String email = userLocalDetails.getString("email", "");

        return new User(username, password, email);
    }

    public void setCurrentFriend(String username) {
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putString("currentFriend", username);
        editor.commit();
    }

    public String getCurrentFriend() {
        return userLocalDetails.getString("currentFriend", null);
    }

    public void clearUserData() {

        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.clear();
        editor.commit();

        myDatabaseHelper.clear();
    }

    //deal with add/ remove/ update friend.
    public void addFriend(User user) {
        myDatabaseHelper.addFriend(user);
    }

    public void addNewMessageFrom(String username) {
        myDatabaseHelper.addNewMessageFrom(username);
    }

    public void resetNewMessageCounterFrom(String username){
        myDatabaseHelper.resetNewMessageCounterFrom(username);
    }

    public ArrayList<User> getFriendList() {
        return myDatabaseHelper.getFriendList();
    }

    public ArrayList<String> getFriendNameList() {
        return myDatabaseHelper.getFriendNameList();
    }

    //
    public void setEnableBackground(boolean b) {
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putBoolean("enableBackground", b);
        editor.commit();
    }

    public void setEnableAnimation(boolean b) {
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putBoolean("enableAnimation", b);
        editor.commit();
    }

    public boolean getEnableBackground() {
        return userLocalDetails.getBoolean("enableBackground", true);
    }

    public boolean getEnableAnimation() {
        return userLocalDetails.getBoolean("enableAnimation", true);
    }

    public void addMessage(Message message) {
        myDatabaseHelper.addMessage(message);
    }

    public ArrayList<Message> getMessageListWith(String currentFriendName) {
        return  myDatabaseHelper.getMessageListWith(currentFriendName);
    }

    ;

    private String getFirstItem(String encodedMessage) {
        return encodedMessage.substring(0, encodedMessage.indexOf("#"));
    }

    private String getSecondItem(String encodedMessage) {
        int start = encodedMessage.indexOf("#");
        int end = encodedMessage.indexOf("#", start + 1);
        if (end < 0) {
            return encodedMessage.substring(start + 1);
        }
        return encodedMessage.substring(start + 1, end);
    }

    private String getThirdItem(String encodedMessage) {
        int start = encodedMessage.indexOf("#");//first #
        start = encodedMessage.indexOf("#", start + 1);//sencond #
        int end = encodedMessage.lastIndexOf("#");//third one
        return encodedMessage.substring(start + 1, end);
    }

    private String getLastItem(String encodedMessage) {
        int start = encodedMessage.lastIndexOf("#");//third one, also the last
        return encodedMessage.substring(start + 1);
    }

}