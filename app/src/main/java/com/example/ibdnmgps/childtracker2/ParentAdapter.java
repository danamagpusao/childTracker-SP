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

import static java.security.AccessController.getContext;


public class ParentAdapter extends ArrayAdapter<Parent>{

    List<Parent> parent_list;

    public ParentAdapter(Context context, int textViewResourceId, List<Parent> objects) {
        super(context, textViewResourceId, objects);
        parent_list = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.mylist, null);
        TextView textView = (TextView) v.findViewById(R.id.Itemname);
        textView.setText(parent_list.get(position).getName());
        return v;
    }

}
