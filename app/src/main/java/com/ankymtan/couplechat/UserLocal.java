package com.ankymtan.couplechat;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ankym on 23/6/2015.
 */
public class UserLocal {

    private static String SP_NAME = "userDetails";
    private SharedPreferences userLocalDetails;

    public UserLocal(Context context){
        userLocalDetails = context.getSharedPreferences(SP_NAME, 0);
    }

    public void storeUser(User user){
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putString("usernameEt", user.getName());
        editor.putString("password", user.getPassword());
        editor.putString("email", user.getEmail());
        editor.commit();
    }

    public void setUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.putBoolean("loggedIn", loggedIn);
        editor.commit();
    }

    public User getLoggedInUser(){
        if (userLocalDetails.getBoolean("loggedIn", false)==false){
            return null;
        }

        String username = userLocalDetails.getString("usernameEt", "");
        String password = userLocalDetails.getString("password", "");
        String email = userLocalDetails.getString("email", "");

        return new User(username, password, email);
    }

    public void clearUserData(){
        SharedPreferences.Editor editor = userLocalDetails.edit();
        editor.clear();
        editor.commit();
    }
}
