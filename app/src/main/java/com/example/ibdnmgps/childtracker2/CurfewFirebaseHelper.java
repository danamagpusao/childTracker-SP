package com.example.ibdnmgps.childtracker2;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import static android.R.attr.id;

/**
 * Created by ibdnmgps on 7/5/2017.
 */

public class CurfewFirebaseHelper extends FirebaseHelper {
    String childId;
    ArrayList<Curfew> curfew_list = new ArrayList<>();
    public CurfewFirebaseHelper(DatabaseReference db) {
        super(db);
        db.keepSynced(true);
        childId = null;

        ChildEventListener cel = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        db.addChildEventListener(cel);

    }
    public CurfewFirebaseHelper(DatabaseReference db, String childId) {
        this.db = db;
        db.keepSynced(true);
        this.childId = childId;


        ChildEventListener cel = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        db.addChildEventListener(cel);
    }

    //WRITE
    public Boolean save(Curfew object)
    {
        if(object!=null)
        {
            try{
                DatabaseReference id = db.child("Child/" + childId + "/Curfew");
                id.child("start").setValue(object.getStart());
                id.child("end").setValue(object.getEnd());

                for(String val : object.getDays()) {
                    try{
                        id.child("days").child(val).setValue("true");
                    }catch(DatabaseException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return true;

            }catch(DatabaseException e){
                e.printStackTrace();
                return false;
            }

        }

        return false;

    }

    @Override
    public ArrayList retrieve() {
        return curfew_list;
    }

    public void fetchData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(ds.getKey().equals(childId)) { //belongs to current child
                curfew_list.clear();
                for (DataSnapshot wow : ds.child("Curfew").getChildren()) {
                    Curfew curfew = new Curfew();
                    curfew.setId(wow.getKey());
                    curfew.setStart(wow.child("start").getValue(String.class));
                    curfew.setEnd(wow.child("end").getValue(String.class));
                    ArrayList<String> days = new ArrayList<>();
                    for(DataSnapshot d : wow.child("days").getChildren()){
                        days.add(d.getKey());
                    }
                    curfew.setDays(days);
                    curfew_list.add(curfew);
                    System.out.println("key" + wow.getKey());
                }
            }
        }
    }
}
