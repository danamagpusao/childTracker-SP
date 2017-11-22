package com.example.ibdnmgps.childtracker2;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
public class SafezoneList extends ListActivity implements View.OnClickListener{
    DatabaseReference db;
    FloatingActionButton add_btn;
    ArrayList<Safezone> safezone_list = new ArrayList<>();
    ChildTrackerDatabaseHelper h;
    String child_ref;
    SafezoneAdapter adapter;
    Dialog dialog;
    Button dDelete, dView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safezone_list);
        db = Utils.getDatabase().getReference();
        add_btn = (FloatingActionButton) findViewById(R.id.safezone_list_add_btn);
        adapter = new SafezoneAdapter(this, R.layout.layout_safezone_list, safezone_list);
        this.setListAdapter(adapter);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        child_ref = getIntent().getExtras().getString("child_ref");
        if(child_ref == null) finish();
        if(child_ref.equals("na")){
            child_ref = h.getFiles("child_ref"); //todo change to user_ref
        }
         //initializes dialog components
        dialog  = new Dialog(this);
        dialog.setTitle("Options");
        dialog.setContentView(R.layout.layout_safezone_options);

        dDelete = (Button) dialog.findViewById(R.id.safezone_options_delete);
        dView = (Button) dialog.findViewById(R.id.safezone_options_view);

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveSafezone(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                safezone_list.clear();
                retrieveSafezone(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                safezone_list.clear();
                retrieveSafezone(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(h.getFiles("device").equals("parent")) add_btn.setVisibility(View.GONE);
        else add_btn.setOnClickListener(this);
    }

    public void retrieveSafezone(DataSnapshot dataSnapshot){
        Boolean isDuplicate = false;
        for (DataSnapshot wow : dataSnapshot.child(child_ref).child("Safezone").getChildren()) {
            Safezone safezone = new Safezone();
            safezone.setId(wow.getKey());
            Location temp = new Location(wow.getKey());
            if(wow.child("lat").getValue(Double.class) != null && wow.child("lon").getValue(Double.class)!=null
                    && wow.child("radius").getValue(Double.class)!=null ) {
                temp.setLatitude(wow.child("lat").getValue(Double.class));
                temp.setLongitude(wow.child("lon").getValue(Double.class));
                safezone.setRadius(wow.child("radius").getValue(Double.class));
            }
            else return;
            safezone.setCenter(temp);

            safezone.setName(wow.child("name").getValue(String.class));
            System.out.println("sizee" + safezone_list.size());
            for(Safezone p : safezone_list) {
                if(safezone.getId().equals( p.getId())) isDuplicate = true;
            }
            if(!isDuplicate) safezone_list.add(safezone);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(ListView listView, View itemView, final int position, long id){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(view.getId()){
                    case R.id.safezone_options_delete:
                        Toast.makeText(SafezoneList.this,"delete " + position, Toast.LENGTH_SHORT).show();
                        SafezoneFirebaseHelper helper = new SafezoneFirebaseHelper(db);
                        helper.remove(child_ref,safezone_list.get(position).getId());
                        adapter.notifyDataSetChanged();
                        finish();
                        stopService(new Intent(SafezoneList.this, ChildTrackerService.class));
                        startService(new Intent(SafezoneList.this, ChildTrackerService.class));
                        break;

                    case R.id.safezone_options_view:
                        Intent intent = new Intent(SafezoneList.this, SafeZoneSelection.class);
                        intent.putExtra("child_ref", child_ref);
                        intent.putExtra("safezone", safezone_list.get(position));
                        startActivity(intent);
                        break;
                }
            }
        };
        adapter.notifyDataSetChanged();
        dDelete.setOnClickListener(listener);
        dView.setOnClickListener(listener);
        if(h.getFiles("device").equals("parent")) dDelete.setVisibility(View.GONE);
        dialog.show();
     }

    @Override
    public void onClick(View view) {
        if(safezone_list.size() < 3) {
            Intent intent = new Intent(SafezoneList.this, SafeZoneSelection.class);
            intent.putExtra("child_ref", child_ref);
            intent.putExtra("choice", "select");
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SafezoneList.this,"You can only add at most 3 safezones",Toast.LENGTH_SHORT).show();
        }
    }
}
