package com.example.ibdnmgps.childtracker2;

import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.storage.StorageReference;



public class ViewMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location loc;
    private StorageReference sr;
    private  Bitmap b =null;
    private String child_ref = null;
    private MarkerOptions mo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loc = getIntent().getExtras().getParcelable("location");
        child_ref = getIntent().getExtras().getString("child_ref");


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        final LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("child")
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));


        //todo customize marker
    }
}
