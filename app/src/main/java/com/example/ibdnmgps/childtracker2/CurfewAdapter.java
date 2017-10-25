package com.example.ibdnmgps.childtracker2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import static com.example.ibdnmgps.childtracker2.R.id.textView;
import static java.security.AccessController.getContext;

/**
 * Created by ibdnmgps on 6/28/2017.
 */

public class CurfewAdapter extends ArrayAdapter<Curfew> {

    List<Curfew> items;

    public CurfewAdapter(Context context, int textViewResourceId, List<Curfew> objects) {
        super(context, textViewResourceId, objects);
        items = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.layout_curfew_list, null);
        TextView title = (TextView) v.findViewById(R.id.layout_curfew_list_title);
        TextView subtitle = (TextView) v.findViewById(R.id.layout_curfew_list_subtitle);
        /*TODO Test*/
        title.setText(items.get(position).getStart() + "-" + items.get(position).getEnd());
        List days = items.get(position).getDays();
        String days_text = "";
        for(int i = 0; i < days.size(); i++){
            days_text = days_text+" "+days.get(i);
        }
        subtitle.setText(days_text);
        return v;

    }

}
