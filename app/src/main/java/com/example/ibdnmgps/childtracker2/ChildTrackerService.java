package com.example.ibdnmgps.childtracker2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.R.attr.data;
import static android.R.attr.name;

/**
 * Modified 6/30/2017 for App,
 *  based from Sample code by filipp on 6/16/2016.
 */
public class ChildTrackerService extends Service {

    private static String TAG = "ChildTrackerService";

    private LocationListener listener;
    private LocationManager locationManager;

    private DatabaseReference db;
    private LocationFirebaseHelper helper;
    private String ref;
    private ChildTrackerDatabaseHelper h;
    private  Handler mHandler=new Handler();
    private Location curLoc = null;
    private boolean is_run = true;
    private ArrayList<String> parent_key_list = new ArrayList<>();
    private ParentFirebaseHelper ch;
    private ArrayList<Parent> parent_list = new ArrayList<>();


    //SMS feature
    private int PERMISSION_SEND_SMS = 1;
    private PendingIntent sentPI, deliveredPI;
    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ref = h.getFiles("child_ref");
        db = FirebaseDatabase.getInstance().getReference();
        ch = new ParentFirebaseHelper(db,ref);
        helper = new LocationFirebaseHelper(db,ref);

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(ref)) { //belongs to current child
                        parent_key_list.clear();
                        for (DataSnapshot wow : ds.child("parents").getChildren()) {
                            if (!parent_key_list.contains(wow.getKey()))
                                parent_key_list.add(wow.getKey());
                        }
                    }
                }
                parent_list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (parent_key_list.contains(ds.getKey())) {
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
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // first.remove(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        // listener to check if the child is at HOME at specified curfew
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //broadcast location
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", location.getLongitude() + " " + location.getLatitude());
                sendBroadcast(i);
                handleNewLocation(location);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.parseInt(h.getFiles("on")), 0, listener); // ToDo
        try {

        } catch (Exception e) {
            try{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(h.getFiles("on")), 0, listener);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            e.printStackTrace();
        }

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update Sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update Generic Failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update No Service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update Radio Off", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update Delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(ChildTrackerService.this, "SMS Location Update not Delivered", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }

    public void sendSMS(ArrayList<Parent> list, ChildLocation loc) {
        for(Parent p : list) {
            sendToParent(p.getPhoneNum(),
                    "Location Update ("+loc.getTimeCreated() +"): \n" +
                    "lat:" + loc.getLocation().getLatitude() +"\n" +
                    "long:" + loc.getLocation().getLongitude()
                    );
        }

    }

    private void sendToParent(String number, String message) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                !=  PackageManager.PERMISSION_GRANTED){
        }

        SmsManager sms =  SmsManager.getDefault();
        sms.sendTextMessage(number,null,
               message, null, null);

    }


    private void handleNewLocation(Location location) {
        System.out.println("OHH HI" + location.toString());
        //todo save to DB
        ChildLocation child_loc = new ChildLocation();
        child_loc.setLocation(location);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        child_loc.setTimeCreated(formattedDate);
        if(helper.save(child_loc)!= null)
            System.out.println("saved to DB");

        if(!isNetworkAvailable()){
            sendSMS(parent_list,child_loc);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSentReceiver);


        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
        is_run = false;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }




}
