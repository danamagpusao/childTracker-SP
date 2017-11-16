package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.KEY_INT_OFF;
import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.KEY_INT_ON;
import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.KEY_OFFLINE_SEND;
import static com.example.ibdnmgps.childtracker2.ChildTrackerDatabaseHelper.TABLE_CHILDTRACKER_FILES;

public class Settings extends AppCompatActivity {
    private ChildTrackerDatabaseHelper h;
    private EditText on_et;
    private Switch sms;
    private Button pincode_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        h = new ChildTrackerDatabaseHelper(getApplicationContext());

        on_et = (EditText) findViewById(R.id.on_int);
        sms = (Switch) findViewById(R.id.is_sms);
        pincode_btn = (Button) findViewById(R.id.pincode_settings);

        on_et.setText(Integer.parseInt(h.getFiles("on"))/60000+"");
        if(Integer.parseInt(h.getFiles("sms")) == 1) sms.setChecked(true);
        else sms.setChecked(false);
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
        if(validator()) {
            values.put(ChildTrackerDatabaseHelper.KEY_INT_ON, Integer.parseInt(on_et.getText().toString()) * 60000);
            if (sms.isChecked()) values.put(ChildTrackerDatabaseHelper.KEY_OFFLINE_SEND, 1);
            else values.put(ChildTrackerDatabaseHelper.KEY_OFFLINE_SEND, 0);
            h.updateChildTracker(values);

            if (!h.getFiles("on").equals(on_et.getText().toString())) {
                stopService(new Intent(this, ChildTrackerService.class));
                startService(new Intent(this, ChildTrackerService.class));
                Toast.makeText(this, "Restart Service!", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    public boolean validator () {
        if(Double.parseDouble(on_et.getText().toString()) < 5) {
                on_et.setError("must be atleast 5!");
            return false;
       }
        return true;
    }
}
