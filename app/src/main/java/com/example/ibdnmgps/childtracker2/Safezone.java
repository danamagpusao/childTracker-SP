package com.example.ibdnmgps.childtracker2;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ibdnmgps on 4/22/2017.
 */

public class Safezone implements Parcelable {
    protected String id;
    protected Location center;
    protected double radius;
    protected int isHome; // 0 false 1 true
    protected String name;

    public Safezone() {
    }

    public Safezone(String id){
        this.id = id;
    }

    public Safezone(Location center, double radius) {
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

    public void setName(String name){ this.name = name;}


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

    public String getName() {return this.name;}


    protected Safezone(Parcel in) {
        id = in.readString();
        center = (Location) in.readValue(Location.class.getClassLoader());
        radius = in.readDouble();
        isHome = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeValue(center);
        dest.writeDouble(radius);
        dest.writeInt(isHome);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Safezone> CREATOR = new Parcelable.Creator<Safezone>() {
        @Override
        public Safezone createFromParcel(Parcel in) {
            return new Safezone(in);
        }

        @Override
        public Safezone[] newArray(int size) {
            return new Safezone[size];
        }
    };
}