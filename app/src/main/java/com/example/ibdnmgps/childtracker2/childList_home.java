package com.example.ibdnmgps.childtracker2;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;




public class childList_home extends ListActivity {
    private final String TAG = "ChildList_home";
    private DatabaseReference db;
    private ChildFirebaseHelper helper;
    private ChildAdapter adapter;
    private ChildTrackerDatabaseHelper h;
    private String user_key;
    private ArrayList<Child> child_list = new ArrayList<>();
    private Button logout_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_list_home);

        //setup database
        db= Utils.getDatabase().getReference("");
        helper=new ChildFirebaseHelper(db);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        user_key = h.getFiles("child_ref"); //todo user_ref
        adapter = new ChildAdapter(childList_home.this, R.layout.mylist, child_list);
        logout_btn = (Button) findViewById(R.id.child_list_logout);
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(childList_home.this)
                        .setTitle("Log Out")
                        .setMessage("Do you really want to Log Out?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                stopService(new Intent(childList_home.this, SOSNotifService.class));
                                FirebaseAuth.getInstance().signOut();
                                h.resetDB();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    finishAndRemoveTask();
                                } else {
                                    finishAffinity();
                                }
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });



        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieve(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                retrieve(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieve(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                retrieve(dataSnapshot);
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
                Boolean add = true;
                for(Child c : child_list)// avoid duplicates
                    if (c.getID().equals(child.getID()))
                        add = false;
                if(add) child_list.add(child);
            }
            adapter.notifyDataSetChanged();
        }


    }

}

