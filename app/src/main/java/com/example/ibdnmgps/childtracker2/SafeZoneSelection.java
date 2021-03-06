package com.example.ibdnmgps.childtracker2;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.R.attr.type;
import static com.example.ibdnmgps.childtracker2.R.id.map;

public class SafeZoneSelection extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private GoogleMap mMap;
    private Location safezone_loc;
    private String child_ref = null;
    private MarkerOptions mo;
    private DatabaseReference db;
    private ArrayList<LatLng> location_list = new ArrayList<>();
    private Boolean isInitialized;
    private Button save_btn;
    private Marker marker;
    private EditText radius_txt;
    private TextView radius_cur_txt;
    private Safezone safezone;
    private LatLng markerlatlng;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_zone_selection);
        child_ref = getIntent().getExtras().getString("child_ref");
        safezone = getIntent().getExtras().getParcelable("safezone");
        String choice = getIntent().getExtras().getString("choice");

        db = FirebaseDatabase.getInstance().getReference();
        isInitialized = false;
        radius_txt = (EditText) findViewById(R.id.safezone_rad);
        radius_cur_txt = (TextView) findViewById(R.id.safezone_rad_txt);
        save_btn = (Button) findViewById(R.id.select_safezone);
        save_btn.setOnClickListener(this);
        safezone_loc = new Location("center");
        mo = new MarkerOptions();
         if(choice == null){
            save_btn.setVisibility(View.GONE);
            radius_txt.setVisibility(View.GONE);
            if(safezone == null) finish();
        } else {
             radius_cur_txt.setVisibility(View.GONE);
             mo.draggable(true);
             mo.title("Drag and drop me!");
         }
        if(safezone!=null) {
            radius_cur_txt.setText("Radius: "+ safezone.getRadius() +" m");
            markerlatlng = new LatLng(safezone.getCenter().getLatitude(),safezone.getCenter().getLongitude());
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_safezone);
            mapFragment.getMapAsync(this);
        }

        else {
            db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    retrieveLocationList(dataSnapshot);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    retrieveLocationList(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    retrieveLocationList(dataSnapshot);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    retrieveLocationList(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void retrieveLocationList(DataSnapshot dataSnapshot) {
        if(dataSnapshot.child(child_ref).child("SafeZone") != null){
            if(dataSnapshot.child(child_ref).child("SafeZone").getChildrenCount() > 0)
                location_list.clear();

                for (DataSnapshot wow : dataSnapshot.child(child_ref).child("SafeZone").getChildren()) {
                    if(wow.child("lat").getValue(Double.class)!= null && wow.child("lon").getValue(Double.class)!= null) {
                        LatLng loc = new LatLng(wow.child("lat").getValue(Double.class), wow.child("lon").getValue(Double.class));
                        location_list.add(0, loc);
                    }
                }
                if(!location_list.isEmpty())
                    markerlatlng = location_list.get(0);

        }
        else if(dataSnapshot.child(child_ref).child("ChildLocation")!=null) {
                if (dataSnapshot.child(child_ref).child("ChildLocation").getChildrenCount() > 0)
                    location_list.clear();
                for (DataSnapshot wow : dataSnapshot.child(child_ref).child("ChildLocation").getChildren()) {
                    if(wow.child("lat").getValue(Double.class)!= null && wow.child("lon").getValue(Double.class)!= null) {
                        LatLng loc = new LatLng(wow.child("lat").getValue(Double.class), wow.child("lon").getValue(Double.class));
                        location_list.add(0, loc);
                    }
                }
            if(!location_list.isEmpty())
                markerlatlng = location_list.get(0);

        }
        if(markerlatlng == null){
            if (!runtime_permissions()) {
                Intent i = new Intent(getApplicationContext(), ChildTrackerService.class);
                startService(i);
                if (locationManager == null)
                    locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                Location loc = getLocation();
                if(loc == null) {
                    markerlatlng = new LatLng(14.651489,121.049309);
                } else {
                    markerlatlng = new LatLng(loc.getLatitude(), loc.getLongitude());
                }
            }
        }
        if (!isInitialized) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_safezone);
            mapFragment.getMapAsync(this);
        } else finish();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        isInitialized = true;
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerlatlng,15));
        mo.position(markerlatlng);
        if(marker == null)
            marker =  mMap.addMarker(mo);
        marker.showInfoWindow();
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("System out", "onMarkerDragStart..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                marker = arg0;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                // TODO Auto-generated method stub
                Log.d("System out", "onMarkerDragEnd..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                marker = arg0;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                // TODO Auto-generated method stub
                Log.i("System out", "onMarkerDrag...");
                marker = arg0;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                runtime_permissions();
            }
        }
    }
    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    private Location getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }

        //dangerous
        if(locationManager == null) return null;
        else return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    }


    @Override
    public void onClick(View view) {
        if(!radius_txt.getText().toString().equals("") && Double.parseDouble(radius_txt.getText().toString()) >= 100  ) {
            safezone_loc.setLatitude(marker.getPosition().latitude);
            safezone_loc.setLongitude(marker.getPosition().longitude);
            Double radius = Double.parseDouble(radius_txt.getText().toString());
            Safezone safezone_temp = new Safezone(safezone_loc, radius);
            SafezoneFirebaseHelper h = new SafezoneFirebaseHelper(db, child_ref);
            h.save(safezone_temp);
            stopService(new Intent(this, ChildTrackerService.class));
            startService(new Intent(this, ChildTrackerService.class));
            finish();
        } else {
            radius_txt.setError("Invalid radius. Must be at least 100 meters!");
        }
    }
}
