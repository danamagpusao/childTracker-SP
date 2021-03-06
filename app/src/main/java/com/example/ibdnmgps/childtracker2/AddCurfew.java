package com.example.ibdnmgps.childtracker2;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.delay;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;

public class AddCurfew extends AppCompatActivity {
    TextView start_text, end_text;
    private int  mHour, mMinute;
    CurfewFirebaseHelper helper;
    DatabaseReference db;
    String currentChildId;
    Calendar startTime, endTime;
    Curfew curfew;
    ChildTrackerDatabaseHelper h;

    ArrayList<String> days;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_curfew);

        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        if(h.getFiles("device").equals("child"))
            currentChildId = h.getFiles("child_ref");
        else {
            currentChildId = getIntent().getExtras().get("child_ref").toString();
        }
        System.out.println("user keyyyy!" + currentChildId);
        db = Utils.getDatabase().getReference();
        helper = new CurfewFirebaseHelper(db,currentChildId);
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

        start_text = (TextView) findViewById(R.id.addcurfew_startTime);
        end_text = (TextView) findViewById(R.id.addCurfew_endTime);

        days = new ArrayList<>();

        View.OnClickListener set_time_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(h.getFiles("device").equals("child"))
                    setTime(v);
                else Toast.makeText(AddCurfew.this,"Curfew can only be set in Child Device",Toast.LENGTH_SHORT).show();
            }
        };
        start_text.setOnClickListener(set_time_listener);
        end_text.setOnClickListener(set_time_listener);

        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();

        System.out.println(currentChildId + "<<< add curfew child id");
        if(h.getFiles("device").equals("parent")){
            TextView mode = (TextView) findViewById(R.id.device_mode_add_curfew);
            mode.setBackgroundColor(ContextCompat.getColor(this,android.R.color.holo_purple));
            mode.setText(getString(R.string.common_parent_mode));
        }

    }

    public void setTime(View v) {
            System.out.println("SET TIME IN");
            final View view = v;

            // Get Current Time
            Calendar c = Calendar.getInstance();

            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            if(v == start_text) {
                // Start Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String hour = hourOfDay + "";
                                String min = minute + "";
                                if(hourOfDay < 10) hour = "0"+hourOfDay;
                                if(minute < 10) min = "0"+minute;
                                start_text.setText(hour + ":" + min);
                                startTime = Calendar.getInstance();
                                startTime.setTimeInMillis(System.currentTimeMillis());

                                if(minute < 15){
                                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay-1);
                                    startTime.set(Calendar.MINUTE, 60-15+minute);
                                } else {
                                    startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    startTime.set(Calendar.MINUTE, minute-15);
                                }
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();

            }
            else if( v == end_text){
                // End Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                String hour = hourOfDay + "";
                                String min = minute + "";
                                if(hourOfDay < 10) hour = "0"+hourOfDay;
                                if(minute < 10) min = "0"+minute;
                                end_text.setText(hour + ":" + min);
                                endTime = Calendar.getInstance();
                                endTime.setTimeInMillis(System.currentTimeMillis());

                                if(minute < 15){
                                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay-1);
                                    endTime.set(Calendar.MINUTE, 60-15+minute);
                                } else {
                                    endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    endTime.set(Calendar.MINUTE, minute-15);
                                }
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
    }

    public void addCurfew(View view) {
        if(!h.getFiles("device").equals("parent")) {
            Curfew curfew = new Curfew();
            curfew.setStart(start_text.getText().toString());
            curfew.setEnd(end_text.getText().toString());

            curfew.setDays(days);
            Boolean b = helper.save(curfew);
            if (!b) Toast.makeText(AddCurfew.this, "DB error", Toast.LENGTH_SHORT).show();
            else {
                stopService(new Intent(this, ChildTrackerService.class));
                startService(new Intent(this, ChildTrackerService.class));
            }
        }
        else {

            Toast.makeText(AddCurfew.this, "Curfew can only be edited in child's device", Toast.LENGTH_SHORT).show();
        }
    }


    private void retrieveCurfew(DataSnapshot dataSnapshot) {
        for (DataSnapshot wow : dataSnapshot.child(currentChildId).getChildren()) {
            if(wow.getKey().equals("Curfew")){
                curfew = new Curfew();
                curfew.setStart(wow.child("start").getValue(String.class));
                curfew.setEnd(wow.child("end").getValue(String.class));
                start_text.setText(curfew.getStart());
                end_text.setText(curfew.getEnd());
            }
            System.out.println("key" + wow.getKey());

        }
    }


}
