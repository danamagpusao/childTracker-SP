package com.example.ibdnmgps.childtracker2;

import android.location.Location;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ibdnmgps on 7/7/2017.
 */

class LocationFirebaseHelper extends FirebaseHelper{

    private String childId;
    private ArrayList<ChildLocation> location_list = new ArrayList<>();


    public LocationFirebaseHelper(DatabaseReference db, String childId) {
        this.db = db;
        db.keepSynced(true);
        this.childId = childId;


        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveLocationList(dataSnapshot);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                retrieveLocationList(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieveLocationList(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                retrieveLocationList(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    //WRITE
    public String save(ChildLocation object)
    {
        DatabaseReference id = null;
        if(object!=null)
        {
            try{
                id = db.child("Child/" + childId + "/ChildLocation").push();
                id.child("lat").setValue(object.getLocation().getLatitude());
                id.child("lon").setValue(object.getLocation().getLongitude());
                id.child("time_created").setValue(object.getTimeCreated());


            }catch(DatabaseException e){
                e.printStackTrace();

            }
        }

        return id.getKey();

    }

    public ChildLocation retrieveLatest() {
        return location_list.get(0);
    }


    private void retrieveLocationList(DataSnapshot dataSnapshot) {
        if( dataSnapshot.child(childId).child("ChildLocation").getChildrenCount() >0 )
            location_list.clear();
        for (DataSnapshot wow : dataSnapshot.child(childId).child("ChildLocation").getChildren()) {
            ChildLocation loc = new ChildLocation();
            loc.setId(wow.getKey());
            Location temp = new Location(wow.getKey());
            if(wow.child("lat").getValue(Double.class) != null && wow.child("lon").getValue(Double.class)!=null) {
                temp.setLatitude(wow.child("lat").getValue(Double.class));
                temp.setLongitude(wow.child("lon").getValue(Double.class));
            }
            else return;
            loc.setLocation(temp);
            loc.setTimeCreated(wow.child("time_created").getValue(String.class));
            location_list.add(0,loc);
        }
    }

    @Override
    ArrayList retrieve() {
        return location_list;
    }
}
