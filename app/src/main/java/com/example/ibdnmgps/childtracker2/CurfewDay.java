package com.example.ibdnmgps.childtracker2;

/**
 * Created by ibdnmgps on 4/25/2017.
 */

public class CurfewDay {
    protected int _id;
    protected String day;
    protected int curfew_id;

//initializers
    public CurfewDay(){

    }
    public CurfewDay(String day, int curfew_id) {
        this.day = day;
        this.curfew_id = curfew_id;
    }

//setters
    public void setId(int _id){
        this._id = _id;
    }
    public void setDay(String day){
        this.day = day;

    }
    public void setCurfewId (int curfew_id) {
        this.curfew_id = curfew_id;
    }

 // getters
    public int getId(){
        return this._id;
    }
    public String getDay(){
        return this.day;

    }
    public int getCurfewId() {
        return this.curfew_id;
    }

}
