package com.example.ibdnmgps.childtracker2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;


/**
 * Created by ibdnmgps on 6/28/2017.
 */

public class SafezoneAdapter extends ArrayAdapter<Safezone>{

    final List<Safezone> items;

    public SafezoneAdapter(Context context, int textViewResourceId, List<Safezone> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.layout_safezone_list, null);
        TextView lat = (TextView) v.findViewById(R.id.layout_safezone_list_lat);
        TextView lon = (TextView) v.findViewById(R.id.layout_safezone_list_long);
        lat.setText("Lat: " + items.get(position).getCenter().getLatitude());
        lon.setText("Long: " + items.get(position).getCenter().getLongitude());
        return v;
    }

}
