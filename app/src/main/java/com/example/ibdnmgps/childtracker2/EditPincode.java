package com.example.ibdnmgps.childtracker2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.LOG;

public class EditPincode extends AppCompatActivity {

    private Button ok_btn;
    private EditText pincode,cur_pincode;
    private EditText confirm;
    private CheckBox checkbox;
    private ChildTrackerDatabaseHelper childTrackerDatabaseHelper;
    private int PERMISSION_SEND_SMS = 1;
    private PendingIntent sentPI, deliveredPI;
    private String SENT = "SMS_SENT";
    private String DELIVERED = "SMS_DELIVERED";
    private BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    private DatabaseReference db;
    private ArrayList<Parent> parent_list = new ArrayList<Parent>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pincode);

        childTrackerDatabaseHelper = new ChildTrackerDatabaseHelper(getApplicationContext());
        db = FirebaseDatabase.getInstance().getReference();
        //initialize components
        ok_btn = (Button) findViewById(R.id.ep_ok);
        pincode = (EditText) findViewById(R.id.ep_pincode);
        confirm = (EditText) findViewById(R.id.ep_confirm);
        cur_pincode = (EditText) findViewById(R.id.ep_cur_pincode);
        checkbox = (CheckBox) findViewById(R.id.ep_checkbox);

        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT),0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED),0);

        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                retrieveParent(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void savePincode(View view) {
        //Checks if current pincode is correct
        String currentPincode = childTrackerDatabaseHelper.getFiles("pin_code");
        Log.e(LOG, currentPincode+ " == "  + cur_pincode.getText().toString());
        if(currentPincode == null || currentPincode.isEmpty()){
            currentPincode = "1234";
        }

        if(!cur_pincode.getText().toString().equals(currentPincode)) cur_pincode.setError("Wrong pincode!");
        else if(!pincode.getText().toString().equals(confirm.getText().toString())){
            confirm.setError("Pincode and confirm pincode do not match!");
        }else if(pincode.getText().toString().length() != 4){
            confirm.setError("Must be 4 digit number!");
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Change pin code")
                    .setMessage("Do you really want to change pin code?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ContentValues values = new ContentValues();
                            values.put(ChildTrackerDatabaseHelper.KEY_PIN_CODE, pincode.getText().toString());
                            childTrackerDatabaseHelper.updateChildTracker(values);
                            if(checkbox.isChecked()){
                                if(!parent_list.isEmpty())
                                    for(Parent p : parent_list)
                                        sendSMS(pincode.getText().toString(),p.getPhoneNum());
                                else
                                    Toast.makeText(EditPincode.this, "No Parent!", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    private void sendSMS(String pincode, String number) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                !=  PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        }

            SmsManager sms =  SmsManager.getDefault();
            sms.sendTextMessage(number,null,
                    "The pincode for this childtracker has been changed: " + pincode, sentPI, deliveredPI);
    }


    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSentReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(EditPincode.this, "SMS Sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(EditPincode.this, "Generic Failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(EditPincode.this, "No Service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(EditPincode.this, "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(EditPincode.this, "Radio Off", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(EditPincode.this, "SMS Delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(EditPincode.this, "SMS not Delivered", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));


    }

    private void retrieveParent(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(dataSnapshot.getKey().equals("Parent") && ds.child("children/"+childTrackerDatabaseHelper.getFiles("child_ref").toString()).getValue() != null) {
                Parent parent = new Parent();
                parent.setId(ds.getKey());
                parent.setPhoneNum(ds.child("phoneNum").getValue(String.class));
                parent.setName(ds.child("name").getValue(String.class));
                if(!parent_list.contains(parent))
                    parent_list.add(parent);
                System.out.println(parent.getName());
            }
        }
    }
}
