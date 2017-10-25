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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import static com.example.ibdnmgps.childtracker2.R.id.child_pic;
import static java.security.AccessController.getContext;


public class ChildAdapter extends ArrayAdapter<Child>{

    List<Child> children;
    Context context;
    StorageReference sr;

    public ChildAdapter(Context context, int textViewResourceId, List<Child> objects) {
        super(context, textViewResourceId, objects);
        children = objects;
        this.context = context;
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
        textView.setText(children.get(position).getName());
        ImageView imageView = (ImageView) v.findViewById(R.id.itemimage);
        sr = FirebaseStorage.getInstance().getReference().child("profile_pic").child(children.get(position).getID());
        if (sr != null)
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(sr)
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(imageView);


        return v;
    }

}
