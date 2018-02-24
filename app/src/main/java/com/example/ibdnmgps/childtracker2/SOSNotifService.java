package com.example.ibdnmgps.childtracker2;



import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


import android.media.RingtoneManager;
import android.os.IBinder;

import android.support.annotation.Nullable;

import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


/**
 * 7/7/2017
 *
 */
public class SOSNotifService extends Service {

    private final String TAG = "SOSNotifService";

    private DatabaseReference db;
    private String ref;
    private ChildTrackerDatabaseHelper h;


    NotificationCompat.Builder mBuilder;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ref = h.getFiles("child_ref");
        db = Utils.getDatabase().getReference("SOS");
        mBuilder = new NotificationCompat.Builder(this);

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                promptSOS(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                promptSOS(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                promptSOS(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                promptSOS(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void promptSOS(final DataSnapshot mainData) {
        final String child_ref = mainData.child("child_ref").getValue(String.class);
        final String child_name = mainData.child("child_name").getValue(String.class);
        final Double loc_lat = mainData.child("loc_lat").getValue(Double.class);
        final Double loc_long = mainData.child("loc_long").getValue(Double.class);
        final String loc_time = mainData.child("time_created").getValue(String.class);
        final String parent_ref = mainData.child("parent_ref").getValue(String.class);
        if(child_ref != null && parent_ref != null && parent_ref.equals(ref)) {
            Log.v(TAG,"New SOS Message");
            Location loc = new Location("GPS");
            loc.setLatitude(loc_lat);
            loc.setLongitude(loc_long);

            android.support.v4.app.NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(SOSNotifService.this)
                            .setSmallIcon(R.mipmap.logo_round)
                            .setContentTitle("SOS")
                            .setContentText(child_name + " sent you an SOS(" +
                                    loc_time + ")!")
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
            db.child(mainData.getKey()).removeValue();
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "SOSNotifService Stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }



}
