package com.example.ibdnmgps.childtracker2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ChildLock extends AppCompatActivity {
    private ChildTrackerDatabaseHelper h;
    private String pincode;
    private EditText pincode_et;
    private String ref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_lock);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
        pincode_et = (EditText) findViewById(R.id.childLock_pincode);

        pincode = h.getFiles("pin_code");
        if(pincode == null || pincode.isEmpty()){
            pincode = "1234";
        }
    }

    public void unlock(View view) {
        System.out.println("RIGHT RIGHT>>>>>> " +pincode);
        System.out.println("OK OK OK OK >>>>>> " +pincode_et.getText().toString());
        if(pincode_et.getText().toString().equals(pincode)){
            Intent i = new Intent(ChildLock.this,MenuList.class);
            Intent intent = new Intent(ChildLock.this, MenuList.class);
            intent.putExtra("child_ref", ref);
            startActivity(intent);
            setResult(RESULT_OK);
            finish();
        }
        else{
            pincode_et.setError("Wrong Pincode!");
            pincode_et.setText("");
        }
    }
}
