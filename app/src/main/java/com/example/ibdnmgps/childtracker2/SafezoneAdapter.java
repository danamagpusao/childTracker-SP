package com.example.ibdnmgps.childtracker2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import static com.example.ibdnmgps.childtracker2.R.id.layout_safezone_list_delete;

/**
 * Created by ibdnmgps on 6/28/2017.
 */

public class SafezoneAdapter extends ArrayAdapter<Safezone> {

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
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        View v = convertView;
        View.OnClickListener open_safezone = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("clicked text view btn #" + items.get(pos));
            }
        };

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.layout_safezone_list, null);
        TextView lat = (TextView) v.findViewById(R.id.layout_safezone_list_lat);
        TextView lon = (TextView) v.findViewById(R.id.layout_safezone_list_long);
        Button delete = (Button) v.findViewById(layout_safezone_list_delete);

        delete.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 System.out.println("clicked delete btn #" + items.get(pos));
             }
         }
        );


       /*TODO test*/
        //Testing purpose only
         lat.setText("latlatlatlatlatlatlatlat");
         lon.setText("longlonglonglonglonglong");
         lat.setOnClickListener(open_safezone);
         lon.setOnClickListener(open_safezone);
       // lat.setText("Lat: " + items.get(position).getLatitude());
       // lon.setText("Long: " + items.get(position).getLongitude());
        return v;

    }
}
