package com.ankymtan.couplechat;

/**
 * Created by ankym on 23/6/2015.
 */
public class User {
    private String name, password, email;
    public User(String name, String password, String email){
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public User(String name, String password){
        this.name = name;
        this.password = password;
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

}
