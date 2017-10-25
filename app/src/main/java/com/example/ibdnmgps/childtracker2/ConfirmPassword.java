package com.example.ibdnmgps.childtracker2;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ConfirmPassword extends AppCompatActivity {

    ChildTrackerDatabaseHelper h;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_password);
        h = new ChildTrackerDatabaseHelper(getApplicationContext());
    }

    public void confirmPassword(View view) {
        EditText passField1 = (EditText)findViewById(R.id.password1);
        EditText passField2 = (EditText)findViewById(R.id.password2);
        String confirm_password = passField2.getText().toString();
        String first_password =  passField1.getText().toString();
        System.out.println("+"+confirm_password + "==" + first_password+"+");

        // save password to DB
        if(confirm_password.equals(first_password)) {
            //save to local database
            ContentValues values = new ContentValues();

            String device_type = this.getIntent().getExtras().getString("choice");
            values.put(ChildTrackerDatabaseHelper.KEY_PIN_CODE, first_password);
            values.put(ChildTrackerDatabaseHelper.KEY_DEVICE, device_type);
            values.put(ChildTrackerDatabaseHelper.KEY_MODE, "parent_mod");
            values.put(ChildTrackerDatabaseHelper.KEY_FIRST, 0);

            Intent intent;
            if( device_type.equals("child_dev")){
                intent = new Intent(ConfirmPassword.this, AddChildInformation.class); //go to child information
                intent.putExtra("password",first_password);
                intent.putExtra("device_type",device_type);
                startActivity(intent);
                finish();
            }
            else if(device_type.equals("parent_dev") ){
                if(h.updateChildTracker(values) > 0){
                    intent = new Intent(ConfirmPassword.this, ParentLoginActivity.class); // go to childList_home
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(ConfirmPassword.this,"Database Error 001", Toast.LENGTH_LONG );
                }

            }

        }

        else{
            Toast.makeText(ConfirmPassword.this,"Password does not match previous input",Toast.LENGTH_LONG).show();
            passField1.setText("");
            passField2.setText("");
        }
    }
}
