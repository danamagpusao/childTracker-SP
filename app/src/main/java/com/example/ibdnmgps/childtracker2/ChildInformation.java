package com.example.ibdnmgps.childtracker2;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import static android.R.attr.data;
import static android.R.attr.name;
import static com.example.ibdnmgps.childtracker2.R.id.child_pic;

public class ChildInformation extends AppCompatActivity {
    StorageReference sr;
    Child child = new Child();
    TextView name;
    TextView phone_num;
    String child_ref;
    Intent i;
    String filePath = null;
    ImageView child_pic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_information);
        child_ref = this.getIntent().getExtras().getString("child_ref");
        name = (TextView) findViewById(R.id.ci_name);
        phone_num = (TextView) findViewById(R.id.ci_phone);

        name.setText(this.getIntent().getExtras().getString("name"));
        phone_num.setText(this.getIntent().getExtras().getString("phoneNum"));

        child_pic = (ImageView) findViewById(R.id.child_pic);
        child_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChildInformation.this, UploadPicActivity.class);
                intent.putExtra("child_ref", child_ref);
                startActivity(intent);
                finish();
            }
        });


        sr = FirebaseStorage.getInstance().getReference().child("profile_pic").child(child_ref);
        if (sr != null)
            Glide.with(ChildInformation.this)
                    .using(new FirebaseImageLoader())
                    .load(sr)
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(child_pic);

    }



    public void viewCurfew(View view){
        i = new Intent(ChildInformation.this, AddCurfew.class);
        i.putExtra("child_ref",child_ref);
        startActivity(i);
    }

    public void viewSafezone(View view){
        i = new Intent(ChildInformation.this, SafezoneList.class);
        i.putExtra("child_ref",child_ref);
        startActivity(i);
    }

    public void viewLocations(View view){
        i = new Intent(ChildInformation.this, LocationList.class);
        i.putExtra("child_ref",child_ref);
        startActivity(i);
    }
}
