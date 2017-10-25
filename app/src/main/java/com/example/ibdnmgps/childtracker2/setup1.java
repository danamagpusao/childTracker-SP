package com.example.ibdnmgps.childtracker2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class setup1 extends AppCompatActivity {
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup1);
        intent = new Intent(setup1.this, ConfirmPassword.class);
    }

    public void childDevice(View view){
        intent.putExtra("choice", "child_dev");
        startActivity(intent);
        finish();
    }

    public void parentDevice(View view){
        intent.putExtra("choice", "parent_dev");
        startActivity(intent);
        finish();
    }
}
