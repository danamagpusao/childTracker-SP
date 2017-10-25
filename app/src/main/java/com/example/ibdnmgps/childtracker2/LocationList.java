package com.example.ibdnmgps.childtracker2;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class LocationList extends ListActivity {
    ChildTrackerDatabaseHelper h;
    DatabaseReference db;
    ArrayList<ChildLocation> location_list = new ArrayList<>();
    String[] itemname ={
            "No location yet",
    };
    String child_ref;
    LocationListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        db = FirebaseDatabase.getInstance().getReference();
        child_ref = this.getIntent().getExtras().getString("child_ref");
        h = new ChildTrackerDatabaseHelper(this);
        adapter = new LocationListAdapter(this, R.layout.layout_location_list, location_list);
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
        this.setListAdapter(adapter);

    }

    private void retrieveLocationList(DataSnapshot dataSnapshot) {
        if( dataSnapshot.child(child_ref).child("ChildLocation").getChildrenCount() >0 )
            location_list.clear();
        for (DataSnapshot wow : dataSnapshot.child(child_ref).child("ChildLocation").getChildren()) {
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
            System.out.println("key" + wow.getKey());
            System.out.println("lat:" + loc.getLocation().getLatitude());
            System.out.println("long:" + loc.getLocation().getLongitude());
        }
        adapter.notifyDataSetChanged();


    }

    @Override
    public void onListItemClick(ListView listView, View itemView, int position, long id){
        Intent intent = new Intent(LocationList.this, ViewMap.class);
        intent.putExtra("location", location_list.get(position).getLocation());
        intent.putExtra("child_ref",child_ref);
        startActivity(intent);
    }

}