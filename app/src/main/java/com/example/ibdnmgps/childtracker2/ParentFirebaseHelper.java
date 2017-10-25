package com.example.ibdnmgps.childtracker2;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.R.attr.id;


public class ParentFirebaseHelper extends FirebaseHelper {
    DatabaseReference db;
    Boolean saved;
    String childId;
    ArrayList<Parent> parent_list=new ArrayList<>();
    ArrayList<String> parent_key_list = new ArrayList<String>();


    public ParentFirebaseHelper(DatabaseReference db) {
        this.db = db;
        db.keepSynced(true);
        childId = null;
        ChildEventListener cel = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(childId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(childId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(childId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                 fetchData(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(childId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                 fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        db.addChildEventListener(cel);
    }
    public ParentFirebaseHelper(DatabaseReference db, String childId) {
        this.db = db;
        db.keepSynced(true);
        this.childId = childId;
        final String tempChildId = childId;
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(tempChildId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                if(dataSnapshot.getKey() == "Parent") fetchData(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(tempChildId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                if(dataSnapshot.getKey() == "Parent") fetchData(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(tempChildId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                if(dataSnapshot.getKey() == "Parent") fetchData(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    if(ds.getKey().equals(tempChildId)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                if(dataSnapshot.getKey() == "Parent") fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    //WRITE
    public String save(Parent parent)
    {
        if(parent!=null)
        {
            try
            {
                   DatabaseReference id =  db.child("Parent").child(parent.getId());
                   id.setValue(parent);
                   return id.getKey();

            }catch (DatabaseException e)
            {
                e.printStackTrace();
                return "";
            }
        }

        return "";

    }

    public Boolean remove(String parent_id, String child_id)
    {
        if(parent_id!=null)
        {
            try
            {
                Query q = db.child("Child/"+child_id+"/parents/"+parent_id);
                Query p = db.child("Parent/"+parent_id+"/children/"+child_id);
                try{
                    q.getRef().removeValue();
                    p.getRef().removeValue();
                    return true;
                }catch(DatabaseException e) {
                    e.printStackTrace();
                    return false;
                }

            }catch (DatabaseException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    // UPDATE INFO

    public Boolean update(Parent parent)
    {
        if(parent!=null)
        {
            try
            {
                Query q = db.child("Parent").orderByKey().equalTo(parent.getId());
                try{
                    q.getRef().setValue(parent);
                    return true;
                }catch(DatabaseException e) {
                    e.printStackTrace();
                    return false;
                }

            }catch (DatabaseException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public Boolean addChild(String parentId,String childId)
    {
            try
            {
                db.child("Parent").child(parentId).child("children").child(childId).setValue(true);
                db.child("Child").child(childId).child("parents").child(parentId).setValue(true);
                return true;

            }catch (DatabaseException e)
            {
                e.printStackTrace();
                return false;
            }
    }

    public Boolean removeChild(String parentId,String childId)
    {
        try
        {
            db.child("Parent").child("children").child(childId).setValue(false);
            db.child("Child").child("parents").child(parentId).setValue(false);

        }catch (DatabaseException e)
        {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public void fetchData(DataSnapshot dataSnapshot)
    {
       parent_list.clear();
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
                if(parent_key_list.contains(ds.getKey())) {
                    Parent parent = new Parent();
                    parent.setId(ds.getKey());
                    parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                    parent.setName(ds.child("name").getValue(String.class));
                    parent.setPassword(ds.child("password").getValue(String.class));
                    parent_list.add(parent);
                    System.out.println("key" + ds.getKey());
                }
        }


    }

    public ArrayList<Parent> retrieve(){
        return parent_list;
    }

}
