package com.example.ibdnmgps.childtracker2;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    FloatingActionButton fa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        db = FirebaseDatabase.getInstance().getReference();
        child_ref = this.getIntent().getExtras().getString("child_ref");

        //initialize views
        fa = (FloatingActionButton) findViewById(R.id.location_list_view);
        fa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog d = new Dialog(LocationList.this);
                d.setContentView(R.layout.layout_manual_location);
                Button mView =  (Button) d.findViewById(R.id.manual_location_view);
                final EditText mLat =  (EditText) d.findViewById(R.id.manual_location_lat);
                final EditText mLon =  (EditText) d.findViewById(R.id.manual_location_lon);
                mView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        if(!mLat.getText().toString().equals("") &&
                                !mLat.getText().toString().equals("")) {
                            Location mLoc = new Location("manualLoc");
                            mLoc.setLatitude(Double.parseDouble(mLat.getText().toString()));
                            mLoc.setLongitude(Double.parseDouble(mLon.getText().toString()));
                            Intent intent = new Intent(LocationList.this, ViewMap.class);
                            intent.putExtra("location", mLoc);
                            intent.putExtra("child_ref",child_ref);
                            startActivity(intent);

                        } else {
                            mLat.setError("Invalid!");
                            mLon.setError("Invalid!");
                        }
                    }
                });
                d.show();
            }
        });

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
            if(location_list.size()>50)
                location_list.subList(50, location_list.size()).clear();
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
