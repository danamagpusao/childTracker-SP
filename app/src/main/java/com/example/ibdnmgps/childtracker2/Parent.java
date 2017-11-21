package com.example.ibdnmgps.childtracker2;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.author;

/**
 * Created by ibdnmgps on 4/22/2017.
 */

public class Parent {
    protected String id;
    protected String name;
    protected String phoneNum;
    protected String password;
    protected Boolean receiveSMS;

    public Parent(){

    }

    public Parent(String password) {
        this.password = password;
        this.receiveSMS = false;
    }
    public Parent(String phoneNum, String password) {
        this.phoneNum = phoneNum;
        this.password = password;
        this.receiveSMS = false;
    }

    public void setId(String _id) {
        this.id = _id;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setReceiveSMS(boolean receiveSMS) {
        this.receiveSMS = receiveSMS;
    }
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNum() {return phoneNum;}

    public String getPassword(){
        return password;
    }

    public boolean getReceiveSMS(){
        return receiveSMS;
    }





}
