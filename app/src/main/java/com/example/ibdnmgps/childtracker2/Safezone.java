package com.example.ibdnmgps.childtracker2;

import android.location.Location;

/**
 * Created by ibdnmgps on 4/22/2017.
 */

public class Safezone {
    protected String id;
    protected Location center;
    protected double radius;
    protected int isHome; // 0 false 1 true

    public Safezone() {
    }

    public Safezone(String id){
        this.id = id;
    }

    public Safezone(String id, Location center, double radius) {
        this.id = id;
        this.center = center;
        this.radius = radius;
    }

    //setters
    public void setId(String id){
        this.id = id;
    }

    public void setCenter (Location location){
        this.center = location;
    }

    public void setRadius (double radius) {
        this.radius = radius;
    }

    public void setIsHome(int isHome) {
        this.isHome = isHome;
    }

    //getters
    public String getId() {
       return this.id;
    }

    public Location getCenter(){
        return this.center;
    }

    public double getRadius() {
        return this.radius;
    }

    public int getIsHome() {
        return this.isHome;
    }


}
