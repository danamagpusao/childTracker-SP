package com.example.ibdnmgps.childtracker2;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;


import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CurfewList extends ListActivity {
    private int child_id;
    private ChildTrackerDatabaseHelper h;
    FloatingActionButton add_btn;
    DatabaseReference db;
    CurfewFirebaseHelper helper;
    ArrayList<Curfew> curfew_list = new ArrayList<>();
    CurfewAdapter adapter;
    String child_ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curfew_list);
        add_btn = (FloatingActionButton) findViewById(R.id.curfew_list_add_btn);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        db= FirebaseDatabase.getInstance().getReference();
        child_ref = getIntent().getExtras().getString("child_ref");
        if(child_ref.equals("na")){
            child_ref = h.getFiles("child_ref"); //todo change to user_ref
        }
        helper=new CurfewFirebaseHelper(db, child_ref);
        adapter = new CurfewAdapter(this, R.layout.layout_curfew_list,curfew_list);
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveCurfew(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieveCurfew(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                retrieveCurfew(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        this.setListAdapter(adapter);

    }

    private void retrieveCurfew(DataSnapshot dataSnapshot) {
        for (DataSnapshot wow : dataSnapshot.child(child_ref).child("Curfew").getChildren()) {
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
        adapter.notifyDataSetChanged();
    }

    public void showAddCurfew(View view){
        // redirect to addCurfew Activity
        Intent intent = new Intent(CurfewList.this, AddCurfew.class);
        intent.putExtra("child_ref",child_ref);
        startActivity(intent); //go to add curfew
        finish();
    }

    @Override
    public void onListItemClick(ListView listView, View itemView, final int position, long id){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.curfew_list_options);
        dialog.setTitle("Options");
        Button del = (Button) dialog.findViewById(R.id.delete_curfew_btn);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.child("Child/" + child_ref + "/Curfew/" + curfew_list.get(position).getId()).removeValue();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        dialog.show();
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("CurfewList", "http://[ENTER-YOUR-URL-HERE]");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }
}
