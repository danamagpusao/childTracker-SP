package com.example.ibdnmgps.childtracker2;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChildFirebaseHelper extends FirebaseHelper {

    ArrayList<String> child_refs = new ArrayList<>();
    ArrayList<Child> children = new ArrayList<Child>();
    String parentId;
    ChildEventListener cel;
    public ChildFirebaseHelper(DatabaseReference db){
        super(db);
        db.keepSynced(true);
        cel = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(parentId)) { //belongs to given parent
                        child_refs.clear();
                        for (DataSnapshot wow : ds.child("children").getChildren()) {
                            if (!child_refs.contains(wow.getKey()))
                                child_refs.add(wow.getKey());
                        }
                    }
                }
                if (dataSnapshot.getKey() == "Child") fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(parentId)) { //belongs to given parent
                        child_refs.clear();
                        for (DataSnapshot wow : ds.child("children").getChildren()) {
                            if (!child_refs.contains(wow.getKey()))
                                child_refs.add(wow.getKey());
                        }
                    }
                }
                if (dataSnapshot.getKey() == "Child") fetchData(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(parentId)) { //belongs to given parent
                        child_refs.clear();
                        for (DataSnapshot wow : ds.child("children").getChildren()) {
                            if (!child_refs.contains(wow.getKey()))
                                child_refs.add(wow.getKey());
                        }
                    }
                }
                if (dataSnapshot.getKey() == "Child") fetchData(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(parentId)) { //belongs to given parent
                        child_refs.clear();
                        for (DataSnapshot wow : ds.child("children").getChildren()) {
                            if (!child_refs.contains(wow.getKey()))
                                child_refs.add(wow.getKey());
                        }
                    }
                }
                if (dataSnapshot.getKey() == "Child") fetchData(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        db.addChildEventListener(cel);

    }

    public ChildFirebaseHelper(DatabaseReference db, String parentId){
        super(db);
        this.parentId = parentId;
    }
    //WRITE
    public String save(Child object) {
        if(object!=null)
        {
            try
            {
                DatabaseReference id = db.child("Child").child(object.getID());
                id.setValue(object);
                return id.getKey();

            }catch (DatabaseException e)
            {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }


    //READ
    public ArrayList<Child> retrieve() {
        return objects;
    }
    public void fetchData(DataSnapshot dataSnapshot)
    {
        children.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(child_refs.contains(ds.getKey())) {
                Child child = new Child();
                child.setId(ds.getKey());
                child.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                child.setName(ds.child("name").getValue(String.class));
                children.add(child);
                System.out.println("key" + ds.getKey());
            }
        }
    }

  }

