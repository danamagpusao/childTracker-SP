package com.example.ibdnmgps.childtracker2;

/**
 * Created by ibdnmgps on 4/22/2017.
 */

public class Child {

    protected String id;
    protected String name;
    protected String phoneNum;


    public Child(){
    }

    public Child(String name, int parent_id){
        this.name =name;
    }

    public Child( String name, String phoneNum) {
        this.name = name;
        this.phoneNum = phoneNum;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getPhoneNum() {
        return this.phoneNum;
    }


}
