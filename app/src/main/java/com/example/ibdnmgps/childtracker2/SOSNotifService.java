package com.example.ibdnmgps.childtracker2;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Intent;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Handler;
import android.os.IBinder;

import android.support.annotation.Nullable;

import android.support.v7.app.NotificationCompat;

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

    private DatabaseReference db;
    private LocationFirebaseHelper helper;
    private String ref;
    private ChildTrackerDatabaseHelper h;
    private  Handler mHandler=new Handler();
    private Location curLoc = null;
    private boolean is_run = true;
    private ArrayList<String> parent_key_list = new ArrayList<>();
    private LocationFirebaseHelper ch;
    private ArrayList<Parent> parent_list = new ArrayList<>();
    NotificationCompat.Builder mBuilder;
    private String child_ref;
    private String loc_ref;
    String name;
    Location loc;
    String phone_num;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ref = h.getFiles("child_ref");
        db = FirebaseDatabase.getInstance().getReference("SOS");
        helper = new LocationFirebaseHelper(db,ref);
        mBuilder = new NotificationCompat.Builder(this);
        loc_ref = null;
        child_ref = null;

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    for(DataSnapshot d: dataSnapshot.getChildren()){ // stores SOS data
                        if(d.getKey().equals("child_ref")  && d.getValue() != null){
                            child_ref = d.getValue(String.class);
                        }
                        else if(d.getKey().equals("loc_ref") && d.getValue() != null){
                            loc_ref = d.getValue(String.class);
                        }
                    }


                    final DatabaseReference child_fb = FirebaseDatabase.getInstance().getReference("Child/"+child_ref);
                    final DatabaseReference loc_fb = child_fb.child("ChildLocation/"+loc_ref);
                    child_fb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            name = dataSnapshot.child("name").getValue(String.class);
                            phone_num = dataSnapshot.child("phoneNum").getValue(String.class);
                            Location loc = new Location("GPS");
                            for(DataSnapshot loc_child : dataSnapshot.child("ChildLocation").child(loc_ref).getChildren()){
                               if(loc_child.getKey().equals("lat") && loc_child.getValue() != null) {
                                    loc.setLatitude(loc_child.getValue(double.class));
                               }
                               else if(loc_child.getKey().equals("lon")  && loc_child.getValue() != null){
                                   loc.setLongitude(loc_child.getValue(double.class));
                               }
                            }
                            System.out.println("OPEN NOTIF:" + name + ": " +  phone_num);

                            //todo create push notif here
                            android.support.v4.app.NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(SOSNotifService.this)
                                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                                            .setContentTitle("SOS")
                                            .setContentText(name + "sent you an SOS.");


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

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                dataSnapshot.getRef().getParent().removeValue();

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



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
        is_run = false;
    }


}
