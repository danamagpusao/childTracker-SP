package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.R.attr.name;

public class UserInfoActivity extends AppCompatActivity {
    private static String TAG = "UserInfoActivity";
    private FirebaseUser user;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Spinner UImode;
    private EditText UIname;
    private EditText UIpassword;
    private boolean isProfileSuccess = false;
    private DatabaseReference db;
    private ChildTrackerDatabaseHelper h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        db =  Utils.getDatabase().getReference();
        h = new ChildTrackerDatabaseHelper(getApplicationContext());


        user = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (user == null) {
            returnToSignUp ();

        }

        UImode = (Spinner) findViewById(R.id.ui_spinner);
        UIname = (EditText) findViewById(R.id.ui_nickname);

        Button UInext = (Button) findViewById(R.id.ui_next);

        UInext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                completeSignUp();
            }
        });

    }

    private void completeSignUp(){

        String name = UIname.getText().toString();
        String type = String.valueOf(UImode.getSelectedItem()).toLowerCase();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            isProfileSuccess = true;
                        }
                    }
                });


        if(isProfileSuccess) {
            ContentValues values = new ContentValues();
            if(type.equals("child")){ //save to firebase database
                saveToDBUser(name,"child");
                values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, type);
                values.put(ChildTrackerDatabaseHelper.KEY_MODE, type);
                values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, user.getUid());
                h.updateChildTracker(values);
            } else if(type.equals("parent")){
                Log.d(TAG,"Parent:in");
                saveToDBUser(name,"parent");
                values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, type);
                values.put(ChildTrackerDatabaseHelper.KEY_MODE, type);
                values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, user.getUid());
                h.updateChildTracker(values);
            }
        }


    }

    private void returnToSignUp () {
        Intent intent = new Intent(this,LogInActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveToDBUser(String name,String type){

        if(type.equals("child")) {
            Log.d(TAG, "createChildUser:in");
            Child object = new Child();
            object.setName(name);
            object.setPhoneNum(user.getPhoneNumber());
            object.setId(user.getUid());

            ChildFirebaseHelper helper = new ChildFirebaseHelper(db);
            if (!helper.save(object).equals("")) {
                Intent intent = new Intent(UserInfoActivity.this, ChildHome.class);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "FirebaseDatabase_AddChild:Failed");
                Toast.makeText(this, "Firebase Database Error", Toast.LENGTH_SHORT);
            }
        }
        else if(type.equals("parent")){
            Log.d(TAG, "createParentUser:in");
            Parent object = new Parent();
            object.setName(name);
            object.setPhoneNum(user.getPhoneNumber());
            object.setId(user.getUid());
            object.setReceiveSMS(false);

            ParentFirebaseHelper helper = new ParentFirebaseHelper(db);
            if (!helper.save(object).equals("")) {
                Intent intent = new Intent(UserInfoActivity.this, childList_home.class);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "FirebaseDatabase_AddParent:Failed");
                Toast.makeText(this, "Firebase Database Error", Toast.LENGTH_SHORT).show();
            }
        }
    }





}
