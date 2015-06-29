package com.ankymtan.couplechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ankym on 23/6/2015.
 */
public class UserLocal {

    private static String SP_NAME = "userDetails";
    private SharedPreferences userLocalDetails;

    public UserLocal(Context context) {
        userLocalDetails = context.getSharedPreferences(SP_NAME, 0);
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
        return userLocalDetails.getString("currentFriend", "");
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.clear();
        editor.commit();
    }

    public void addFriend(User user) {
        HashSet<String> friendNames;
        friendNames = getFriends();

        SharedPreferences.Editor editor = userLocalDetails.edit();
        friendNames.add(user.getName());
        Log.d("by me", user.getName());
        editor.putStringSet("friendNames", friendNames);
        editor.commit();
        getFriends();
    }

    public HashSet<String> getFriends() {
        HashSet<String> result = (HashSet<String>) userLocalDetails.getStringSet("friendNames", new HashSet());
        for (String string : result) {
            Log.d("by me ", string);
        }
        return new HashSet<>();
    }

    public void setEnableBackground(boolean b){
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putBoolean("enableBackground", b);
        editor.commit();
    }

    public void setEnableAnimation(boolean b){
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putBoolean("enableAnimation", b);
        editor.commit();
    }

    public boolean getEnableBackground(){
        return userLocalDetails.getBoolean("enableBackground", true);
    }

    public boolean getEnableAnimation(){
        return userLocalDetails.getBoolean("enableAnimation", true);
    }

}