package com.example.ibdnmgps.childtracker2;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;




public class childList_home extends ListActivity {
    DatabaseReference db;
    ChildFirebaseHelper helper;
    ChildAdapter adapter;
    ChildTrackerDatabaseHelper h;
    String user_key;
    ArrayList<Child> child_list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_list_home);

        //setup database
        db= FirebaseDatabase.getInstance().getReference();
        helper=new ChildFirebaseHelper(db);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        user_key = h.getFiles("child_ref"); //todo user_ref
        System.out.println("user keyyyy!" + user_key);
        adapter = new ChildAdapter(childList_home.this, R.layout.mylist, child_list);



        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieve(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                retrieve(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Database error occured!");
            }

        });

        this.setListAdapter(adapter);

        Intent i = new Intent(getApplicationContext(), SOSNotifService.class);
        startService(i);
    }


    @Override
    public void onListItemClick(ListView listView, View itemView, int position, long id){
        Intent intent = new Intent(childList_home.this, ChildInformation.class);
        intent.putExtra("child_ref", child_list.get(position).getID());
        intent.putExtra("name", child_list.get(position).getName());
        intent.putExtra("phoneNum", child_list.get(position).getPhoneNum());

        startActivity(intent);
    }

    public void retrieve(DataSnapshot dataSnapshot) {
        System.out.println("retrieving ...");
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(dataSnapshot.getKey().equals("Child") && ds.child("parents/"+user_key).getValue() != null) {
                Child child = new Child();
                child.setId(ds.getKey());
                child.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                child.setName(ds.child("name").getValue(String.class));
                if(!child_list.contains(child))
                    child_list.add(child);
                System.out.println(child.getName());
            }
            adapter.notifyDataSetChanged();
        }


    }

}

