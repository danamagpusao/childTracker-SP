package com.example.ibdnmgps.childtracker2;

import android.location.Location;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.attr.id;


public class ChildLocation implements Comparable<ChildLocation> {
    private Location location;
    private String time_created;
    private String id;



    public ChildLocation(){

    }

    public ChildLocation(Location location){
        this.location = location;
    }

    public ChildLocation(Location location, String time_created){
        this.location = location;
        this.time_created = time_created;
    }

    public void setLocation(Location location){
        this.location = location;
    }

    public void setTimeCreated(String time_created){
        this.time_created = time_created;
    }

    public void setId(String id){
        this.id = id;
    }

    public Location getLocation(){
        return this.location;
    }

    public String getTimeCreated(){
        return this.time_created;
    }

    public String getId(){
        return this.id;
    }

    public Date getDateTime(){
        Date date1= null;
        try {
            date1 = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a").parse(this.getTimeCreated());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }


    @Override
    public int compareTo(@NonNull ChildLocation childLocation) {
        return getDateTime().compareTo(childLocation.getDateTime());
    }
}
