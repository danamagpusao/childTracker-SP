package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.KEY_INT_OFF;
import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.KEY_INT_ON;
import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.KEY_OFFLINE_SEND;
import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.TABLE_CHILDTRACKER_FILES;

public class Settings extends AppCompatActivity {
    private ChildTrackerDatabaseHelper h;
    private EditText on_et;
    private EditText off_et;
    private Switch sms;
    private Button pincode_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        h = new ChildTrackerDatabaseHelper(getApplicationContext());

        on_et = (EditText) findViewById(R.id.on_int);
        off_et = (EditText) findViewById(R.id.off_int);
        sms = (Switch) findViewById(R.id.is_sms);
        pincode_btn = (Button) findViewById(R.id.pincode_settings);

        on_et.setText(h.getFiles("on"));
        off_et.setText(h.getFiles("off"));
        if(sms.isChecked()) sms.setChecked(true);
        pincode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.this, EditPincode.class);
                startActivity(i);
            }
        });

    }

    public void saveSettings(View view) {
        ContentValues values = new ContentValues();
        // Todo Validator
        values.put(ChildTrackerDatabaseHelper.KEY_INT_OFF, off_et.getText().toString());
        values.put(ChildTrackerDatabaseHelper.KEY_INT_ON, on_et.getText().toString());
        if(sms.isChecked()) values.put(ChildTrackerDatabaseHelper.KEY_OFFLINE_SEND, 1);
        else  values.put(ChildTrackerDatabaseHelper.KEY_OFFLINE_SEND, 0);
        h.updateChildTracker(values);

        if(h.getFiles("on") != on_et.getText().toString() || h.getFiles("off") !=  off_et.getText().toString()){
            Intent i = new Intent(getApplicationContext(),ChildTrackerService.class);
            stopService(i);
            startService(i);
        }
        finish();
    }
}
