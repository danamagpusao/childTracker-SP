package com.example.ibdnmgps.childtracker2;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import static android.R.attr.bitmap;
import static android.graphics.Bitmap.createScaledBitmap;

//based from http://viralpatel.net/blogs/pick-image-from-galary-android-app/
public class UploadPicActivity extends Activity {


    private static int RESULT_LOAD_IMAGE = 1;
    private StorageReference uStorageReference;
    private ProgressDialog uProgressDialog;
    private  Button buttonLoadImage;
    private Button buttonUploadImage;
    private Uri selectedImage;
    private Bitmap smaller;
    private String child_ref = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pic);
        uStorageReference = FirebaseStorage.getInstance().getReference();
        uProgressDialog = new ProgressDialog(this);
        buttonLoadImage = (Button) findViewById(R.id.ua_browse_btn);
        buttonUploadImage = (Button) findViewById(R.id.ua_upload_btn);
        child_ref = getIntent().getExtras().getString("child_ref");
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        buttonUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUpload(view);
            }
        });

        runtime_permissions();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK ) {

            selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            System.out.println(picturePath+" i got this");
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.ua_image);

            smaller = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(picturePath),
                    120, 120, false);

            imageView.setImageBitmap(smaller);

            buttonUploadImage.setEnabled(true);

        }


    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ){

            requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE},100);

            return true;
        }else if(child_ref != null){
            buttonLoadImage.setEnabled(true);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && child_ref != null ){
                buttonLoadImage.setEnabled(true);
            }else {
                runtime_permissions();
            }
        }
    }

    public void onUpload(View view){
        uProgressDialog.setMessage("Uploading Image ...");
        uProgressDialog.show();

        String filename = "profile_picture_"+child_ref;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        smaller.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File ExternalStorageDirectory = Environment.getExternalStorageDirectory();
        File file = new File(ExternalStorageDirectory + File.separator + filename);

        FileOutputStream fileOutputStream = null;
        try {
            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes.toByteArray());

            Toast.makeText(UploadPicActivity.this,
                    file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        final StorageReference filepath = uStorageReference.child("profile_pic").child(child_ref);
        filepath.putFile(Uri.fromFile(file)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(UploadPicActivity.this,"Upload Complete",Toast.LENGTH_SHORT);
                uProgressDialog.dismiss();
                Intent intent = new Intent(UploadPicActivity.this, ChildInformation.class);
                intent.putExtra("child_ref",child_ref);
                startActivity(intent);
                finish();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadPicActivity.this,"Upload Failed",Toast.LENGTH_SHORT);
            }
        });


    }
}