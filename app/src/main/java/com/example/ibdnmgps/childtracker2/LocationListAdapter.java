package com.example.ibdnmgps.childtracker2;

/**
 * Created by ibdnmgps on 5/24/2017.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.ibdnmgps.childtracker2.R.id.textView;
import static java.security.AccessController.getContext;


public class LocationListAdapter extends ArrayAdapter<ChildLocation>{

    List<ChildLocation> location_list;

    public LocationListAdapter(Context context, int textViewResourceId, List<ChildLocation> objects) {
        super(context, textViewResourceId, objects);
        location_list = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.layout_location_list, null);
        TextView latitude_tv = (TextView) v.findViewById(R.id.locationList_lat);
        TextView longitude_tv = (TextView) v.findViewById(R.id.locationList_lon);
        TextView dateTime_tv = (TextView) v.findViewById(R.id.locationList_dateTime);
        latitude_tv.setText("Latitude: " + location_list.get(position).getLocation().getLatitude());
        longitude_tv.setText("Longitude: " + location_list.get(position).getLocation().getLongitude());
        dateTime_tv.setText("Retrieved: " + location_list.get(position).getTimeCreated());
        return v;

    }

}
