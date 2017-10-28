package com.example.ibdnmgps.childtracker2;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


import android.media.RingtoneManager;
import android.os.Handler;
import android.os.IBinder;

import android.support.annotation.Nullable;

import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

/**
 * 7/7/2017
 *
 */
public class SOSNotifService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;

    private DatabaseReference db,maindb;
    private LocationFirebaseHelper helper;
    private String ref;
    private ChildTrackerDatabaseHelper h;


    NotificationCompat.Builder mBuilder;
    private String child_ref;
    private String loc_ref;
    String name;
    String phone_num;
    Boolean result = false;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ref = h.getFiles("child_ref");
        maindb = FirebaseDatabase.getInstance().getReference();
        db = FirebaseDatabase.getInstance().getReference("SOS");
        helper = new LocationFirebaseHelper(db,ref);
        mBuilder = new NotificationCompat.Builder(this);
        loc_ref = null;
        child_ref = null;

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                promptSOS(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        System.out.println("SOSNotifService Started --- ");

    }

    public void promptSOS(final DataSnapshot mainData) {
        if(mainData.child("received/"+ref).getValue() == null) {
            loc_ref = mainData.child("loc_ref").getValue(String.class);
            child_ref = mainData.child("child_ref").getValue(String.class);
            if (child_ref == null) mainData.child("child_reference").getValue(String.class);

            for (DataSnapshot mini : mainData.getChildren()) {
                System.out.println(mini.getValue(String.class) + " " + mini.getKey());
            }
            System.out.println("SOS NOTIF SERVICE " + child_ref + " " + loc_ref);
            final DatabaseReference child_fb = FirebaseDatabase.getInstance().getReference("Child/" + child_ref);
            child_fb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue(String.class);
                    Location loc = new Location("GPS");
                    if (dataSnapshot.child("ChildLocation").child(loc_ref).getValue() != null && mainData.child("loc_ref") != null) {
                        loc.setLatitude(dataSnapshot.child("ChildLocation").child(mainData.child("loc_ref").getValue(String.class) + "/lat").getValue(double.class));
                        loc.setLongitude(dataSnapshot.child("ChildLocation").child(mainData.child("loc_ref").getValue(String.class) + "/lon").getValue(double.class));

                        String timestamp = dataSnapshot.child("ChildLocation")
                                .child(mainData.child("loc_ref")
                                .getValue(String.class) + "/time_created")
                                .getValue(String.class);

                        //todo create push notif here
                        android.support.v4.app.NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(SOSNotifService.this)
                                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                                        .setContentTitle("SOS")
                                        .setContentText(name + " sent you an SOS("+
                                                timestamp+")!")
                                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));


                        Intent resultIntent = new Intent(SOSNotifService.this, ViewMap.class);
                        resultIntent.putExtra("location", loc);
                        resultIntent.putExtra("child_ref", child_ref);

                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        SOSNotifService.this,
                                        (int) System.currentTimeMillis(),
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.notify(001, mBuilder.build());

                        db.child(mainData.getKey() + "/received/" + ref).setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }

        Toast.makeText(this, "SOSNotifService Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SOSNotifService", "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }


}
