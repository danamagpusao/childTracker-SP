package com.example.ibdnmgps.childtracker2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.Manifest.permission_group.SMS;

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
    private ArrayList<Parent> parent_list = new ArrayList<>();
    private ArrayList<Safezone> safezone_list = new ArrayList<>();


    //SMS feature
    private PendingIntent sentPI, deliveredPI;
    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private int interval;
    private static boolean isSetAlarm = false;
    private Calendar startCurfew, endCurfew;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        ref = h.getFiles("child_ref");
        db = Utils.getDatabase().getReference();
        helper = new LocationFirebaseHelper(db,ref);
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);


        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
                retrieveSafezone(dataSnapshot);
                retrieveCurfew(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                retrieveSafezone(dataSnapshot);
                retrieveParent(dataSnapshot);
                retrieveCurfew(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieveSafezone(dataSnapshot);
                retrieveParent(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        

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
        interval = Integer.parseInt(h.getFiles("on"));
        setLocationManager();
    }

    private void setLocationManager() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, listener); // ToDo
        }catch(Exception e) {
            try{
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, 0, listener); // ToDo

            }catch(Exception ex) {
                Log.i(TAG, "cannot_retrieve_location");
            }
        }
    }

    public void sendSMS(ArrayList<Parent> list, ChildLocation loc) {
        if(Integer.parseInt(h.getFiles("sms")) == 1 ) {
            if(list.isEmpty()) System.out.println("Parent list EMPTY!");
            for (Parent p : list) {
                if (p.getReceiveSMS()) {
                    sendToParent(p.getPhoneNum(),
                            "Location Update (" + loc.getTimeCreated() + "): \n" +
                                    "Child ID:" + ref + "\n" +
                                    "lat:" + loc.getLocation().getLatitude() + "\n" +
                                    "long:" + loc.getLocation().getLongitude()
                    );

                    System.out.println("SENDING SMS TO " + p.getPhoneNum());
                }
            }
        } else {
            System.out.println("SMS Update disabled");
        }
    }

    private void sendToParent(String number, String message) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Sending SMS permission declined");
        }
        SmsManager.getDefault().sendTextMessage(number, null, message, sentPI, deliveredPI);
    }


    private void handleNewLocation(Location location) {
        if(!checkSafeZone(location)){
            ChildLocation child_loc = new ChildLocation();
            child_loc.setLocation(location);
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            String formattedDate = sdf.format(date);
            child_loc.setTimeCreated(formattedDate);
            if(helper.save(child_loc)!= null) {
                System.out.println("saved to DB");
            }
            if(!isNetworkAvailable()){ // sends SMS if internet connection is not available
                sendSMS(parent_list,child_loc);
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY);
            cal.set(Calendar.MINUTE, Calendar.MINUTE);
            if(cal.getTime().after(startCurfew.getTime()) && cal.getTime().before(endCurfew.getTime()) ){
                System.out.println("curfew timee");
                scheduleNotification(getNotification(2),2,System.currentTimeMillis()+5000);
            } else {
                System.out.println("not curfew timee");
            }
        }
    }

    private boolean checkSafeZone(Location location) {
        //checks if distance is within safezone radius
        Boolean bool= false;
        //checks if safezone-location distance is within atleast one safezone radius
        for(Safezone s : safezone_list) {
            Location temp = new Location("safezone");
            temp.setLatitude(s.getCenter().getLatitude());
            temp.setLongitude(s.getCenter().getLongitude());
            if(BigDecimal.valueOf(getDistance(location,temp)).compareTo(BigDecimal.valueOf(s.getRadius())) <= 0)  {
                bool = true;
            }
            System.out.println( "isSafezone?"+ bool + " " + getDistance(location,temp) + " vs " + s.getRadius() );
        }
        return bool;
    }

    private Double getDistance(Location location, Location safezone) {
        Double lat1 = deg2rad(location.getLatitude());
        Double lat2 = deg2rad(safezone.getLatitude());
        Double latDis = deg2rad(safezone.getLatitude() - location.getLatitude());
        Double lonDis = deg2rad(safezone.getLongitude() - location.getLongitude());
        double a = (Math.sin(latDis/2) * Math.sin(latDis/2)) + (Math.cos(lat1) * Math.cos(lat2) * Math.sin(lonDis/2) * Math.sin(lonDis/2));
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private Double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }


    private boolean isNetworkAvailable() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        boolean success = false;
        try {
            URL url = new URL("https://google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.connect();
            success = connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    private void retrieveParent(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (dataSnapshot.getKey().equals("Parent") && ds.child("children/" + ref).getValue() != null) {
                Parent parent = new Parent();
                parent.setId(ds.getKey());
                parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                parent.setName(ds.child("name").getValue(String.class));
                if(ds.child("receiveSMS").getValue(Boolean.class) != null)
                    parent.setReceiveSMS(ds.child("receiveSMS").getValue(Boolean.class));
                else parent.setReceiveSMS(false);
                if (!parent_list.contains(parent))
                    parent_list.add(parent);
                System.out.println(parent.getName());
            }
        }
    }

    public void retrieveSafezone(DataSnapshot dataSnapshot){
        Boolean isDuplicate = false;
        for (DataSnapshot wow : dataSnapshot.child(ref).child("Safezone").getChildren()) {
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
            for(Safezone p : safezone_list) {
                if(safezone.getId().equals( p.getId())) isDuplicate = true;
            }
            if(!isDuplicate) safezone_list.add(safezone);
        }
    }

    private void retrieveCurfew(DataSnapshot dataSnapshot) {
        for (DataSnapshot wow : dataSnapshot.child(ref).getChildren()) {
            if (wow.getKey().equals("Curfew")) {
                Curfew curfew = new Curfew();
                curfew.setStart(wow.child("start").getValue(String.class));
                curfew.setEnd(wow.child("end").getValue(String.class));

                startCurfew = setCalendar(curfew.getStart());
                endCurfew = setCalendar(curfew.getEnd());

                System.out.println(startCurfew.get(Calendar.HOUR_OF_DAY) +":" + startCurfew.get(Calendar.MINUTE));
                System.out.println(endCurfew.get(Calendar.HOUR_OF_DAY) +":" + endCurfew.get(Calendar.MINUTE));
                if(!isSetAlarm){
                    scheduleNotification(getNotification(0),0,startCurfew.getTimeInMillis());
                    scheduleNotification(getNotification(1),1,endCurfew.getTimeInMillis());
                    isSetAlarm = true;
                }
            }
        }
    }

    private Calendar setCalendar(String time){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        String[] string = time.split(":");
        if(Integer.parseInt(string[1]) < 15){
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(string[0])-1);
            cal.set(Calendar.MINUTE, 60-15+Integer.parseInt(string[1]));
        } else {
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(string[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(string[1])-15);
        }
        if(System.currentTimeMillis()> cal.getTimeInMillis())
            cal.add(Calendar.DATE, 1);
        return cal;
    }

    private void scheduleNotification(Notification notification, int _id, long time) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, _id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, _id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if(_id == 2){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
             } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        }
        else alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);

        System.out.println(">>> set alarm " + _id);
    }

    private Notification getNotification(int _id) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Curfew");
        if(_id == 0)
            builder.setContentText("15 minutes before curfew");
        else if(_id == 1)
            builder.setContentText("15 minutes before end of curfew");
        else if(_id==2)
            builder.setContentText("CURFEW TIME!");
        builder.setSmallIcon(R.mipmap.logo_round);
        return builder.build();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }



}
