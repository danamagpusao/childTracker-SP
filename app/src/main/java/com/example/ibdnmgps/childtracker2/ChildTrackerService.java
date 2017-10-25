package com.example.ibdnmgps.childtracker2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

                curLoc = location;

                //check if curfew
                final ArrayList<Curfew> curfew_list = new ArrayList<Curfew>();
                // -- retrieves curfew_list of child
                db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.getKey().equals(ref)) { //belongs to current child
                                for (DataSnapshot wow : ds.child("Curfew").getChildren()) {
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
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // --- checks date and time

                Date now = new Date();
                SimpleDateFormat time_format = new SimpleDateFormat("HH:mm");
                SimpleDateFormat day_format = new SimpleDateFormat("EEEE");

                for(Curfew cur : curfew_list) {
                    String day_str = day_format.format(now);
                    int start_diff = 0, end_diff = 0;
                    //--compare start time
                    try {
                        start_diff = now.compareTo(time_format.parse(cur.getStart()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //--compare end time
                    try {
                        end_diff = now.compareTo(time_format.parse(cur.getEnd()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(start_diff >= 0 ||  end_diff <=0  ){
                        // notif
                        android.support.v4.app.NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(ChildTrackerService.this)
                                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                                        .setContentTitle("CURFEW")
                                        .setContentText("Time to go home");


                        Intent resultIntent = new Intent(ChildTrackerService.this, ChildHome.class); // todo fix

                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        ChildTrackerService.this,
                                        (int) System.currentTimeMillis(),
                                        resultIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotifyMgr =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.notify(001, mBuilder.build());

                    }
                }

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
            smsService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void smsService() throws Exception{
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay
                (new Runnable() {
                    public void run() {
                        System.out.println("SCHEDULEDDD");
                        if(!parent_list.isEmpty()){
                            for(Parent parent : parent_list) {
                                SmsManager sm = SmsManager.getDefault();
                                // here is where the destination of the text should go
                                //String number = parent.getPhoneNum();
                                String number = "+6309175235809";
                                sm.sendTextMessage(number, null, "Your child is at lat:" + curLoc.getLatitude() + " long:" + curLoc.getLongitude(), null, null);

                            System.out.println("sent location to "+ parent.getName()+ ": " + parent.getPhoneNum() );
                            }
                        }



                    }
                }, 0, Integer.parseInt(h.getFiles("off")), TimeUnit.MILLISECONDS);
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
