package com.ankymtan.couplechat.entity;

/**
 * Created by ankym on 23/6/2015.
 */
public class User {
    private String name, password, email;
    private int unreadCounter; // count number of unread message send from this user
    public User(String name, String password, String email){
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public User(String name, String password){
        this.name = name;
        this.password = password;
    }

    public User(String name, int unreadCounter){
        this.name = name;
        this.unreadCounter = unreadCounter;
    }

    public User(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getPassword(){
        return password;
    }

    public String getEmail(){
        return email;
    }

    public int getUnreadCounter(){
        return unreadCounter;
    }

}
