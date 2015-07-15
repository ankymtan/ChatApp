package com.ankymtan.couplechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
    private SharedPreferences friendList;
    private SharedPreferences messageList;

    public UserLocal(Context context) {
        userLocalDetails = context.getSharedPreferences(SP_NAME, 0);
        friendList = context.getSharedPreferences(SP_NAME_FRIEND_LIST, 0);
        messageList = context.getSharedPreferences(SP_MESSAGE_LIST, 0);
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

        SharedPreferences.Editor editorFriendList = friendList.edit();
        editorFriendList.clear();
        editorFriendList.commit();

        SharedPreferences.Editor editorMessageList = messageList.edit();
        editorMessageList.clear();
        editorMessageList.commit();

    }

    //deal with add/ remove/ update friend.
    public void addFriend(User user) {
        HashSet<String> friends;
        friends = (HashSet<String>) friendList.getStringSet("friendNames", new HashSet());

        String encodedFriendInfo = user.getName() + "#0";
        friends.add(encodedFriendInfo);

        SharedPreferences.Editor editor = friendList.edit();

        editor.clear();
        editor.putStringSet("friendNames", friends);
        editor.commit();
    }

    public void addNewMessageFrom(String username) {
        HashSet<String> friends;
        friends = (HashSet<String>) friendList.getStringSet("friendNames", new HashSet());

        for (String friend : friends) {
            if (getFirstItem(friend).equals(username)) {
                int counter = Integer.valueOf(getSecondItem(friend));
                friends.remove(friend);
                friends.add(username + "#" + (counter + 1));
                break;
            }
        }

        SharedPreferences.Editor editor = friendList.edit();
        editor.clear();
        editor.putStringSet("friendNames", friends);
        editor.commit();

    }

    public void resetNewMessageCounterFrom(String username){
        HashSet<String> friends;
        friends = (HashSet<String>) friendList.getStringSet("friendNames", new HashSet());

        for (String friend : friends) {
            if (getFirstItem(friend).equals(username)) {
                friends.remove(friend);
                friends.add(username + "#0");
                break;
            }
        }

        SharedPreferences.Editor editor = friendList.edit();
        editor.clear();
        editor.putStringSet("friendNames", friends);
        editor.commit();
    }

    public ArrayList<User> getFriendList() {
        HashSet<String> friendInfos = (HashSet<String>) friendList.getStringSet("friendNames", new HashSet());
        ArrayList<User> result = new ArrayList<>();

        for (String friendInfo : friendInfos) {
            result.add(new User(getFirstItem(friendInfo), Integer.valueOf(getLastItem(friendInfo))));
        }

        Collections.sort(result, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        return result;
    }

    public ArrayList<String> getFriendNameList() {
        HashSet<String> friendInfos = (HashSet<String>) friendList.getStringSet("friendNames", new HashSet());
        ArrayList<String> result = new ArrayList<>();

        for (String friendInfo : friendInfos) {
            result.add(getFirstItem(friendInfo));
        }

        return result;
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

        //init
        HashSet<String> messages = new HashSet<>();
        String encodedMessage;

        messages = (HashSet<String>) messageList.getStringSet("message", new HashSet());

        //encode input message
        encodedMessage = message.getGMT() + "#" + message.getUsernameFrom() + "#" + message.getUsernameTo() + "#" + message.getMessage();
        messages.add(encodedMessage);

        //put back
        SharedPreferences.Editor editor = messageList.edit();
        editor.clear();
        editor.putStringSet("message", messages);
        editor.commit();
    }

    public ArrayList<Message> getMessageListWith(String currentFriendName) {

        ArrayList<Message> result = new ArrayList<>();
        ArrayList<String> encodedMessages = new ArrayList<>();
        HashSet<String> messages = (HashSet<String>) messageList.getStringSet("message", new HashSet());

        for (String encodedMessage : messages) {
            encodedMessages.add(encodedMessage);
        }

        Collections.sort(encodedMessages, String.CASE_INSENSITIVE_ORDER);

        for (String encodedMessage : encodedMessages) {
            if (getSecondItem(encodedMessage).equals(currentFriendName)) {
                Message message = new Message.Builder(Message.TYPE_MESSAGE).usernameFrom(currentFriendName)
                        .usernameTo(getLoggedInUser().getName()).GMT(getFirstItem(encodedMessage)).message(getLastItem(encodedMessage))
                        .build();
                result.add(message);
                Log.d(BY_ME, "check hashset order  " + encodedMessage);
            } else if (getThirdItem(encodedMessage).equals(currentFriendName)) {
                Message message = new Message.Builder(Message.TYPE_MESSAGE).usernameFrom(getLoggedInUser().getName())
                        .usernameTo(currentFriendName).GMT(getFirstItem(encodedMessage)).message(getLastItem(encodedMessage))
                        .build();
                result.add(message);
                Log.d(BY_ME, "check hashset order  " + encodedMessage);
            }
        }
        return result;
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