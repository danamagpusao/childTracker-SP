package com.example.ibdnmgps.childtracker2;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.ibdnmgps.childtracker2.R.id.textView;

public class ChildHome extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver;
    private DatabaseReference db;
    private ArrayList<Parent> parent_list = new ArrayList<>();
    private ChildTrackerDatabaseHelper h;
    private String ref;
    private LocationManager locationManager;
    private LocationFirebaseHelper helper;
    private int PERMISSION_SEND_SMS = 1;
    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private PendingIntent sentPI, deliveredPI;
    private BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;
    private Button sosBtn;
    private Boolean mBooleanIsPressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);
        db = FirebaseDatabase.getInstance().getReference();
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ref = h.getFiles("child_ref");
        helper = new LocationFirebaseHelper(db, ref);

        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT),0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED),0);
        sosBtn = (Button) findViewById(R.id.sosBtn);

        sosBtn.setOnTouchListener(new View.OnTouchListener()
        {
            private final Handler handler = new Handler();
            private final Runnable runnable = new Runnable() {
                public void run() {
                    if(mBooleanIsPressed)
                    {
                        SOS();
                    }
                }
            };


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Execute your Runnable after 5000 milliseconds = 5 seconds.
//After this 5secs it will check if is pressed
                    handler.postDelayed(runnable, 5000);
                    mBooleanIsPressed = true;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(mBooleanIsPressed) {
                        mBooleanIsPressed = false;
                        handler.removeCallbacks(runnable);
                    }
                }
                return false;
            }
        });


        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        if (!runtime_permissions()) {
            Intent i = new Intent(getApplicationContext(), ChildTrackerService.class);
            startService(i);
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

    }

    public void gotoMenu(View view) {
        Intent intent = new Intent(ChildHome.this, ChildLock.class);
        intent.putExtra("child_ref", ref);
        startActivity(intent);

    }

    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

            return true;
        }
        return false;
    }

    public void SOS() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        ChildLocation child_loc = new ChildLocation();
        child_loc.setLocation(location);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        child_loc.setTimeCreated(formattedDate);
        String loc_ref =helper.save(child_loc);

        //trigger SMS
        try {
            //trigger SMS
            if(!parent_list.isEmpty())
            for(Parent p : parent_list)
                sendSMS(p,child_loc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //save to firebase
        try {
            DatabaseReference sos = db.child("SOS").push();
            sos.child("loc_ref").setValue(loc_ref);
            sos.child("child_ref").setValue(ref);
        }catch(DatabaseException e){
            e.printStackTrace();
        }

    }

    private void sendSMS(Parent parent, ChildLocation child_loc){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                !=  PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        }

        SmsManager sms =  SmsManager.getDefault();
        sms.sendTextMessage(parent.getPhoneNum(),null,
                "SOS MESSAGE ("+child_loc.getTimeCreated()+ "\n" +
                "Lat:" + child_loc.getLocation().getLatitude() +
                "\n Long:" + child_loc.getLocation().getLongitude(), sentPI, deliveredPI);

    }



    private void retrieveParent(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(dataSnapshot.getKey().equals("Parent") && ds.child("children/"+ref).getValue() != null) {
                Parent parent = new Parent();
                parent.setId(ds.getKey());
                parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                parent.setName(ds.child("name").getValue(String.class));
                if(!parent_list.contains(parent))
                    parent_list.add(parent);
                System.out.println(parent.getName());

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(ChildHome.this, "SMS Sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(ChildHome.this, "Generic Failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(ChildHome.this, "No Service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(ChildHome.this, "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(ChildHome.this, "Radio Off", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                    switch (getResultCode()){
                        case Activity.RESULT_OK:
                            Toast.makeText(ChildHome.this, "SMS Delivered", Toast.LENGTH_SHORT).show();
                            break;
                        case Activity.RESULT_CANCELED:
                            Toast.makeText(ChildHome.this, "SMS not Delivered", Toast.LENGTH_SHORT).show();
                            break;

                    }
            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(ChildHome.this,intent.getExtras().get("coordinates")+"",Toast.LENGTH_SHORT).show();

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Intent i =new Intent(getApplicationContext(),ChildTrackerService.class);
                startService(i);
                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            }else {
                runtime_permissions();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSentReceiver);
    }




}
