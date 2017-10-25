package com.example.ibdnmgps.childtracker2;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SafezoneList extends ListActivity {
    DatabaseReference db;
    FloatingActionButton add_btn;
    ArrayList<Safezone> safezone_list = new ArrayList<>();
    ChildTrackerDatabaseHelper h;
    String child_ref;
    SafezoneAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safezone_list);
        db = FirebaseDatabase.getInstance().getReference();
        add_btn = (FloatingActionButton) findViewById(R.id.safezone_list_add_btn);
        adapter = new SafezoneAdapter(this, R.layout.layout_safezone_list, safezone_list);
        this.setListAdapter(adapter);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        child_ref = getIntent().getExtras().getString("child_ref");
        if(child_ref.equals("na")){
            child_ref = h.getFiles("child_ref"); //todo change to user_ref
        }


        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveSafezone(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                retrieveSafezone(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieveSafezone(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void retrieveSafezone(DataSnapshot dataSnapshot){
        safezone_list.clear();
        for (DataSnapshot wow : dataSnapshot.child(child_ref).child("ChildLocation").getChildren()) {
            Safezone safezone = new Safezone();
            safezone.setId(wow.getKey());
            Location temp = new Location(wow.getKey());
            temp.setLatitude(wow.child("lat").getValue(Double.class));
            temp.setLongitude(wow.child("lon").getValue(Double.class));
            safezone.setCenter(temp);
            safezone.setRadius(wow.child("radius").getValue(Double.class));
            safezone.setIsHome(wow.child("isHome").getValue(Integer.class));
            safezone_list.add(safezone);
            System.out.println("key" + wow.getKey());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView listView, View itemView, int position, long id){
        Intent intent = new Intent(SafezoneList.this, ViewMap.class);
        intent.putExtra("location", safezone_list.get(position).getCenter());
        startActivity(intent);
    }
}
