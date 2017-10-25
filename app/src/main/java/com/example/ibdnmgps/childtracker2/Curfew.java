package com.example.ibdnmgps.childtracker2;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ibdnmgps on 4/22/2017.
 */

public class Curfew {
    String id;
    String start;
    String end;
    ArrayList<String> days;

    public Curfew() {
        this.start = "";
        this.end = "";
    }

    public Curfew( String start, String end){
        this.start = start;
        this.end = end;
    }

    public void setId(String id){
        this.id = id;
    }
    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }


    public void setDays(ArrayList<String> days){
        this.days = days;
    }

    public String getId() {
        return this.id;
    }
    public String getStart(){
        return this.start;
    }

    public String getEnd(){
        return this.end;
    }


    public ArrayList<String> getDays() { return this.days;}

}
