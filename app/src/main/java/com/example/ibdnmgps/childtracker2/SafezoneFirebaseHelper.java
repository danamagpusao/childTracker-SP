package com.example.ibdnmgps.childtracker2;

import android.location.Location;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ibdnmgps on 10/30/2017.
 */

public class SafezoneFirebaseHelper extends FirebaseHelper {

    private ArrayList safezone_list;
    private String childId;

    public SafezoneFirebaseHelper(DatabaseReference db) {
        this.db = db;
        db.keepSynced(true);

    }

    public SafezoneFirebaseHelper(DatabaseReference db, String childId) {
        this.db = db;
        db.keepSynced(true);
        this.childId = childId;
    }

    //WRITE
    public String save(Safezone object)
    {
        DatabaseReference id = null;
        if(object!=null)
        {
            try{
                id = db.child("Child/" + childId + "/Safezone").push();
                id.child("lat").setValue(object.getCenter().getLatitude());
                id.child("lon").setValue(object.getCenter().getLongitude());
                id.child("lon").setValue(object.getCenter().getLongitude());
                id.child("radius").setValue(object.getRadius());


            }catch(DatabaseException e){
                e.printStackTrace();

            }
        }

        return id.getKey();

    }

    public Boolean remove(String child_id, String safezone_id)
    {
        if(child_id!=null && safezone_id != null)
        {
                try{
                    db.child("Child/"+child_id+"/Safezone/"+safezone_id).removeValue();
                    return true;
                }catch(DatabaseException e) {
                    e.printStackTrace();
                    return false;
                }
        }
        return false;
    }


    public ArrayList<Safezone> retrieveSafeZoneList() {

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if( dataSnapshot.child(childId).child("Safezone").getChildrenCount() >0 )
                    safezone_list.clear();
                for (DataSnapshot wow : dataSnapshot.child(childId).child("Safezone").getChildren()) {
                    Safezone safeZone = new Safezone();
                    safeZone.setId(wow.getKey());
                    Location temp = new Location(wow.getKey());
                    if(wow.child("lat").getValue(Double.class) != null && wow.child("lon").getValue(Double.class)!=null) {
                        temp.setLatitude(wow.child("lat").getValue(Double.class));
                        temp.setLongitude(wow.child("lon").getValue(Double.class));
                    }
                    else return;
                    safeZone.setCenter(temp);
                    safezone_list.add(0,safeZone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return safezone_list;
    }
    @Override
    ArrayList retrieve() {
        return safezone_list;
    }
}
