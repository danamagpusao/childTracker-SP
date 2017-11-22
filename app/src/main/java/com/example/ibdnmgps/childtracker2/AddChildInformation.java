package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

public class AddChildInformation extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";

    private DatabaseReference db;
    private ChildFirebaseHelper helper;
    private ChildTrackerDatabaseHelper h;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private boolean mVerificationInProgress = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_information);
        db = Utils.getDatabase().getReference();
        helper = new ChildFirebaseHelper(db);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());


    }

    public void addChild(View view) {
        EditText name_et = (EditText) findViewById(R.id.aci_name_field);
        EditText phone_num_et = (EditText) findViewById(R.id.aci_phonenum_field);
        CountryCodePicker ccp = (CountryCodePicker) findViewById(R.id.ccp);
        String name = name_et.getText().toString();
        String phone_num = ccp.getSelectedCountryCodeWithPlus().toString() + phone_num_et.getText().toString();


        if(name.length() > 0 && phone_num.length() == 14) {
            Child object = new Child();
            object.setName(name);
            object.setPhoneNum(phone_num);
            String ref = helper.save(object);
            if(!ref.equals("")){
                ContentValues values = new ContentValues();
                values.put(ChildTrackerDatabaseHelper.KEY_PIN_CODE, this.getIntent().getExtras().getString("password"));
                values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, this.getIntent().getExtras().getString("device_type"));
                values.put(ChildTrackerDatabaseHelper.KEY_MODE, "child_mod");
                values.put(ChildTrackerDatabaseHelper.KEY_FIRST, 0);
                values.put(ChildTrackerDatabaseHelper.KEY_CHILD_REF, ref);
                h.updateChildTracker(values);
                Intent intent = new Intent(AddChildInformation.this,ChildHome.class);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(this,"Cannot add Info, please make sure you are connected to the internet", Toast.LENGTH_LONG).show();
                finish();
            }

        }

    }

}
