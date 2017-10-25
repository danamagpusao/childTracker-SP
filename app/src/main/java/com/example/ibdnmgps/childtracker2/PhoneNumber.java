package com.example.ibdnmgps.childtracker2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class PhoneNumber extends AppCompatActivity {
    ChildTrackerDatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);
        db = new ChildTrackerDatabaseHelper(getApplicationContext());
    }

    public void save(View view){
        EditText numField = (EditText)findViewById(R.id.text_number);
        String num = numField.getText().toString();
        if(num.length() == 11 ){
            //     Format: Parent(phone_num, password)
            Parent p = new Parent(num,this.getIntent().getExtras().getString("password"));
            long id = db.createParent(p,1);
             if(id > -1){
                 Toast.makeText(PhoneNumber.this,"SUCCESS",Toast.LENGTH_LONG).show();
                 Intent intent = new Intent(PhoneNumber.this, childList_home.class);
                 startActivity(intent);
             }
            else
                Toast.makeText(PhoneNumber.this,"FAIL",Toast.LENGTH_LONG).show();

        }
        else{
            Toast.makeText(PhoneNumber.this,"Invalid phone number",Toast.LENGTH_LONG).show();
        }
    }
}
