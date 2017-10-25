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

    String childId;
    ArrayList<ChildLocation> location_list = new ArrayList<>();

    public LocationFirebaseHelper(DatabaseReference db) {
        super(db);
        db.keepSynced(true);
        childId = null;

    }

    public LocationFirebaseHelper(DatabaseReference db, String childId) {
        this.db = db;
        db.keepSynced(true);
        this.childId = childId;

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

    @Override
    public ArrayList retrieve() {
        return location_list;
    }

}
