package com.example.ibdnmgps.childtracker2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class ParentLoginActivity extends AppCompatActivity {


    private EditText mNumberView;
    private EditText mPasswordView;
    private CountryCodePicker ccp;
    private DatabaseReference db;
    private ArrayList<Parent> all_parent_list = new ArrayList<>();
    private ChildTrackerDatabaseHelper h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        db = FirebaseDatabase.getInstance().getReference();

        db.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              all_parent_list.clear();
                 for (DataSnapshot ds : dataSnapshot.getChildren()) {
                     if (dataSnapshot.getKey().equals("Parent")) {
                         Parent parent = new Parent();
                         parent.setId(ds.getKey());
                         parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                         parent.setName(ds.child("name").getValue(String.class));
                         parent.setPassword(ds.child("password").getValue(String.class));
                         all_parent_list.add(parent);
                     }
                 }
             }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                all_parent_list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals("Parent")) {
                        Parent parent = new Parent();
                        parent.setId(ds.getKey());
                        parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                        parent.setName(ds.child("name").getValue(String.class));
                        parent.setPassword(ds.child("password").getValue(String.class));
                        all_parent_list.add(parent);

                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                all_parent_list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals("Parent")) {
                        Parent parent = new Parent();
                        parent.setId(ds.getKey());
                        parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                        parent.setName(ds.child("name").getValue(String.class));
                        parent.setPassword(ds.child("password").getValue(String.class));
                        all_parent_list.add(parent);

                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mNumberView = (EditText) findViewById(R.id.parent_num);
        mPasswordView = (EditText) findViewById(R.id.parent_password);
        ccp = (CountryCodePicker) findViewById(R.id.ccp_parent);

    }

    public void loginTrigger(View view) {
        String num = ccp.getSelectedCountryCodeWithPlus().toString() + mNumberView.getText().toString();
        String pas = mPasswordView.getText().toString();
        Parent parent = null;

        //checks if the login credentials exists
        for(Parent p: all_parent_list) {
            if(p.getPhoneNum().equals(num) && p.getPassword().equals(pas))
                parent = p;
        }
        //todo change key_child_ref to key_user_ref
        if(parent != null){
            ContentValues values = new ContentValues();
            values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, parent.getId());
            h.updateChildTracker(values);
            startActivity(new Intent(ParentLoginActivity.this,childList_home.class));
            Toast.makeText(ParentLoginActivity.this,
                    "Successfully Logged In!",
                    Toast.LENGTH_LONG);
            finish();
        }
        else{
            Toast.makeText(ParentLoginActivity.this,
                    "Parent does not exist!",
                    Toast.LENGTH_LONG);
        }


    }


}

